package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.payload.requests.GroupRequest;
import ru.nsu.server.payload.requests.PlanRequest;
import ru.nsu.server.payload.requests.RegistrationRequest;
import ru.nsu.server.payload.requests.RoomRequest;
import ru.nsu.server.payload.requests.SubjectRequest;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;
import ru.nsu.server.services.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;

    @Autowired
    public AdminController(
            UserService userService,
            RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService) {
        this.roomGroupTeacherSubjectPlanService = roomGroupTeacherSubjectPlanService;
        this.userService = userService;
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_group")
    @Transactional
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupRequest groupRequest) {
        String groupNumber = groupRequest.getGroupNumber();

        if (roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(groupNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая группа уже существует."));
        }
        roomGroupTeacherSubjectPlanService.saveNewGroup(groupNumber, groupRequest.getFaculty(), groupRequest.getCourse(), groupRequest.getStudentsNumber());
        return ResponseEntity.ok(new MessageResponse("Группа " + groupNumber + " успешно сохранена"));
    }

    @PostMapping("/delete_group/{group}")
    @Transactional
    public ResponseEntity<?> deleteGroup(@PathVariable @Valid @NotBlank String group) {

        if (!roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(group)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такой группа не существует."));
        }
        roomGroupTeacherSubjectPlanService.deleteGroupByNumber(group);
        return ResponseEntity.ok(new MessageResponse("Группа " + group + " успешно удалена"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_subject")
    @Transactional
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectRequest subjectRequest) {
        String subjectName = subjectRequest.getName();
        if (roomGroupTeacherSubjectPlanService.ifExistBySubjectName(subjectName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такой предмет уже существует."));
        }
        roomGroupTeacherSubjectPlanService.saveNewSubject(subjectName, subjectRequest.getTimesInAWeek());
        return ResponseEntity.ok(new MessageResponse("Предмет " + subjectName + " успешно сохранен"));
    }

    @PostMapping("/delete_subject/{subjectName}")
    @Transactional
    public ResponseEntity<?> deleteSubject(@PathVariable @Valid @NotBlank String subjectName) {
        if (!roomGroupTeacherSubjectPlanService.ifExistBySubjectName(subjectName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такого предмет не существует."));
        }
        roomGroupTeacherSubjectPlanService.deleteSubject(subjectName);
        return ResponseEntity.ok(new MessageResponse("Предмет " + subjectName + " успешно удален"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_room")
    @Transactional
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        String roomName = roomRequest.getName();
        if (roomGroupTeacherSubjectPlanService.ifExistByRoomName(roomName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая комната уже существует."));
        }
        roomGroupTeacherSubjectPlanService.saveNewRoom(roomName, roomRequest.getType(), roomRequest.getCapacity());
        return ResponseEntity.ok(new MessageResponse("Комната " + roomName + " успешно сохранен"));
    }


    @PostMapping("/delete_room/{room}")
    @Transactional
    public ResponseEntity<?> deleteRoom(@PathVariable @Valid @NotBlank String room) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByRoomName(room)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такой комнаты не существует."));
        }
        roomGroupTeacherSubjectPlanService.deleteRoom(room);
        return ResponseEntity.ok(new MessageResponse("Комната " + room + " успешно удалена"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_plan")
    @Transactional
    public ResponseEntity<?> createPlan(@Valid @RequestBody PlanRequest planRequest) {
        roomGroupTeacherSubjectPlanService.saveNewPlan(planRequest.getTeacher(), planRequest.getSubject(), planRequest.getSubjectType(),
                planRequest.getGroups(), planRequest.getTimesInAWeek());
        return ResponseEntity.ok(new MessageResponse("План успешно сохранен"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete_plan/{id}")
    @Transactional
    public ResponseEntity<?> deletePlan(@PathVariable @Valid @NotBlank Long id) {
        if (!roomGroupTeacherSubjectPlanService.ifExistPlanById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Плана с айди " + id + " не существует."));
        }
        roomGroupTeacherSubjectPlanService.deletePlanById(id);
        return ResponseEntity.ok(new MessageResponse("План успешно сохранен"));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/register_student")
    @Transactional
    public ResponseEntity<?> registerNewStudent(@Valid @RequestBody RegistrationRequest registrationRequest) {

        String newUserEmail = registrationRequest.getEmail();

        if (userService.existByEmailCheck(newUserEmail)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая почта уже зарегистрирована."));
        }
        String newUserPassword = userService.saveNewUser(newUserEmail, registrationRequest.getFullName(),
                registrationRequest.getPhone());

        log.info("User password:" + newUserPassword);
        return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован с паролем:" + newUserPassword));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/register_teacher")
    @Transactional
    public ResponseEntity<?> registerNewTeacher(@Valid @RequestBody RegistrationRequest registrationRequest) {
        String newUserEmail = registrationRequest.getEmail();

        if (userService.existByEmailCheck(newUserEmail)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая почта уже зарегистрирована."));
        }
        String newUserPassword = userService.saveNewTeacher(newUserEmail, registrationRequest.getFullName(),
                registrationRequest.getPhone());

        log.info("teacher password:" + newUserPassword);
        return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован с паролем:" + newUserPassword));
    }

    //    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/register_admin")
    @Transactional
    public ResponseEntity<?> registerNewAdmin(@Valid @RequestBody RegistrationRequest registrationRequest) {
        String newUserEmail = registrationRequest.getEmail();

        if (userService.existByEmailCheck(newUserEmail)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая почта уже зарегистрирована."));
        }
        String newUserPassword = userService.saveNewAdmin(newUserEmail, registrationRequest.getFullName(),
                registrationRequest.getPhone());

        log.info("admin password:" + newUserPassword);
        return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован с паролем:" + newUserPassword));
    }
}
