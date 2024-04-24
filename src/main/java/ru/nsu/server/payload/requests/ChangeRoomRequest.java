package ru.nsu.server.payload.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoomRequest {

    @NotNull
    @Schema(description = "Айди пары (json строки из БД), которое хочется изменить.", example = "1", required = true)
    private Long subjectId;

    @NotNull
    @NotBlank
    @Schema(description = "Новая комната, в которую хочется перенести пару. Комната обязана быть из БД, а тип комнаты должен соответствовать типу пары", example = "12345", required = true)
    private String newRoom;
}
