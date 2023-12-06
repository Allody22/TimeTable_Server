package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.exception.NotInDataBaseException;
import ru.nsu.server.payload.requests.ConstraintRequest;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.ConstraintService;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/constraints")
public class ConstraintController {

    private final ConstraintService constraintService;

    @Autowired
    public ConstraintController(
            ConstraintService constraintService) {
        this.constraintService = constraintService;
    }

    @GetMapping("/get_all")
    @Transactional
    public ResponseEntity<?> getAllConstraints() {
        return ResponseEntity.ok(constraintService.getAllConstraints());
    }


    @PostMapping("/create_constraint")
    @Transactional
    public ResponseEntity<?> createConstraint(@Valid @RequestBody ConstraintRequest constraintRequest) {
        String constraintNameRu = constraintRequest.getConstraintNameRu();
        String constraintNameEng = (constraintService.findConstraintByRuName(constraintNameRu)
                .orElseThrow(() -> new NotInDataBaseException("Такого ограничения не существует или оно еще не поддерживается: " + constraintNameRu))).getName();
        switch (constraintNameEng) {
            case "number_of_teaching_days" -> {
                constraintService.saveNewNumberOfTeachingDays(constraintRequest.getTeacher(),
                        constraintRequest.getNumber());
                return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
            }
            case "forbidden_period_for_teacher" -> {
                constraintService.saveNewForbiddenPeriodForTeacher(constraintRequest.getDay(), constraintRequest.getTeacher(), constraintRequest.getPeriod());
                return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
            }
            case "forbidden_period_for_group" -> {
                constraintService.saveNewForbiddenPeriodForGroup(constraintRequest.getDay(), constraintRequest.getGroup(), constraintRequest.getPeriod());
                return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
            }
            case "forbidden_day_for_teacher" -> {
                constraintService.saveNewForbiddenDayForTeachers(constraintRequest.getDay(), constraintRequest.getTeacher());
                return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
            }
            case "forbidden_day_for_group" -> {
                constraintService.saveNewForbiddenDayForGroups(constraintRequest.getDay(), constraintRequest.getGroup());
                return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
            }
            case "teachers_overlapping" -> {
                constraintService.saveNewTeachersOverlapping(constraintRequest.getTeacher1(), constraintRequest.getTeacher2());
                return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
            }
            case "groups_overlapping" -> {
                constraintService.saveNewGroupsOverlapping(constraintRequest.getGroup1(), constraintRequest.getGroup2());
                return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
            }
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Такого ограничения не существует " +
                "или оно еще не поддерживается."));
    }
}
