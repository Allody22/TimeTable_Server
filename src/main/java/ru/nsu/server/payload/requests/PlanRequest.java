package ru.nsu.server.payload.requests;


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
    private String teacher;

    @NotBlank
    private String subject;

    @NotBlank
    private String subjectType;

    @NotBlank
    private String groups;

    @NotNull
    private int timesInAWeek;
}
