package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.model.Operations;
import ru.nsu.server.payload.response.FailureResponse;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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
@RequestMapping("/timetable/potential")
public class PotentialTimetableController {

    private final TimetableService timetableService;

    private final OperationsRepository operationsRepository;

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;

//    private final String pythonExecutableURL = "/Algo/venv/Scripts/python.exe";
//    private final String pythonAlgoURL = "/Algo/src/app/algo.py";
//    private final  String outputFileURL = "/Algo/algorithm_output.txt";


    private final String pythonExecutableURL = "/Algo/venv/Scripts/python.exe";
    private final String pythonAlgoURL = "/Algo/algo.py";
    private final String outputFileURL = "/Algo/algorithm_output.txt";


    @Autowired
    public PotentialTimetableController(TimetableService timetableService, RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService, OperationsRepository operationsRepository) {
        this.timetableService = timetableService;
        this.roomGroupTeacherSubjectPlanService = roomGroupTeacherSubjectPlanService;
        this.operationsRepository = operationsRepository;
    }

    @PostMapping("/activate")
    @Transactional
    public ResponseEntity<?> makePotentialActual() {
        timetableService.convertOptionalTimeTableToActual();
        return ResponseEntity.ok(new MessageResponse("Теперь потенциальное расписание, полученное последний раз с помощью генерации алгоритма стало актуальным!"));
    }

    @GetMapping("/all")
    @Transactional
    public ResponseEntity<?> getAllPotential() {
        return ResponseEntity.ok(timetableService.getAllPotentialTimeTable());
    }

    @GetMapping("/group/{group}")
    @Transactional
    public ResponseEntity<?> getPotentialGroupTimetable(@PathVariable @Valid @NotBlank String group) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(group)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой группы не существует.")));
        }
        return ResponseEntity.ok(timetableService.getPotentialGroupTimetable(group));
    }

    @GetMapping("/teacher/{teacher}")
    @Transactional
    public ResponseEntity<?> getPotentialTeacherTimetable(@PathVariable @Valid @NotBlank String teacher) {
        if (!roomGroupTeacherSubjectPlanService.ifExistTeacherByFullName(teacher)){
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такого преподавателя не существует.")));
        }
        return ResponseEntity.ok(timetableService.getPotentialTeacherTimetable(teacher));
    }

    @GetMapping("/room/{room}")
    @Transactional
    public ResponseEntity<?> getPotentialRoomTimetable(@PathVariable @Valid @NotBlank String room) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByRoomName(room)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой комнаты не существует.")));
        }
        return ResponseEntity.ok(timetableService.getPotentialRoomTimetable(room));
    }

    @GetMapping("/faculty/{faculty}")
    @Transactional
    public ResponseEntity<?> getPotentialFacultyTimetable(@PathVariable @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(timetableService.getPotentialFacultyTimetable(faculty));
    }

    @Transactional
    @PostMapping("/create_timetable_db")
    public ResponseEntity<?> createTimeTableFromDB() {
        try {
            var output = executeScript();
            if (!output.getRight()) {
                var failureResponse = parseFailure(output.getLeft());
                return ResponseEntity.badRequest().body((failureResponse));
            }
            return ResponseEntity.ok(new MessageResponse("Новое потенциальное расписание сделано"));
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Произошла ошибка: " + e.getMessage()));
        }
    }

    @PostMapping("/create_timetable_kolya")
    @Transactional
    public ResponseEntity<?> createTimeTableKolya() {
        try {
            var output = executeScriptKolya();
            if (!output.getRight()) {
                var failureResponse = parseFailure(output.getLeft());

                Operations operations = new Operations();
                operations.setDateOfCreation(new Date());
                operations.setUserAccount("Админ");
                operations.setDescription("Неудачная попытка создать расписание из заранее заготовленного конфига");
                operationsRepository.save(operations);
                return ResponseEntity.badRequest().body((failureResponse));
            }
            List<String> list = List.of(output.getLeft().split("\n"));
            timetableService.saveNewPotentialTimeTable(list);
            return ResponseEntity.ok(new MessageResponse("Новое потенциальное расписание сделано"));
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Произошла ошибка: " + e.getMessage()));
        }
    }

    @Transactional
    @PostMapping("/create_timetable_db_async")
    public ResponseEntity<?> createTimeTableFromDBAsync() {
        CompletableFuture<Pair<String, Boolean>> future = executeScriptDBAsync();

        try {
            Pair<String, Boolean> result = future.get(5, TimeUnit.SECONDS);
            if (result.getRight()) {
                return ResponseEntity.ok(new MessageResponse("Потенциальное расписание успешно создано"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Ошибка при создании расписания: " + result.getLeft()));
            }
        } catch (TimeoutException e) {
            return ResponseEntity.ok(new MessageResponse("Расписание еще составляется, пожалуйста, подождите"));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Ошибка выполнения: " + e.getMessage()));
        }
    }

    @PostMapping("/create_timetable_kolya_async")
    @Transactional
    public ResponseEntity<?> createTimeTableKolyaAsync() {
        CompletableFuture<Pair<String, Boolean>> future = executeScriptKolyaAsync();

        try {
            Pair<String, Boolean> result = future.get(5, TimeUnit.SECONDS);
            if (result.getRight()) {
                return ResponseEntity.ok(new MessageResponse("Потенциальное расписание успешно создано"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Ошибка при создании расписания: " + result.getLeft()));
            }
        } catch (TimeoutException e) {
            return ResponseEntity.ok(new MessageResponse("Расписание еще составляется, пожалуйста, подождите"));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Ошибка выполнения: " + e.getMessage()));
        }
    }

    @GetMapping("/check_file")
    @Transactional
    public ResponseEntity<?> checkFile() {
        String baseDir = System.getProperty("user.dir");
        String outputFilePath = baseDir + "/Algo/algorithm_output.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFilePath))) {
            String firstLine = reader.readLine();
            if ("FAILED".equals(firstLine)) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                String data = builder.toString();
                String jsonResponse = timetableService.convertFailureToJSON(data);
                return ResponseEntity.badRequest().body((jsonResponse));

            } else if ("SUCCESSFULLY".equals(firstLine)) {
                return ResponseEntity.ok(new MessageResponse("Расписание успешно составлено и есть в потенциальном"));

            } else if ("EMPTY".equals(firstLine)) {
                return ResponseEntity.ok(new MessageResponse("Алгоритм для запуска расписания еще никогда не запускался"));

            } else {
                return ResponseEntity.ok(new MessageResponse("Расписание всё еще составляется"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Pair<String, Boolean> executeScript() throws IOException, InterruptedException {
        timetableService.saveConfigToFile();

        String baseDir = System.getProperty("user.dir");
        String pythonExecutablePath = baseDir + pythonExecutableURL;
        String pythonScriptPath = baseDir + pythonAlgoURL;
        String outputFilePath = baseDir + outputFileURL;
        String inputURL = "/Algo/my_config_example.json";
        String jsonFilePath = baseDir + inputURL;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
            writer.write("");
        } catch (IOException e) {
            log.error("Error writing to file: " + outputFilePath, e);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath, pythonScriptPath, jsonFilePath);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        String output = new BufferedReader(new InputStreamReader(process.getInputStream(), "windows-1251")).lines().collect(Collectors.joining("\n"));
        log.info(output);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }
        String firstLine = output.split("\n")[0];
        String returnOutput = output.substring(output.indexOf('\n') + 1);

        if ("FAILED".equals(firstLine)) {
            log.info("Script execution failed.");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("FAILED\n");
                writer.write(parseFailure(returnOutput).toString());
            } catch (IOException e) {
                log.error("Error writing to file: " + outputFilePath, e);
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
                log.error("Error writing to file: " + outputFilePath, e);
            }
            List<String> list = List.of(returnOutput.split("\n"));
            timetableService.saveNewPotentialTimeTable(list);
            return Pair.of(returnOutput, true);
        } else {
            log.info("Script output does not start with 'FAILED' or 'SUCCESSFULLY'.");
        }
        return Pair.of(returnOutput, true);
    }

    public Pair<String, Boolean> executeScriptKolya() throws IOException, InterruptedException {
        String baseDir = System.getProperty("user.dir");
        String pythonScriptPath = baseDir + pythonAlgoURL;
        String jsonFilePath = baseDir + "/Algo/src/resources/config_example.json";
        String pythonExecutablePath = baseDir + pythonExecutableURL;
        String outputFilePath = baseDir + outputFileURL;
        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath, pythonScriptPath, jsonFilePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
            writer.write("");
        } catch (IOException e) {
            log.error("Error writing to file: " + outputFilePath, e);
        }

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        String output = new BufferedReader(new InputStreamReader(process.getInputStream(), "windows-1251")).lines().collect(Collectors.joining("\n"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
            writer.write(output);
        } catch (IOException e) {
            log.error("Error writing to file: " + outputFilePath, e);
        }
        log.info(output);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }

        String firstLine = output.split("\n")[0];
        String returnOutput = output.substring(output.indexOf('\n') + 1);

        if ("FAILED".equals(firstLine)) {
            log.info("Script execution failed.");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("FAILED\n");
                writer.write(parseFailure(returnOutput).toString());
            } catch (IOException e) {
                log.error("Error writing to file: " + outputFilePath, e);
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
                log.error("Error writing to file: " + outputFilePath, e);
            }
            List<String> list = List.of(returnOutput.split("\n"));
            timetableService.saveNewPotentialTimeTable(list);

            return Pair.of(returnOutput, true);

        } else {
            log.info("Script output does not start with 'FAILED' or 'SUCCESSFULLY'.");
        }

        return Pair.of(returnOutput, true);
    }

    @Async("taskExecutor")
    public CompletableFuture<Pair<String, Boolean>> executeScriptDBAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeScript();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Async("taskExecutor")
    public CompletableFuture<Pair<String, Boolean>> executeScriptKolyaAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeScriptKolya();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
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

}
