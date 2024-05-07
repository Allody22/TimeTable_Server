package ru.nsu.server.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import ru.nsu.server.model.current.WeekTimetable;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/timetable/actual")
@Tag(name = "01. Actual Timetable controller", description = "Получение информации о последнем рабочем " +
        "и активированном расписании по отдельным категориям (группы, факультеты, преподаватели...)." +
        "Тут находится ПОСЛЕДНЕЕ активированное РАБОЧЕЕ потенциальное расписание.")
public class ActualTimetableController {

    private final TimetableService timetableService;

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;


    @Operation(
            summary = "Получение всего актуального расписания для всех факультетов, групп и тп.",
            description = """
                    Из базы данных достаётся вся сущность расписания из репозитория без фильтрации на группы, преподов, комнаты и тп.
                    Дальнейшая информация может фильтроваться на сайте или выводится для проверки всего расписания.""",
            tags = {"actual timetable", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all")
    @Transactional
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(timetableService.getAllTimeTable());
    }

    @Operation(
            summary = "Получение актуального расписания для определённой группы",
            description = """
                    Из базы данных достаётся вся сущность расписания из репозитория, фильтруясь по определённой группе.""",
            tags = {"actual timetable", "get", "group"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Передали группу которой не существует", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/group/{group}")
    @Transactional
    public ResponseEntity<?> getGroupTimetable(@Parameter(description = "Номер группы для которой мы ищем расписание", example = "21215") @PathVariable("group") @Valid @NotBlank String group) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(group)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой группы не существует.")));
        }
        return ResponseEntity.ok(timetableService.getGroupTimetable(group));
    }

    @Operation(
            summary = "Получение актуального расписания для определённого учителя.",
            description = """
                    Из базы данных достаётся вся сущность расписания из репозитория, фильтруясь по имени учителя.""",
            tags = {"actual timetable", "get", "teacher"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Передали учителя которого не существует", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/teacher/{teacher}")
    @Transactional
    public ResponseEntity<?> getTeacherTimetable(@Parameter(description = "Данные учителя для которого мы ищем расписание", example = "Богданов Михаил Сергеевич") @PathVariable("teacher") @Valid @NotBlank String teacher) {
        if (!roomGroupTeacherSubjectPlanService.ifExistTeacherByFullName(teacher)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такого преподавателя не существует.")));
        }
        return ResponseEntity.ok(timetableService.getTeacherTimetable(teacher));
    }

    @Operation(
            summary = "Получение актуального расписания для определённой комнаты.",
            description = """
                    Из базы данных достаётся вся сущность расписания из репозитория, фильтруясь по номера комнаты.""",
            tags = {"actual timetable", "get", "room"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Передали комнату, которой не существует", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/room/{room}")
    @Transactional
    public ResponseEntity<?> getRoomTimetable(@Parameter(description = "Номер комнаты для которого мы ищем расписание", example = "3228") @PathVariable("room") @Valid @NotBlank String room) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByRoomName(room)) {
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой комнаты не существует.")));
        }
        return ResponseEntity.ok(timetableService.getRoomTimetable(room));
    }

    @Operation(
            summary = "Получение актуального расписания для определённого факультета.",
            description = """
                    Из базы данных достаётся вся сущность расписания из репозитория, фильтруясь по названию факультета.""",
            tags = {"actual timetable", "get", "faculty"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = WeekTimetable[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/faculty/{faculty}")
    @Transactional
    public ResponseEntity<?> getFacultyTimetable(@Parameter(description = "Номер комнаты для которого мы ищем расписание", example = "3228") @PathVariable("faculty") @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(timetableService.getFacultyTimetable(faculty));
    }

}
