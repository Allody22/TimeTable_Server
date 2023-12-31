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
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.RoomGroupTeacherSubjectPlanService;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/timetable/actual")
public class ActualTimetableController {

    private final TimetableService timetableService;

    private final RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService;

    @Autowired
    public ActualTimetableController(
            TimetableService timetableService,
            RoomGroupTeacherSubjectPlanService roomGroupTeacherSubjectPlanService) {
        this.timetableService = timetableService;
        this.roomGroupTeacherSubjectPlanService = roomGroupTeacherSubjectPlanService;
    }

    @GetMapping("/all")
    @Transactional
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(timetableService.getAllTimeTable());
    }

    @GetMapping("/group/{group}")
    @Transactional
    public ResponseEntity<?> getGroupTimetable(@PathVariable @Valid @NotBlank String group) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByGroupNumber(group)){
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой группы не существует.")));
        }
        return ResponseEntity.ok(timetableService.getGroupTimetable(group));
    }

    @GetMapping("/teacher/{teacher}")
    @Transactional
    public ResponseEntity<?> getTeacherTimetable(@PathVariable @Valid @NotBlank String teacher) {
        if (!roomGroupTeacherSubjectPlanService.ifExistTeacherByFullName(teacher)){
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такого преподавателя не существует.")));
        }
        return ResponseEntity.ok(timetableService.getTeacherTimetable(teacher));
    }

    @GetMapping("/room/{room}")
    @Transactional
    public ResponseEntity<?> getRoomTimetable(@PathVariable @Valid @NotBlank String room) {
        if (!roomGroupTeacherSubjectPlanService.ifExistByRoomName(room)){
            return ResponseEntity.badRequest().body((new MessageResponse("Ошибка! Такой комнаты не существует.")));
        }
        return ResponseEntity.ok(timetableService.getRoomTimetable(room));
    }

    @GetMapping("/faculty/{faculty}")
    @Transactional
    public ResponseEntity<?> getFacultyTimetable(@PathVariable @Valid @NotBlank String faculty) {
        return ResponseEntity.ok(timetableService.getFacultyTimetable(faculty));
    }

}
