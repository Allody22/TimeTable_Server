package ru.nsu.server.payload.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanRequest {

    @NotBlank
    @Schema(description = "ФИО учителя, который будет привязан к этому предмету в учебном плане.", example = "Богданов Михаил Сергеевич", required = true)
    private String teacher;

    @NotBlank
    @Schema(description = "Название предмета, который будет привязан к этому учителя в учебном плане.", example = "Оптимизация java", required = true)
    private String subject;

    @NotBlank
    @Schema(description = "Тип предмета (лекционный, терминальный и тп), который будет привязан к этому предмету в учебном плане.", example = "Лекция", required = true)
    private String subjectType;

    @NotBlank
    @Schema(description = "Номер группы, который будет привязан к этому предмету в учебном плане.", example = "21215", required = true)
    private String groups;

    @NotNull
    @Schema(description = "Количество пар в неделю, которое будет привязан к этому предмету в учебном плане.", example = "4", required = true)
    private int timesInAWeek;
}
