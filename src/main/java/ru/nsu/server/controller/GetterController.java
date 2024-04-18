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
import ru.nsu.server.model.Group;
import ru.nsu.server.model.Operations;
import ru.nsu.server.model.Plan;
import ru.nsu.server.model.Room;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.services.ConstraintService;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/get")
@Tag(name = "4. Getter controller", description = "Контроллер для получения константной информации. " +
        "Тут можно получить поддерживаемые ограничения, существующие номера групп, имена учителей, названия факультетов и тп.")
public class GetterController {

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;

    private final ConstraintService constraintService;

    private final OperationsRepository operationsRepository;

    @Operation(
            summary = "Получение всех операций, которые когда-либо делали юзеры.",
            description = """
                    Получается информация о всех операциях (действиях) юзеров, которые до этого выполнялись и фиксировали в БД.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Operations[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_operations")
    @Transactional
    public ResponseEntity<?> getAllOperations() {
        return ResponseEntity.ok(operationsRepository.findAll());
    }

    @Operation(
            summary = "Получение всех существующих в БД групп.",
            description = """
                    Получается информация о всех группах, которые создавались и сохранялись в БД.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Group[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_groups")
    @Transactional
    public ResponseEntity<?> getAllGroups() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllGroups());
    }

    @Operation(
            summary = "Получение всех российских названий ограничений.",
            description = """
                    Получаются русские названий всех доступных и поддерживаемых ограничений.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = String[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_ru_constraints")
    @Transactional
    public ResponseEntity<?> getAllRuConstraints() {
        return ResponseEntity.ok(constraintService.getAllConstraintsRu());
    }

    @Operation(
            summary = "Получение всех английских названий ограничений.",
            description = """
                    Получаются английские названий всех доступных и поддерживаемых ограничений.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = String[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_eng_constraints")
    @Transactional
    public ResponseEntity<?> getAllEngConstraints() {
        return ResponseEntity.ok(constraintService.getAllConstraintsEng());
    }

    @Operation(
            summary = "Получение все номера групп на данном факультете.",
            description = """
                    Получаем список сущностей групп, привязанных к определённому факультету, переданному через параметры строки.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Group[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/faculty_groups/{faculty}")
    @Transactional
    public ResponseEntity<?> getAllGroupsOfFaculty(@Parameter(description = "Название факультета, группы которого нас интересуют", example = "ФИТ") @PathVariable("faculty") @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllGroupsByFaculty(faculty));
    }

    @Operation(
            summary = "Получение названия всех предметов, существующих в базе данных.",
            description = """
                    Просто залезаем в репозиторий базы данных и достаём названий всех предметов как список строк.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = String[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_subjects")
    @Transactional
    public ResponseEntity<?> getAllSubjects() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllSubjects());
    }

    @Operation(
            summary = "Получение существующего учебного плана.",
            description = """
                    Получается из сущностей учебного плана со всей информационной, привязанного к определённому плану,\s
                    то есть предмет, преподаватель, комната, количество пар в неделю и тп.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Plan[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_plan")
    @Transactional
    public ResponseEntity<?> getAllPlan() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllPlan());
    }

    @Operation(
            summary = "Получение всех зарегистрированных на сайте учителей.",
            description = """
                    Получаются строковые ФИО учителей, которые существуют на нашем сайте и которые могут преподавать определённый предмет.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = String[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_teachers")
    @Transactional
    public ResponseEntity<?> getAllTeachers() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllTeachers());
    }

    @Operation(
            summary = "Получение списка всех существующих комнат.",
            description = """
                    Получаются список из сущностей комнаты, где каждый элемент содержит информацию о номере комнаты, вместимости, 
                    предназначении (терминальная, лекционная и тп).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Room[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/all_rooms")
    @Transactional
    public ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllRooms());
    }

}
