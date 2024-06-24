package ru.nsu.server.controller;


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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.nsu.server.model.actual.WeekTimetable;
import ru.nsu.server.model.user.Operations;
import ru.nsu.server.payload.response.FailureResponse;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.repository.logs.PotentialTimetableLogsRepository;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;
import ru.nsu.server.services.TestLoaderService;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.*;
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
@Tag(name = "04. Potential TimeTable controller", description = "Контроллер для составления потенциального расписания. " +
        "В нём человек может попробовать сгенерировать расписание, получить информацию о текущем статусе, " +
        "активировать последнее успешно сгенерированное потенциальное расписание..")
public class PotentialTimetableController {

    private final TimetableService timetableService;

    private final OperationsRepository operationsRepository;

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;

    private final PotentialTimetableLogsRepository potentialTimetableLogsRepository;
    private final TestLoaderService testLoaderService;

    @Value("${timetable.url.python.executable}")
    private String pythonExecutableURL;

    @Value("${timetable.url.python.algo.url}")
    private String pythonAlgoURL;

    @Value("${timetable.url.java.output}")
    private String outputFileURL;

    @Value("${timetable.url.java.test}")
    private String javaTestResources;

    @Value("${timetable.url.python.config}")
    private String pythonConfigUrl;

    private SimpMessagingTemplate simpMessagingTemplate;


    @Autowired
    public PotentialTimetableController(TimetableService timetableService, RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService,
                                        OperationsRepository operationsRepository, SimpMessagingTemplate simpMessagingTemplate, PotentialTimetableLogsRepository potentialTimetableLogsRepository, TestLoaderService testLoaderService) {
        this.timetableService = timetableService;
        this.roomGroupTeacherSubjectPlanService = roomGroupTeacherSubjectPlanService;
        this.operationsRepository = operationsRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.potentialTimetableLogsRepository = potentialTimetableLogsRepository;
        this.testLoaderService = testLoaderService;
    }

    @Operation(
            summary = "Активация расписание.",
            description = """
                    Вся информация о последнем успешно созданном потенциально расписании активируется и переносится в актуальное расписание,
                     при этом сохраняется информация о том какой человек это сделал и в какое время.""",
            tags = {"potential timetable", "activate"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/activate")
    @Transactional
    public ResponseEntity<?> makePotentialActual() {
        timetableService.convertOptionalTimeTableToActual();
        simpMessagingTemplate.convertAndSend("Теперь потенциальное расписание стало актуальным!");
        return ResponseEntity.ok(new MessageResponse("Теперь потенциальное расписание, полученное последний раз с помощью генерации алгоритма стало актуальным!"));
    }

    @Operation(
            summary = "Получение всех операций изменения, которые когда-либо происходили с потенциальном расписанием.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Operations[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/logs")
    @Transactional
    public ResponseEntity<?> getAllOperations() {
        return ResponseEntity.ok(potentialTimetableLogsRepository.findAllPotentialDto());
    }

    @Operation(
            summary = "Получение всего потенциального расписания для всех факультетов, групп и тп.",
            description = """
                    Из базы данных достаётся вся сущность потенциального расписания без фильтрации на группы, преподов, комнаты и тп.
                    Дальнейшая информация может фильтроваться на сайте или выводится для проверки всего расписания.""",
            tags = {"potential timetable", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all")
    @Transactional
    public ResponseEntity<?> getAllPotentialTimeTable() {
        return ResponseEntity.ok(timetableService.getAllPotentialTimeTable());
    }

    @Operation(
            summary = "Получение потенциального расписания для определённой группы",
            description = """
                    Из базы данных достаётся вся сущность потенциального расписания из репозитория, фильтруясь по определённой группе.""",
            tags = {"potential timetable", "get", "group"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Передали группу которой не существует", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/group/{group}")
    @Transactional
    public ResponseEntity<?> getPotentialGroupTimetable(@PathVariable @Valid @NotBlank String group) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(group)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой группы не существует.")));
        }
        return ResponseEntity.ok(timetableService.getPotentialGroupTimetable(group));
    }

    @Operation(
            summary = "Получение потенциального расписания для определённого учителя.",
            description = """
                    Из базы данных достаётся вся сущность потенциального расписания из репозитория, фильтруясь по имени учителя.""",
            tags = {"potential timetable", "get", "teacher"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Передали учителя которого не существует", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/teacher/{teacher}")
    @Transactional
    public ResponseEntity<?> getPotentialTeacherTimetable(@PathVariable @Valid @NotBlank String teacher) {
        if (!roomGroupTeacherSubjectPlanService.ifExistTeacherByFullName(teacher)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такого преподавателя не существует.")));
        }
        return ResponseEntity.ok(timetableService.getPotentialTeacherTimetable(teacher));
    }

    @Operation(
            summary = "Получение потенциального расписания для определённой комнаты.",
            description = """
                    Из базы данных достаётся вся сущность потенциального расписания из репозитория, фильтруясь по номера комнаты.""",
            tags = {"potential timetable", "get", "room"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Передали комнату, которой не существует", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/room/{room}")
    @Transactional
    public ResponseEntity<?> getPotentialRoomTimetable(@PathVariable @Valid @NotBlank String room) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByRoomName(room)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой комнаты не существует.")));
        }
        return ResponseEntity.ok(timetableService.getPotentialRoomTimetable(room));
    }

    @Operation(
            summary = "Получение потенциального расписания для определённого факультета.",
            description = """
                    Из базы данных достаётся вся сущность потенциального расписания из репозитория, фильтруясь по названию факультета.""",
            tags = {"potential timetable", "get", "faculty"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/faculty/{faculty}")
    @Transactional
    public ResponseEntity<?> getPotentialFacultyTimetable(@PathVariable @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(timetableService.getPotentialFacultyTimetable(faculty));
    }


    @Operation(
            summary = "Асинхронное создание нового потенциального расписания.",
            description = """
                    Составлении нового потенциального расписания из данных БД асинхронно.
                    Возможен вызов сразу нескольких составление подряд с помощью типа Future.
                    Если скрипт не может создать расписание из-за невозможности соответствия расписания условия или из-за другой ошибки,\s
                    то это расписание нигде не сохранится, а статус последней попытки создать расписания перейдёт в FAILED""",
            tags = {"potential timetable", "creation", "async"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Ошибка при составлении расписания, связанная с невозможностью выполнения условий", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Ошибка выполнения процесса составления расписания ", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")})})
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_timetable_db_async")
    public ResponseEntity<?> createTimeTableFromDBAsync() {
        CompletableFuture<Pair<String, Boolean>> future = executeScriptDBAsync();
        try {
            Pair<String, Boolean> result = future.get(5, TimeUnit.SECONDS);
            if (result.getRight()) {
                simpMessagingTemplate.convertAndSend("Новое потенциальное расписание успешно составлено");
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


    @Operation(
            summary = "Проверка текущего состояния расписания.",
            description = """
                    Проверяем, что сейчас находится в txt файле, который содержит текущую информации о статусе расписания.
                    Расписание может быть иметь такие статусы:
                    1) Алгоритм для составления расписания еще никогда не запускался
                    2) Минимальная ошибка при составлении расписания
                    3) Расписание успешно составлено
                    4) Расписание еще составляется""",
            tags = {"potential timetable", "status"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/check_file")
    @Transactional
    public ResponseEntity<?> checkFile() throws IOException {
        String baseDir = System.getProperty("user.dir");
        String outputFilePath = baseDir + outputFileURL;

        File outputFile = new File(outputFilePath);
        if (!outputFile.exists()) {
            boolean fileCreated = outputFile.createNewFile();
            if (!fileCreated) {
                throw new IOException("Could not create new file at: " + outputFilePath);
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFilePath))) {
            String firstLine = reader.readLine();
            //TODO минимальная ошибка при составлении расписания
            if (firstLine == null || firstLine.isBlank()) {
                return ResponseEntity.ok(new MessageResponse("Алгоритм для составления расписания еще никогда не запускался"));
            } else if (firstLine.equals("FAILED")) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                String data = builder.toString();
                String jsonResponse = timetableService.convertFailureToJSON(data);
                return ResponseEntity.badRequest().body((jsonResponse));
            } else if (firstLine.equals("SUCCESSFULLY")) {
                return ResponseEntity.ok(new MessageResponse("Расписание успешно составлено"));
                //TODO почему Empty было и откуда оно берётся
            } else if (firstLine.equals("WORKING")) {
                return ResponseEntity.ok(new MessageResponse("Расписание всё еще составляется"));
            } else {
                return ResponseEntity.ok(new MessageResponse("Неизвестное состояние"));
            }
        } catch (IOException e) {
            log.error("exception in checkFile {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Operation(
            summary = "Создание нового потенциального расписания.",
            description = """
                    Составлении нового потенциального расписания из данных БД.
                    Возможен вызов сразу нескольких составление подряд с помощью типа Future.
                    Если скрипт не может создать расписание из-за невозможности соответствия расписания условия или из-за другой ошибки,\s
                    то это расписание нигде не сохранится, а статус последней попытки создать расписания перейдёт в FAILED""",
            tags = {"potential timetable", "creation"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Ошибка при составлении расписания, связанная с невозможностью выполнения условий", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Ошибка выполнения процесса составления расписания ", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")})})
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_timetable_db")
    public ResponseEntity<?> createTimeTableFromDB() {
        try {
            var output = executeTimeTableScript(false);
            if (!output.getRight()) {
                var failureResponse = parseFailure(output.getLeft());
                return ResponseEntity.badRequest().body((failureResponse));
            }
            return ResponseEntity.ok(new MessageResponse("Новое потенциальное расписание сделано"));
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Произошла ошибка: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Создание нового потенциального расписания асинхронно из тестовых данных.",
            description = """
                    Составлении нового потенциального расписания из заранее внесённых тестовых данных джейсона.
                    Возможен вызов сразу нескольких составление подряд с помощью типа Future.
                    Если скрипт не может создать расписание из-за невозможности соответствия расписания условия или из-за другой ошибки,\s
                    то это расписание нигде не сохранится, а статус последней попытки создать расписания перейдёт в FAILED""",
            tags = {"potential timetable", "creation", "test"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Ошибка при составлении расписания, связанная с невозможностью выполнения условий", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Ошибка выполнения процесса составления расписания ", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")})})
    @PostMapping("/create_timetable_test_async")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> createTestTimeTableAsync() throws IOException {
        testLoaderService.initializeDatabaseFromTestData();

        CompletableFuture<Pair<String, Boolean>> future = executeScriptTestAsync();

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

    @Operation(
            summary = "Создание нового потенциального расписания из тестовых данных.",
            description = """
                    Составлении нового потенциального расписания из тестовых данных, заранее внесённых в специальный json файл .
                    Возможен вызов сразу нескольких составление подряд с помощью типа Future.
                    Если скрипт не может создать расписание из-за невозможности соответствия расписания условия или из-за другой ошибки,\s
                    то это расписание нигде не сохранится, а статус последней попытки создать расписания перейдёт в FAILED""",
            tags = {"potential timetable", "creation"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Ошибка при составлении расписания, связанная с невозможностью выполнения условий", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Ошибка выполнения процесса составления расписания ", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")})})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_timetable_test")
    @Transactional
    public ResponseEntity<?> createTestTimeTable() {
        try {
            testLoaderService.initializeDatabaseFromTestData();
            var output = executeTimeTableScript(true);
            if (!output.getRight()) {
                var failureResponse = parseFailure(output.getLeft());

                log.error("algo failure = {}", failureResponse);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Произошла ошибка: " + e.getMessage()));
        }
    }

    public Pair<String, Boolean> executeTimeTableScript(boolean isTest) throws IOException, InterruptedException {
        String baseDir = System.getProperty("user.dir");
        String jsonFilePath = baseDir + javaTestResources;
        String inputURL = pythonConfigUrl;
        if (!isTest) {
            timetableService.saveConfigToFile();
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("FAILED");
            } catch (IOException e) {
                log.error("Error writing to file: {}", outputFilePath, e);
            }
            throw new IOException("Script exited with error code: " + exitCode + " Output: " + output);
        }

        return parseAlgoResponse(outputFilePath, output);
    }


    @Async("taskExecutor")
    public CompletableFuture<Pair<String, Boolean>> executeScriptDBAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeTimeTableScript(false);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Async("taskExecutor")
    public CompletableFuture<Pair<String, Boolean>> executeScriptTestAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeTimeTableScript(true);
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

    private Pair<String, Boolean> parseAlgoResponse(String outputFilePath, String algoOutput) {
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
            timetableService.saveNewPotentialTimeTable(list);
            return Pair.of(returnOutput, true);
        } else {
            log.info("Script output does not start with 'FAILED' or 'SUCCESSFULLY'. It is = " + firstLine);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, Charset.forName("windows-1251")))) {
                writer.write("");
            } catch (IOException e) {
                log.error("Error writing to file: {}", outputFilePath, e);
            }
        }
        return Pair.of(returnOutput, true);
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
