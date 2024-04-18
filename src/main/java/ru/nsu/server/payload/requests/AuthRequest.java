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
public class AuthRequest {

    @NotBlank
    @Email
    @Schema(description = "Почта, привязанная к этому аккаунту.", example = "admin@gmail.com", required = true)
    private String email = null;

    @Schema(description = "Пароль аккаунта.", example = "test", required = true)
    @NotBlank
    private String password = null;
}
