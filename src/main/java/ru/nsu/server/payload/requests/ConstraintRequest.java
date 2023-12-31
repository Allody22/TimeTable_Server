package ru.nsu.server.payload.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintRequest {

    @NotBlank
    private String constraintNameRu;

    private Integer group = -1; // значение по умолчанию

    private Integer group1 = -1; // значение по умолчанию

    private Integer group2 = -1; // значение по умолчанию

    private Integer number = -1; // значение по умолчанию

    private Integer day = -1; // значение по умолчанию

    private String teacher = "Не указан"; // значение по умолчанию

    private String teacher1 = "Не указан"; // значение по умолчанию

    private String teacher2 = "Не указан"; // значение по умолчанию

    private Integer period = -1; // значение по умолчанию
}
