package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.payload.requests.ConstraintTimeRequest;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.ConstraintService;
import ru.nsu.server.services.RefreshTokenService;
import ru.nsu.server.services.TimetableService;
import ru.nsu.server.services.UserService;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/constraints")
public class ConstraintController {

    private final UserService userService;

    private final RefreshTokenService refreshTokenService;

    private final TimetableService timetableService;

    private final ConstraintService constraintService;

    @Autowired
    public ConstraintController(
            ConstraintService constraintService,
            TimetableService timetableService,
            UserService userService,
            RefreshTokenService refreshTokenService) {
        this.constraintService = constraintService;
        this.timetableService = timetableService;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/create_constraint")
    @Transactional
    public ResponseEntity<?> createConstraint(@Valid @RequestBody ConstraintTimeRequest constraintTimeRequest) {
        String constraintName = constraintTimeRequest.getConstraintName();
        if (!constraintService.ifExistConstraintRu(constraintName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такого ограничения не существует " +
                    "или оно еще не поддерживается."));
        }
        constraintService.saveNewConstraint();
        return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
    }
}
