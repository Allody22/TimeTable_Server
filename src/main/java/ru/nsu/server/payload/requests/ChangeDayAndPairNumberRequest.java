package ru.nsu.server.payload.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDayAndPairNumberRequest {

    @NotNull
    @Schema(description = "Айди пары (json строки из БД), которое хочется изменить.", example = "1", required = true)
    private Long subjectId;

    @NotNull
    @Schema(description = "Порядковый номер дня, на который хочется перенести пару. Например 1 - понедельник, 2 - вторник и тп.", example = "1", required = true)
    private Integer newDayNumber;

    @NotNull
    @Schema(description = "Порядковый номер пары, на которую хочется перенести пару. Например 1 - первая пара и тп", example = "1", required = true)
    private Integer newPairNumber;
}
