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

    private Integer group;

    private Integer group1;

    private Integer group2;

    private Integer number;

    private Integer day;

    private String teacher;

    private String teacher1;

    private String teacher2;

    private Integer period;
}
