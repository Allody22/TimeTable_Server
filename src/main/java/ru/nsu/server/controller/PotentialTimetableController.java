package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/timetable/potential")
public class PotentialTimetableController {

    private final TimetableService timetableService;

    private final OperationsRepository operationsRepository;

    @Autowired
    public PotentialTimetableController(
            TimetableService timetableService,
            OperationsRepository operationsRepository) {
        this.timetableService = timetableService;
        this.operationsRepository = operationsRepository;
    }

    @Transactional
    @PostMapping("/create_timetable_db")
    public ResponseEntity<?> fillFile() {
        try {
            timetableService.saveConfigToFile();
            var output = executeScript();
            if (!output.getRight()) {
                var failureResponse = parseFailure(output.getLeft());
                Operations operations = new Operations();
                operations.setDateOfCreation(new Date());
                operations.setUserAccount("Админ");
                operations.setDescription("Неудачная попытка создать расписание");
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

    @PostMapping("/create_timetable_kolya")
    @Transactional
    public ResponseEntity<?> createTimeTable() {
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
        return ResponseEntity.ok(timetableService.getPotentialGroupTimetable(group));
    }

    @GetMapping("/teacher/{teacher}")
    @Transactional
    public ResponseEntity<?> getPotentialTeacherTimetable(@PathVariable @Valid @NotBlank String teacher) {
        return ResponseEntity.ok(timetableService.getPotentialTeacherTimetable(teacher));
    }

    @GetMapping("/room/{room}")
    @Transactional
    public ResponseEntity<?> getPotentialRoomTimetable(@PathVariable @Valid @NotBlank String room) {
        return ResponseEntity.ok(timetableService.getPotentialRoomTimetable(room));
    }

    @GetMapping("/faculty/{faculty}")
    @Transactional
    public ResponseEntity<?> getPotentialFacultyTimetable(@PathVariable @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(timetableService.getPotentialFacultyTimetable(faculty));
    }

    public Pair<String, Boolean> executeScript() throws IOException, InterruptedException {

        String baseDir = System.getProperty("user.dir");

        String pythonExecutablePath = baseDir + "/Algo/venv/Scripts/python.exe";
        String pythonScriptPath = baseDir + "/Algo/algo.py";

        String jsonFilePath = baseDir + "/Algo/my_config_example.json";
        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath, pythonScriptPath, jsonFilePath);

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        String output = new BufferedReader(new InputStreamReader(process.getInputStream(), "windows-1251"))
                .lines().collect(Collectors.joining("\n"));

        log.info(output);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }
        String firstLine = output.split("\n")[0];
        String returnOutput = output.substring(output.indexOf('\n') + 1);

        // Проверяем, соответствует ли первая строка "FAILED" или "SUCCESSFULLY"
        if ("FAILED".equals(firstLine)) {
            // Обработка случая FAILED
            log.info("Script execution failed.");
            return Pair.of(returnOutput, false);
        } else if ("SUCCESSFULLY".equals(firstLine)) {
            // Обработка случая SUCCESSFULLY
            log.info("Script executed successfully.");
            return Pair.of(returnOutput, true);
        } else {
            // Обработка других случаев
            log.info("Script output does not start with 'FAILED' or 'SUCCESSFULLY'.");
        }


        return Pair.of(returnOutput, true);
    }

    public Pair<String, Boolean> executeScriptKolya() throws IOException, InterruptedException {

        String baseDir = System.getProperty("user.dir");

        String pythonScriptPath = baseDir + "/Algo/algo.py";
        String jsonFilePath = baseDir + "/Algo/config_example.json";

        String pythonExecutablePath = baseDir + "/Algo/venv/Scripts/python.exe";

        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath, pythonScriptPath, jsonFilePath);

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        String output = new BufferedReader(new InputStreamReader(process.getInputStream(), "windows-1251"))
                .lines().collect(Collectors.joining("\n"));

        log.info(output);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }

        String firstLine = output.split("\n")[0];
        String returnOutput = output.substring(output.indexOf('\n') + 1);

        // Проверяем, соответствует ли первая строка "FAILED" или "SUCCESSFULLY"
        if ("FAILED".equals(firstLine)) {
            // Обработка случая FAILED
            log.info("Script execution failed.");
            return Pair.of(returnOutput, false);
        } else if ("SUCCESSFULLY".equals(firstLine)) {
            // Обработка случая SUCCESSFULLY
            log.info("Script executed successfully.");
            return Pair.of(returnOutput, true);
        } else {
            // Обработка других случаев
            log.info("Script output does not start with 'FAILED' or 'SUCCESSFULLY'.");
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
                // Неизвестный ключ, возможно стоит логировать или обрабатывать по-другому
                break;
        }
    }

}
