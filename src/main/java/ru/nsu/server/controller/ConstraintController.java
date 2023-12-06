package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.exception.NotInDataBaseException;
import ru.nsu.server.payload.requests.ConstraintRequest;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.ConstraintService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
        constraintService.saveNewUniversalConstraint(constraintNameRu, constraintNameEng, constraintRequest.getGroup(), constraintRequest.getGroup1(),
                constraintRequest.getGroup2(), constraintRequest.getTeacher(), constraintRequest.getTeacher1(),
                constraintRequest.getTeacher2(), constraintRequest.getDay(), constraintRequest.getPeriod(),
                constraintRequest.getNumber());
        return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
    }

    @DeleteMapping("/delete_constraint//{id}")
    @Transactional
    public ResponseEntity<?> deleteConstraint(@PathVariable @Valid @NotBlank Long id) {
        constraintService.deleteUniversalConstraint(id);
        return ResponseEntity.ok(new MessageResponse("Ограничение успешно удалено"));
    }
}
