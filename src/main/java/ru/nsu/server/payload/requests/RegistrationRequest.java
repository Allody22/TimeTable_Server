package ru.nsu.server.payload.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    @NotBlank
    @Email
    @Schema(description = "Почта человека, который собирается регистрироваться.", example = "admin2@gmail.com", required = true)
    private String email;

    @Schema(description = "Номер группы, к которой будет привязан зарегистрированный человек, если он является студентом.", example = "21215")
    private String group;

    @NotBlank
    @Schema(description = "ФИО человека.", example = "Богданов Михаил Сергеевич", required = true)
    private String fullName;

    @Schema(description = "Номер телефона, привязанный к аккаунту. Указывать не обязательно.", example = "88888888888")
    private String phone;
}
