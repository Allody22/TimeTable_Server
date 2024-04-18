package ru.nsu.server.payload.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequest {

    @NotBlank
    @Schema(description = "Номер группы, которая участвует в вопросе.", example = "21215", required = true)
    private String groupNumber;

    @NotBlank
    @Schema(description = "Факультет, привязанный к данной группе.", example = "ФИТ", required = true)
    private String faculty;

    @Schema(description = "Курс группы.", example = "2")
    private int course;

    @Schema(description = "Количество студентов в группе.", example = "21")
    private int studentsNumber;
}
