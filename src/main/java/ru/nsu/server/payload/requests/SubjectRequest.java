package ru.nsu.server.payload.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectRequest {

    @NotBlank
    @Schema(description = "Имя добавляемого предмета.", example = "Оптимизация java", required = true)
    private String name;

    @Schema(description = "Количество пар этого предмета в неделю.", example = "4", required = true)
    private int timesInAWeek;
}
