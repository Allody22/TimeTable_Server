package ru.nsu.server.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.nsu.server.model.config.ConfigModel;
import ru.nsu.server.model.config.ConstraintModel;
import ru.nsu.server.model.user.Operations;
import ru.nsu.server.payload.requests.*;
import ru.nsu.server.payload.response.DataResponse;
import ru.nsu.server.payload.response.FailureResponse;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.services.PotentialTimetableService;

import javax.validation.Valid;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
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
@Tag(name = "7. Potential Timetable Changing controller", description = "Контроллер для попыток внесения изменений в потенциальное расписание " +
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
            summary = "Попытка поставить ОДНУ пару в любой другой день в другой период.",
            description = """
                    Получается айди элемента из актуального расписание, новые желаемые данные, 
                    а сервер проверяет возможность этого изменения и делает его, если возможно.
                    !Важно - не вызывается алгоритм!""",
            tags = {"actual timetable", "change"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DataResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/save_all_pairs_except_one")
    @Transactional
    public ResponseEntity<?> changeDayAndPairNumber(@RequestBody @Valid OnePairRequest onePairRequest) {
        List<ConstraintModel> constraintModelList = potentialTimetableService.changeOnePair(onePairRequest.getSubjectId());

        CompletableFuture<Pair<String, Boolean>> future = executeScriptDBAsync(constraintModelList);
        try {
            Pair<String, Boolean> result = future.get(5, TimeUnit.SECONDS);
            if (result.getRight()) {
//                simpMessagingTemplate.convertAndSend("Новое потенциальное расписание успешно составлено");
                return ResponseEntity.ok(new MessageResponse("Потенциальное расписание успешно создано"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Ошибка при создании расписания: " + result.getLeft()));
            }
        } catch (TimeoutException e) {
            return ResponseEntity.ok(new MessageResponse("Расписание еще составляется, пожалуйста, подождите"));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Ошибка выполнения: " + e.getMessage()));
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Pair<String, Boolean>> executeScriptDBAsync(List<ConstraintModel> constraintModelList) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeTimeTableScript(false, constraintModelList);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Async("taskExecutor")
    public CompletableFuture<Pair<String, Boolean>> executeScriptTestAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeTimeTableScript(true, null);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void saveConfigToFile(ConfigModel configModel) {
        try {
            toJson(configModel);
        } catch (IOException e) {
            // Обработка ошибок ввода-вывода
            log.error("error with saving file to config: {}", e.getMessage());
        }
    }

    public static void toJson(ConfigModel configModel) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(configModel);

        String baseDir = System.getProperty("user.dir");

        String filePath = baseDir + "/TimeTable_Algo/src/resources/config_example.json";

        Files.writeString(Paths.get(filePath), json, Charset.forName("windows-1251"));

    }

    public Pair<String, Boolean> executeTimeTableScript(boolean isTest, List<ConstraintModel> newConstraints) throws IOException, InterruptedException {
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


        File outputFile = new File(outputFilePath);
        if (!outputFile.exists()) {
            boolean fileCreated = outputFile.createNewFile();
            if (!fileCreated) {
                throw new IOException("Could not create new file at: " + outputFilePath);
            }
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
            log.error("output 1: " + output);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("FAILED");
            } catch (IOException e) {
                log.error("Error writing to file: {}", outputFilePath, e);
            }
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }

        return parseAlgoResponse(outputFilePath, output);
    }

    private Pair<String, Boolean> parseAlgoResponse(String outputFilePath, String algoOutput) {
        log.error("output: " + algoOutput);
        String firstLine = algoOutput.split("\n")[0];
        String returnOutput = algoOutput.substring(algoOutput.indexOf('\n') + 1);

        if ("FAILED".equals(firstLine)) {
            log.info("Script execution failed.");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("FAILED\n");
                writer.write(parseFailure(returnOutput).toString());
            } catch (IOException e) {
                log.error("Error writing to file: {}", outputFilePath, e);
            }
            Operations operations = new Operations();
            operations.setDateOfCreation(new Date());
            operations.setUserAccount("Админ");
            operations.setDescription("Неудачная попытка создать расписание из заранее заготовленного конфига");
            operationsRepository.save(operations);

            return Pair.of(returnOutput, false);
        } else if ("SUCCESSFULLY".equals(firstLine)) {
            log.info("Script executed successfully.");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("SUCCESSFULLY");
            } catch (IOException e) {
                log.error("Error writing to file: {}", outputFilePath, e);
            }
            List<String> list = List.of(returnOutput.split("\n"));
            potentialTimetableService.saveNewPotentialTimeTable(list);
            return Pair.of(returnOutput, true);
        } else {
            log.info("Script output does not start with 'FAILED' or 'SUCCESSFULLY'.");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("");
            } catch (IOException e) {
                log.error("Error writing to file: {}", outputFilePath, e);
            }
        }
        return Pair.of(returnOutput, true);
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
        boolean changeResult = potentialTimetableService.changeDayAndPairNumberAndRoom(changeDayAndPairNumberRequest);
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
}
