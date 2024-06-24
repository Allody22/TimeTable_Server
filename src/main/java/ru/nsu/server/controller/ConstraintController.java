package ru.nsu.server.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.nsu.server.exception.NotInDataBaseException;
import ru.nsu.server.payload.requests.ConstraintRequest;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.ConstraintService;
import ru.nsu.server.services.TimetableService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/constraints")
@AllArgsConstructor
@Tag(name = "03. Constraint controller", description = "Контроллер ограничений. В нём человек может удалять, создавать и просматривать ограничения.")
public class ConstraintController {

    private final ConstraintService constraintService;

    private final TimetableService timetableService;

    @Operation(
            summary = "Получение всех ограничений.",
            description = """
                    Получается информация о всех созданных до этого момента ограничения и возвращается подробная информация о них.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = ConstraintController[].class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STAFF')")
    @GetMapping("/get_all")
    @Transactional
    public ResponseEntity<?> getAllConstraints() {
        return ResponseEntity.ok(constraintService.getAllConstraints());
    }


    @Operation(
            summary = "Создание нового ограничения.",
            description = """
                    Создание нового ограничения с определённым названием по условиям, характерным для данного ограничения.
                    Важно, чтобы ограничение с таким именем (названием) вообще существовало и поддерживалось на сервере.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STAFF')")
    @Transactional
    public ResponseEntity<?> createConstraint(@Valid @RequestBody ConstraintRequest constraintRequest) {
        //TODO потом раскоментить
//        if (!timetableService.getAllPotentialTimeTable().isEmpty()){
//            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! В уже составленное расписание нельзя вносить новые ограничения"));
//        }
        String constraintNameRu = constraintRequest.getConstraintNameRu();
        String constraintNameEng = (constraintService.findConstraintByRuName(constraintNameRu)
                .orElseThrow(() -> new NotInDataBaseException("Такого ограничения не существует или оно еще не поддерживается: " + constraintNameRu))).getName();
        constraintService.saveNewUniversalConstraint(constraintNameRu, constraintNameEng, constraintRequest.getGroup(), constraintRequest.getGroup1(),
                constraintRequest.getGroup2(), constraintRequest.getTeacher(), constraintRequest.getTeacher1(),
                constraintRequest.getTeacher2(), constraintRequest.getDay(), constraintRequest.getPeriod(),
                constraintRequest.getNumber(), constraintRequest.getSubjectName(), constraintRequest.getRoom(),
                constraintRequest.getGroups(), constraintRequest.getSubjectType());
        return ResponseEntity.ok(new MessageResponse("Ограничение успешно сохранено"));
    }

    @Operation(
            summary = "Удаление ограничения по его айди.",
            description = """
                    Передаётся айди существующего ограничения, а потом, если такой айди существует, то оно удаляется.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STAFF')")
    @Transactional
    public ResponseEntity<?> deleteConstraint(@Parameter(description = "Уникальный существующее айди ограничения", example = "1") @PathVariable("id") @Valid @NotBlank Long id) {
        if (!constraintService.existById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Ограничения с айди " + id.toString() + " не существует."));
        }
        constraintService.deleteUniversalConstraint(id);
        return ResponseEntity.ok(new MessageResponse("Ограничение успешно удалено"));
    }
}
