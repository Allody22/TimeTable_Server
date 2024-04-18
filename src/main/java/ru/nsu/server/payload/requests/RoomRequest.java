package ru.nsu.server.payload.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {

    @NotBlank
    @Schema(description = "Название комнаты, которая будет добавлена в запрос.", example = "3228", required = true)
    private String name;

    @NotBlank
    @Schema(description = "Тип комнаты (лекционная, терминальная и тп).", example = "Лекционная", required = true)
    private String type;

    @Schema(description = "Вместимость комнаты.", example = "120", required = true)
    private int capacity;
}
