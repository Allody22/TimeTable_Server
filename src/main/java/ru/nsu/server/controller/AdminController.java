package ru.nsu.server.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.nsu.server.model.dto.TimetableLogsDTO;
import ru.nsu.server.payload.requests.*;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.OperationService;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;
import ru.nsu.server.services.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/admin")
@Tag(name = "02. Admin controller", description = "Админский контроллер, в котором человек может создавать " +
        "и удалять группы, учителя, комнаты, учебный план и тп.")
public class AdminController {

    private final UserService userService;

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;
    private final OperationService operationService;

    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public AdminController(
            UserService userService,
            RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService,
            SimpMessagingTemplate simpMessagingTemplate, OperationService operationService) {
        this.roomGroupTeacherSubjectPlanService = roomGroupTeacherSubjectPlanService;
        this.userService = userService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.operationService = operationService;
    }

    @Operation(
            summary = "Получение всех логов потенциального расписания.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = TimetableLogsDTO[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/get_logs/potential")
    @Transactional
    public ResponseEntity<?> getAllPotentialLogs() {
        return ResponseEntity.ok(operationService.getAllPotentialTimetableLogs());
    }

    @Operation(
            summary = "Получение всех логов актуального расписания.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = TimetableLogsDTO[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/get_logs/actual")
    @Transactional
    public ResponseEntity<?> getAllActualLogs() {
        return ResponseEntity.ok(operationService.getAllActualTimetableLogs());
    }

    @Operation(
            summary = "Получение всех логов всех расписаний.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = TimetableLogsDTO[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @GetMapping("/get_logs/all")
    @Transactional
    public ResponseEntity<?> getAllTimetableLogs() {
        return ResponseEntity.ok(operationService.getAllTimetableLogs());
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Создание новой группы.",
            description = """
                    Проверяется название группы, факультет, курс и количество студентов, а потом это всё превращается в одну группу и сохраняется.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Группы с таким номером уже существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/create_group")
    @Transactional
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupRequest groupRequest) {
        String groupNumber = groupRequest.getGroupNumber();

        if (roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(groupNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая группа уже существует."));
        }
        String description = roomGroupTeacherSubjectPlanService.saveNewGroup(groupNumber, groupRequest.getFaculty(), groupRequest.getCourse(), groupRequest.getStudentsNumber());
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("Группа " + groupNumber + " успешно сохранена"));
    }

    @Operation(
            summary = "Удаление группы из бд.",
            description = """
                    Проверяется, есть ли группа с таким номером, и удаляется из БД, если нет проблем.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Группы с таким номером не существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/delete_group/{group}")
    @Transactional
    public ResponseEntity<?> deleteGroup(@Parameter(description = "Номер группы, которую мы хотим удалить", example = "21215") @PathVariable("group") @Valid @NotBlank String group) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(group)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такой группа не существует."));
        }
        String description = roomGroupTeacherSubjectPlanService.deleteGroupByNumber(group);
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("Группа " + group + " успешно удалена"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")

    @Operation(
            summary = "Добавление предмета в список допустимых предметов.",
            description = """
                    Передаётся название предмета с его количеством пар в неделю, и, если такой предмет уже не существует,
                     то этот предмет успешно добавляется в бд.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Предмет с таким названием уже существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/create_subject")
    @Transactional
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectRequest subjectRequest) {
        String subjectName = subjectRequest.getName();
        if (roomGroupTeacherSubjectPlanService.ifExistBySubjectName(subjectName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такой предмет уже существует."));
        }
        String description = roomGroupTeacherSubjectPlanService.saveNewSubject(subjectName, subjectRequest.getTimesInAWeek());
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("Предмет " + subjectName + " успешно сохранен"));
    }


    @Operation(
            summary = "Удаление группы из бд.",
            description = """
                    Проверяется, есть ли группа с таким номером, и удаляется из БД, если нет проблем.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Группы с таким номером не существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/delete_subject/{subjectName}")
    @Transactional
    public ResponseEntity<?> deleteSubject(@Parameter(description = "Название предмета", example = "Оптимизации java") @PathVariable("subjectName") @Valid @NotBlank String subjectName) {
        if (!roomGroupTeacherSubjectPlanService.ifExistBySubjectName(subjectName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такого предмет не существует."));
        }
        String description = roomGroupTeacherSubjectPlanService.deleteSubject(subjectName);
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("Предмет " + subjectName + " успешно удален"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Добавление новой комнаты.",
            description = """
                    Передаётся номер комнаты, её предназначение (лекционная, терминальная и тп) и её вместимость.
                    Если такой комнаты еще не существует. то она успешно сохраняется в БД.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Предмет с таким названием уже существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/create_room")
    @Transactional
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        String roomName = roomRequest.getName();
        if (roomGroupTeacherSubjectPlanService.ifExistByRoomName(roomName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая комната уже существует."));
        }
        String description = roomGroupTeacherSubjectPlanService.saveNewRoom(roomName, roomRequest.getType(), roomRequest.getCapacity());
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("Комната " + roomName + " успешно сохранен"));
    }

    @Operation(
            summary = "Удаление комнаты.",
            description = """
                    Передаётся название комнаты и она удаляется.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Такой комнаты не существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/delete_room/{room}")
    @Transactional
    public ResponseEntity<?> deleteRoom(@Parameter(description = "Название предмета", example = "Оптимизации java") @PathVariable("room") @Valid @NotBlank String room) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByRoomName(room)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такой комнаты не существует."));
        }
        String description = roomGroupTeacherSubjectPlanService.deleteRoom(room);
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("Комната " + room + " успешно удалена"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Создание учебного плана.",
            description = """
                    Связывается информация об учителе, предмете, комнате и тп, а потом это всё превращается в один учебный план.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Количество пар в неделю не должно превышать 42.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/create_plan")
    @Transactional
    public ResponseEntity<?> createPlan(@Valid @RequestBody PlanRequest planRequest) {
        if (planRequest.getTimesInAWeek() > 42) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Кол-во пар в неделю по" +
                    " плану должно быть меньше 42"));
        }
        String description = roomGroupTeacherSubjectPlanService.saveNewPlan(planRequest.getTeacher(), planRequest.getSubject(), planRequest.getSubjectType(),
                planRequest.getGroups(), planRequest.getTimesInAWeek());
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("План успешно сохранен"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Удаление учебного плана.",
            description = """
                    Передаётся известная айдишка учебного плана и, если такая айдишка существует, то этот план удаляется.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Учёбного плана с таким айди не существуют.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @DeleteMapping("/delete_plan/{id}")
    @Transactional
    public ResponseEntity<?> deletePlan(@Parameter(description = "Айди учебного плана на отдельный предмет", example = "1") @PathVariable("id") @Valid @NotBlank Long id) {
        if (!roomGroupTeacherSubjectPlanService.ifExistPlanById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Плана с айди " + id + " не существует."));
        }
        String description = roomGroupTeacherSubjectPlanService.deletePlanById(id);
        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("План успешно удалён."));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Регистрация нового аккаунта студента в системе.",
            description = """
                    Создание нового аккаунта студента, привязанного к переданной почте и ФИО с телефоном.
                    Сервер автоматически генерирует пароль для данного студента.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Возвращается информация о пароле, который автоматически привязался к сгенерированному аккаунта", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Такая почта уже существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/register_student")
    @Transactional
    public ResponseEntity<?> registerNewStudent(@Valid @RequestBody RegistrationRequest registrationRequest) {
        String newUserEmail = registrationRequest.getEmail();

        if (newUserEmail != null && !newUserEmail.isEmpty()) {
            if (userService.existByEmailCheck(newUserEmail)) {
                return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая почта уже зарегистрирована."));
            }
        }
        String description = userService.saveNewUser(newUserEmail, registrationRequest.getFullName(),
                registrationRequest.getPhone());

        simpMessagingTemplate.convertAndSend(description);
        return ResponseEntity.ok(new MessageResponse("Новый пользователь успешно зарегистрирован."));
    }

    @Operation(
            summary = "Регистрация нового аккаунта учителя в системе.",
            description = """
                    Создание нового аккаунта учителя с правами учителя, привязанного к переданной почте и ФИО с телефоном.
                    Сервер автоматически генерирует пароль для данного аккаунта.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Возвращается информация о пароле, который автоматически привязался к сгенерированному аккаунта", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Такая почта уже зарегистрированного в системе.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/register_teacher")
    @Transactional
    public ResponseEntity<?> registerNewTeacher(@Valid @RequestBody RegistrationRequest registrationRequest) {
        String newUserEmail = registrationRequest.getEmail();

        if (newUserEmail != null && !newUserEmail.isEmpty()) {
            if (userService.existByEmailCheck(newUserEmail)) {
                return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая почта уже зарегистрирована."));
            }
        }
        String description = userService.saveNewTeacher(newUserEmail, registrationRequest.getFullName(),
                registrationRequest.getPhone());

        simpMessagingTemplate.convertAndSend(description);

        return ResponseEntity.ok(new MessageResponse("Новый учитель успешно зарегистрирован."));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Регистрация нового аккаунта администратора в системе.",
            description = """
                    Создание нового аккаунта администратора с правами админа, привязанного к переданной почте и ФИО с телефоном.
                    Сервер автоматически генерирует пароль для данного аккаунта.
                    Человек с правами админа может создавать группы, добавлять людей, создавать расписание и активировать, то есть по факту доступны все действия в этом контроллере.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Возвращается информация о пароле, который автоматически привязался к сгенерированному аккаунта", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Такая почта уже зарегистрированного в системе.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/register_admin")
    @Transactional
    public ResponseEntity<?> registerNewAdmin(@Valid @RequestBody RegistrationRequest registrationRequest) {
        String newUserEmail = registrationRequest.getEmail();

        if (newUserEmail != null && !newUserEmail.isEmpty()) {
            if (userService.existByEmailCheck(newUserEmail)) {
                return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая почта уже зарегистрирована."));
            }
        }
        String description = userService.saveNewAdmin(newUserEmail, registrationRequest.getFullName(),
                registrationRequest.getPhone());

        simpMessagingTemplate.convertAndSend(description);

        return ResponseEntity.ok(new MessageResponse("Новый администратор успешно зарегистрирован."));
    }

    @Operation(
            summary = "Смена ролей у пользователя в БД.",
            description = """
                    Смена ролей у аккаунта определённого пользователя.
                    Можно как убрать так и добавить роли.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Возвращается информация о пароле, который автоматически привязался к сгенерированному аккаунта", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Такая почта уже зарегистрированного в системе.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/change_roles")
    @Transactional
    public ResponseEntity<?> changeUserRoles(@Valid @RequestBody ChangeUserRolesRequest changeUserRolesRequest) {
        String description = userService.changeUserRoles(changeUserRolesRequest.getEmail(), changeUserRolesRequest.getRoles());

        simpMessagingTemplate.convertAndSend(description);

        return ResponseEntity.ok(new MessageResponse("Новый администратор успешно зарегистрирован."));
    }
}
