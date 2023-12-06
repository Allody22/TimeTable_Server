package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/timetable/potential")
public class PotentialTimetableController {

    private final TimetableService timetableService;

    @Autowired
    public PotentialTimetableController(
            TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @Transactional
    @PostMapping("/create_timetable_db")
    public ResponseEntity<?> fillFile() {
        try {
            timetableService.saveConfigToFile();
            String output = executeScript();

            List<String> list = List.of(output.split("\n"));
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
            String output = executeScriptKolya();

            List<String> list = List.of(output.split("\n"));
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

    public String executeScript() throws IOException, InterruptedException {

        String baseDir = System.getProperty("user.dir");

        String pythonExecutablePath = baseDir + "/Algo/venv/Scripts/python.exe";
        String pythonScriptPath = baseDir + "/Algo/algo.py";

        String jsonFilePath = baseDir + "/Algo/my_config_example.json";
        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath, pythonScriptPath, jsonFilePath);

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        log.info(output);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }

        return output;
    }

    public String executeScriptKolya() throws IOException, InterruptedException {

        String baseDir = System.getProperty("user.dir");

        String pythonExecutablePath = baseDir + "/Algo/venv/Scripts/python.exe";
        String pythonScriptPath = baseDir + "/Algo/algo.py";

        String jsonFilePath = baseDir + "/Algo/config_example.json";

        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath, pythonScriptPath, jsonFilePath);

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        log.info(output);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }

        return output;
    }

}
