package ru.nsu.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.services.ConstraintService;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/get")
public class GetterController {

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;

    private final ConstraintService constraintService;

    private final OperationsRepository operationsRepository;

    @Autowired
    public GetterController(
            RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService, ConstraintService constraintService,
            OperationsRepository operationsRepository) {
        this.roomGroupTeacherSubjectPlanService = roomGroupTeacherSubjectPlanService;
        this.constraintService = constraintService;
        this.operationsRepository =operationsRepository;
    }

    @GetMapping("/all_operations")
    @Transactional
    public ResponseEntity<?> getAllOperations() {
        return ResponseEntity.ok(operationsRepository.findAll());
    }

    @GetMapping("/all_groups")
    @Transactional
    public ResponseEntity<?> getAllGroups() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllGroups());
    }

    @GetMapping("/all_ru_constraints")
    @Transactional
    public ResponseEntity<?> getAllRuConstraints() {
        return ResponseEntity.ok(constraintService.getAllConstraintsRu());
    }

    @GetMapping("/all_eng_constraints")
    @Transactional
    public ResponseEntity<?> getAllEngConstraints() {
        return ResponseEntity.ok(constraintService.getAllConstraintsEng());
    }

    @GetMapping("/faculty_groups/{faculty}")
    @Transactional
    public ResponseEntity<?> getAllGroupsOfFaculty(@PathVariable @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllGroupsByFaculty(faculty));
    }

    @GetMapping("/all_subjects")
    @Transactional
    public ResponseEntity<?> getAllSubjects() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllSubjects());
    }

    @GetMapping("/all_plan")
    @Transactional
    public ResponseEntity<?> getAllPlan() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllPlan());
    }

    @GetMapping("/all_teachers")
    @Transactional
    public ResponseEntity<?> getAllTeachers() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllTeachers());
    }

    @GetMapping("/all_rooms")
    @Transactional
    public ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomGroupTeacherSubjectPlanService.getAllRooms());
    }

}
