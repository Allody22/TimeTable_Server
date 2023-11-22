package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.configuration.security.jwt.JwtUtils;
import ru.nsu.server.payload.requests.GroupRequest;
import ru.nsu.server.payload.requests.RegistrationRequest;
import ru.nsu.server.payload.requests.RoomRequest;
import ru.nsu.server.payload.requests.SubjectRequest;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.GroupService;
import ru.nsu.server.services.RefreshTokenService;
import ru.nsu.server.services.TimetableService;
import ru.nsu.server.services.UserService;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    private final UserService userService;

    private final RefreshTokenService refreshTokenService;

    private final TimetableService timetableService;

    private final GroupService groupService;

    @Autowired
    public AdminController(
            AuthenticationManager authenticationManager,
            TimetableService timetableService,
            PasswordEncoder encoder,
            UserService userService,
            GroupService groupService,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.groupService = groupService;
        this.timetableService = timetableService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

//    @PreAuthorize("hasRole('ADMINISTRATOR')")
//    @PostMapping("/create_constraint")
//    @Transactional
//    public ResponseEntity<?> createGroup(@Valid @RequestBody ConstraintTimeRequest constraintTimeRequest) {
//
//        timetableService.saveNewGroup(groupNumber, groupRequest.getFaculty(), groupRequest.getCourse());
//        return ResponseEntity.ok(new MessageResponse("Группа " + groupNumber + " успешно сохранена"));
//    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_group")
    @Transactional
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupRequest groupRequest) {
        String groupNumber = groupRequest.getGroupNumber();

        if (groupService.ifExistByGroupNumber(groupNumber)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая группа уже существует."));
        }
        groupService.saveNewGroup(groupNumber, groupRequest.getFaculty(), groupRequest.getCourse(), groupRequest.getStudentsNumber());
        return ResponseEntity.ok(new MessageResponse("Группа " + groupNumber + " успешно сохранена"));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_subject")
    @Transactional
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectRequest subjectRequest) {
        String subjectName = subjectRequest.getName();
        if (timetableService.ifExistBySubjectName(subjectName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такой предмет уже существует."));
        }
        timetableService.saveNewSubject(subjectName);
        return ResponseEntity.ok(new MessageResponse("Предмет " + subjectName + " успешно сохранен"));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create_room")
    @Transactional
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        String roomName = roomRequest.getName();
        if (timetableService.ifExistByRoomName(roomName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такая комната уже существует."));
        }
        timetableService.saveNewRoom(roomName, roomRequest.getType(), roomRequest.getCapacity());
        return ResponseEntity.ok(new MessageResponse("Комната " + roomName + " успешно сохранен"));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
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

    @PreAuthorize("hasRole('ADMINISTRATOR')")
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

    @PreAuthorize("hasRole('ADMINISTRATOR')")
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
