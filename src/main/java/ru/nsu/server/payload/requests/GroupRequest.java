package ru.nsu.server.payload.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequest {

    @NotBlank
    private String groupNumber;

    @NotBlank
    private String faculty;

    private int course;

    private int studentsNumber;
}
