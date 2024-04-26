package ru.nsu.server.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.nsu.server.payload.requests.ChangeDayAndPairNumberAndRoomRequest;
import ru.nsu.server.payload.requests.ChangeDayAndPairNumberRequest;
import ru.nsu.server.payload.requests.ChangeRoomRequest;
import ru.nsu.server.payload.requests.ChangeTeacherRequest;
import ru.nsu.server.payload.response.DataResponse;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/timetable/actual/change")
@Tag(name = "06. Timetable Changing controller", description = "Контроллер для попыток внесения изменений в актуальное расписание " +
        "без вызова алгоритма и без учёта ограничений (constraints).")
public class TimetableChangingController {

    private final TimetableService timetableService;

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
        boolean changeResult = timetableService.changeDayAndPairNumber(changeDayAndPairNumberRequest);
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
        boolean changeResult = timetableService.changeRoom(changeRoomRequest);
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
        boolean changeResult = timetableService.changeDayAndPairNumberAndRoom(changeDayAndPairNumberRequest);
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
        boolean changeResult = timetableService.changeTeacher(changeTeacherRequest);
        return ResponseEntity.ok(new DataResponse(changeResult));
    }
}
