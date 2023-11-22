package ru.nsu.server.payload.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintRequest {

    @NotBlank
    @Email
    private String subjectName;

    @NotBlank
    private String group;

    @NotBlank
    private String name;

    @NotBlank
    private String lastName;

    @NotBlank
    private String patronymic;

    @NotBlank
    private String phone;
}
