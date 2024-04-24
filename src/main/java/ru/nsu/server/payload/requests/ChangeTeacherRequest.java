package ru.nsu.server.payload.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTeacherRequest {

    @NotNull
    @Schema(description = "Айди пары (json строки из БД), которое хочется изменить.", example = "1", required = true)
    private Long subjectId;

    @NotNull
    @NotBlank
    @Schema(description = "ФИО учителя, которвый теперь будет преподавать пару. Учитель обязан существовать в БД", example = "12345", required = true)
    private String newTeacherFullName;
}
