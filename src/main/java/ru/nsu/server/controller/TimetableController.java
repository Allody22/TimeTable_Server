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
import ru.nsu.server.services.GroupService;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/get/timetable")
public class TimetableController {

    private final TimetableService timetableService;

    private final GroupService groupService;

    @Autowired
    public TimetableController(
            TimetableService timetableService, GroupService groupService) {
        this.groupService = groupService;
        this.timetableService = timetableService;
    }

    @GetMapping("/all_groups")
    @Transactional
    public ResponseEntity<?> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
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
        return ResponseEntity.ok(timetableService.getAllRooms());
    }

    @GetMapping("/group/{group}")
    @Transactional
    public ResponseEntity<?> getGroupTimetable(@PathVariable @Valid @NotBlank String group) {
        return ResponseEntity.ok(timetableService.getGroupTimetable(group));
    }

    @GetMapping("/teacher/{teacher}")
    @Transactional
    public ResponseEntity<?> getTeacherTimetable(@PathVariable @Valid @NotBlank String teacher) {
        return ResponseEntity.ok(timetableService.getTeacherTimetable(teacher));
    }

    @GetMapping("/room/{room}")
    @Transactional
    public ResponseEntity<?> getRoomTimetable(@PathVariable @Valid @NotBlank String room) {
        return ResponseEntity.ok(timetableService.getRoomTimetable(room));
    }

    @GetMapping("/faculty/{faculty}")
    @Transactional
    public ResponseEntity<?> getFacultyTimetable(@PathVariable @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(timetableService.getFacultyTimetable(faculty));
    }

}
