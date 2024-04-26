package ru.nsu.server.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.nsu.server.model.dto.ConstraintModel;
import ru.nsu.server.model.dto.ConstraintModelForVariants;
import ru.nsu.server.payload.requests.*;
import ru.nsu.server.payload.response.*;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.services.PotentialTimetableService;

import javax.validation.Valid;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/timetable/potential/change")
@Tag(name = "07. Potential Timetable Changing controller", description = "Контроллер для попыток внесения изменений в потенциальное расписание " +
        "с дополнительной проверкой от алгоритма.")
public class PotentialTimetableChangingController {

    private final PotentialTimetableService potentialTimetableService;

    private final OperationsRepository operationsRepository;

    @Value("${timetable.url.python.executable}")
    private String pythonExecutableURL;

    @Value("${timetable.url.python.algo.url}")
    private String pythonAlgoURL;

    @Value("${timetable.url.java.output}")
    private String outputFileURL;

    @Value("${timetable.url.java.test}")
    private String javaTestResources;

    @Value("${timetable.url.python.config}")
    private String pythoConfigUrl;

    @Autowired
    public PotentialTimetableChangingController(PotentialTimetableService potentialTimetableService,
                                                OperationsRepository operationsRepository) {
        this.potentialTimetableService = potentialTimetableService;
        this.operationsRepository = operationsRepository;
    }



    @Operation(
            summary = "Получение всех вариантов, куда можно переставить пару в потенциальном расписании.",
            description = """
                    Получается айди элемента из потенциального расписание, а затем перебираются все другие\s
                    дни недели + время пары + кабинеты, в которые можно поставить эту пару.
                    На выход идёт размер массива с вариантами и сами варианты.""",
            tags = {"actual timetable", "change"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = VariantsWithVariantsSize.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/pair_variants")
    @Transactional
    public ResponseEntity<?> findAllVariantsForPair(@RequestBody @Valid OnePairRequest onePairRequest) {
        Long pairId = onePairRequest.getSubjectId();
        List<ConstraintModelForVariants> constraintModelsForVariants = potentialTimetableService.findAllNewVariantsForPair(pairId);
        List<PotentialVariants> potentialVariantsForPair = new ArrayList<>();
        for (ConstraintModelForVariants currentConstraintModel : constraintModelsForVariants) {
            CompletableFuture<Boolean> future = executeScriptDBAsync(currentConstraintModel.getConstraintModels());
            try {
                boolean result = future.get(5, TimeUnit.SECONDS);
                if (result) {
                    potentialVariantsForPair.add(new PotentialVariants(currentConstraintModel.getPairId(), currentConstraintModel.getDayNumber(), currentConstraintModel.getSubjectName(),
                            currentConstraintModel.getGroups(), currentConstraintModel.getTeacher(), currentConstraintModel.getFaculty(), currentConstraintModel.getCourse(),
                            currentConstraintModel.getRoom(), currentConstraintModel.getPairNumber(), currentConstraintModel.getPairType()));
                } else {
                    continue;
                }
            } catch (TimeoutException e) {
                return ResponseEntity.ok(new MessageResponse("Расписание еще составляется, пожалуйста, подождите"));
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Ошибка выполнения: " + e.getMessage()));
            }
        }
        potentialTimetableService.saveConfigToFile(null);
        return ResponseEntity.ok(new VariantsWithVariantsSize(potentialVariantsForPair.size(), potentialVariantsForPair));
    }

    @Async("taskExecutor")
    public CompletableFuture<Boolean> executeScriptDBAsync(List<ConstraintModel> constraintModelList) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeTimeTableScript(false, constraintModelList);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }



    public Boolean executeTimeTableScript(boolean isTest, List<ConstraintModel> newConstraints) throws IOException, InterruptedException {
        String baseDir = System.getProperty("user.dir");
        String jsonFilePath = baseDir + javaTestResources;
        String inputURL = pythoConfigUrl;
        if (!isTest) {
            potentialTimetableService.saveConfigToFile(newConstraints);
            jsonFilePath = baseDir + inputURL;
        }
        String pythonExecutablePath = baseDir + pythonExecutableURL;
        String pythonScriptPath = baseDir + pythonAlgoURL;
        String outputFilePath = baseDir + outputFileURL;


        try (FileWriter fileWriter = new FileWriter(outputFilePath, false)) { // false - не добавлять, а переписывать
            fileWriter.write(""); // Очищаем файл
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
            writer.write("WORKING");
        } catch (IOException e) {
            log.error("Error writing to file: {}", outputFilePath, e);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath, pythonScriptPath, jsonFilePath);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        String output = new BufferedReader(new InputStreamReader(process.getInputStream(), "windows-1251")).lines().collect(Collectors.joining("\n"));
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("output in script execution: " + output);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("FAILED");
            } catch (IOException e) {
                log.error("Error writing to file: {}", outputFilePath, e);
            }
            return false;
//            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }

        String firstLine = output.split("\n")[0];
        log.info("Result = {}", firstLine);
        if ("FAILED".equals(firstLine)) {
            return false;
        } else if ("SUCCESSFULLY".equals(firstLine)) {
            return true;
        } else {
            log.info("Script output does not start with 'FAILED' or 'SUCCESSFULLY'.");
        }
        return true;
    }


    public List<FailureResponse> parseFailure(String input) {
        String[] lines = input.split("\n");
        List<FailureResponse> responses = new ArrayList<>();

        for (String line : lines) {
            FailureResponse response = new FailureResponse();
            String[] parts = line.split(", ");

            for (String part : parts) {
                String[] keyValuePair = part.split(": ");
                if (keyValuePair.length == 2) {
                    fillResponse(response, keyValuePair[0].trim(), keyValuePair[1].trim());
                }
            }

            responses.add(response);
        }

        return responses;
    }

    private void fillResponse(FailureResponse response, String key, String value) {
        switch (key) {
            case "Problem with Teacher":
                response.setTeacher(value);
                break;
            case "Subject":
                String subjectWithType = value.replaceAll("[()']", "").trim();
                int lastIndex = subjectWithType.lastIndexOf('_');
                String subjectName = subjectWithType.substring(0, lastIndex).replace("_", " ");
                String subjectType = subjectWithType.substring(lastIndex + 1);
                response.setSubject(subjectName);
                response.setSubjectType(subjectType);
                break;
            case "Group":
                response.setGroup(Integer.parseInt(value));
                break;
            case "Times in a week":
                response.setTimesInAWeek(Integer.parseInt(value));
                break;
            case "Day":
                response.setDay(Integer.parseInt(value));
                break;
            case "Period":
                response.setPeriod(Integer.parseInt(value));
                break;
            case "Room":
                response.setRoom(Integer.parseInt(value));
                break;
            default:
                break;
        }
    }

    @Operation(
            summary = "Попытка поставить пару в другой день в другой период.",
            description = """
                    Получается айди элемента из актуального расписание, новые желаемые данные, 
                    а сервер проверяет возможность этого изменения и делает его, если возможно.
                    !Важно - не вызывается алгоритм!""",
            tags = {"actual timetable", "change"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DataResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/day_and_pair_number")
    @Transactional
    public ResponseEntity<?> changeDayAndPairNumber(@RequestBody @Valid ChangeDayAndPairNumberRequest changeDayAndPairNumberRequest) throws InterruptedException {
        boolean changeResult = potentialTimetableService.changeDayAndPairNumber(changeDayAndPairNumberRequest);
        return ResponseEntity.ok(new DataResponse(changeResult));
    }

    @Operation(
            summary = "Попытка поставить пару в другой кабинет.",
            description = """
                    Получается айди элемента из актуального расписание, новые желаемый кабинет,\s
                    а сервер проверяет возможность этого изменения и делает его, если возможно.
                    !Важно - не вызывается алгоритм!""",
            tags = {"actual timetable", "change"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DataResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/room")
    @Transactional
    public ResponseEntity<?> changeRoom(@RequestBody @Valid ChangeRoomRequest changeRoomRequest) throws InterruptedException {
        boolean changeResult = potentialTimetableService.changeRoom(changeRoomRequest);
        return ResponseEntity.ok(new DataResponse(changeResult));
    }

    @Operation(
            summary = "Попытка поставить пару в другой день в другой период и в другую комнату.",
            description = """
                    Получается айди элемента из актуального расписание, новые желаемые данные, 
                    а сервер проверяет возможность этого изменения и делает его, если возможно.
                    !Важно - не вызывается алгоритм!""",
            tags = {"actual timetable", "change"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DataResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/day_and_pair_number_and_room")
    @Transactional
    public ResponseEntity<?> changeDayAndPairNumberAndRoom(@RequestBody @Valid ChangeDayAndPairNumberAndRoomRequest changeDayAndPairNumberRequest) {
        boolean changeResult = potentialTimetableService.changeDayAndPairNumberAndRoom(changeDayAndPairNumberRequest.getSubjectId(), changeDayAndPairNumberRequest.getNewDayNumber(),
                changeDayAndPairNumberRequest.getNewPairNumber(), changeDayAndPairNumberRequest.getNewRoom());
        return ResponseEntity.ok(new DataResponse(changeResult));
    }

    @Operation(
            summary = "Попытка передать пару другому преподавателю.",
            description = """
                    Получается айди элемента из актуального расписание, ФИО нового преподавателя, которые будет проводить пары, 
                    а сервер проверяет возможность этого изменения и делает его, если возможно.
                    !Важно - не вызывается алгоритм!""",
            tags = {"actual timetable", "change"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DataResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/teacher")
    @Transactional
    public ResponseEntity<?> changeTeacher(@RequestBody @Valid ChangeTeacherRequest changeTeacherRequest) {
        boolean changeResult = potentialTimetableService.changeTeacher(changeTeacherRequest);
        return ResponseEntity.ok(new DataResponse(changeResult));
    }

    //    @Operation(
//            summary = "Попытка поставить ОДНУ пару в любой другой день в другой период.",
//            description = """
//                    Получается айди элемента из актуального расписание, новые желаемые данные,
//                    а сервер проверяет возможность этого изменения и делает его, если возможно.
//                    !Важно - не вызывается алгоритм!""",
//            tags = {"actual timetable", "change"})
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DataResponse.class), mediaType = "application/json")}),
//            @ApiResponse(responseCode = "500", content = @Content)})
//    @PostMapping("/save_all_pairs_except_one")
//    @Transactional
//    public ResponseEntity<?> saveAllPairsExceptOne(@RequestBody @Valid OnePairRequest onePairRequest) {
//        List<ConstraintModel> constraintModelList = potentialTimetableService.changeOnePair(onePairRequest.getSubjectId());
//
//        CompletableFuture<Pair<String, Boolean>> future = executeScriptDBAsync(constraintModelList, false);
//        try {
//            Pair<String, Boolean> result = future.get(5, TimeUnit.SECONDS);
//            if (result.getRight()) {
////                simpMessagingTemplate.convertAndSend("Новое потенциальное расписание успешно составлено");
//                return ResponseEntity.ok(new MessageResponse("Потенциальное расписание успешно создано"));
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Ошибка при создании расписания: " + result.getLeft()));
//            }
//        } catch (TimeoutException e) {
//            return ResponseEntity.ok(new MessageResponse("Расписание еще составляется, пожалуйста, подождите"));
//        } catch (InterruptedException | ExecutionException e) {
//            Thread.currentThread().interrupt();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Ошибка выполнения: " + e.getMessage()));
//        }
//    }
}
