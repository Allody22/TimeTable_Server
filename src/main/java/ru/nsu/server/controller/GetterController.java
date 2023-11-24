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
import ru.nsu.server.services.ConstraintService;
import ru.nsu.server.services.GroupService;
import ru.nsu.server.services.RoomService;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/get")
public class GetterController {

    private final TimetableService timetableService;

    private final GroupService groupService;

    private final RoomService roomService;

    private final ConstraintService constraintService;

    @Autowired
    public GetterController(
            TimetableService timetableService, GroupService groupService,
            RoomService roomService, ConstraintService constraintService) {
        this.roomService = roomService;
        this.constraintService = constraintService;
        this.groupService = groupService;
        this.timetableService = timetableService;
    }

    @GetMapping("/all_groups")
    @Transactional
    public ResponseEntity<?> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
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
        return ResponseEntity.ok(groupService.getAllGroupsByFaculty(faculty));
    }

    @GetMapping("/all_subjects")
    @Transactional
    public ResponseEntity<?> getAllSubjects() {
        return ResponseEntity.ok(timetableService.getAllSubjects());
    }

    @GetMapping("/all_teachers")
    @Transactional
    public ResponseEntity<?> getAllTeachers() {
        return ResponseEntity.ok(timetableService.getAllTeachers());
    }

    @GetMapping("/all_rooms")
    @Transactional
    public ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

}
