package ru.nsu.server.payload.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintRequest {

    @NotBlank
    @Schema(description = "Русское название ограничения, существующее в базе данных.", example = "Максимальное кол-во рабочих дней", required = true)
    private String constraintNameRu;

    @Schema(description = "Стандартный номер группы, привязанный к ограничению.", example = "21215")
    private Integer group = -1; // значение по умолчанию

    @Schema(description = "В ограничениях может участвовать больше 1 группы, это вторая потенциальная группа.", example = "21216")
    private Integer group1 = -1; // значение по умолчанию

    @Schema(description = "В ограничениях может участвовать больше 1 группы, это третья потенциальная группа.", example = "21217")
    private Integer group2 = -1; // значение по умолчанию

    @Schema(description = "Номер, участвующий в ограничении.", example = "1")
    private Integer number = -1; // значение по умолчанию

    @Schema(description = "День недели, которое потенциально необходимо использовать в ограничении.", example = "1")
    private Integer day = -1; // значение по умолчанию

    @Schema(description = "Базовое имя учителя, которое потенциально повлияет на ограничение.", example = "Богданов Михаил Нулёвич")
    private String teacher = "Не указан"; // значение по умолчанию

    @Schema(description = "Еще одно имя учителя, которое потенциально повлияет на ограничение.", example = "Богданов Михаил Первич")
    private String teacher1 = "Не указан"; // значение по умолчанию

    @Schema(description = "Еще одно имя учителя, которое потенциально повлияет на ограничение.", example = "Богданов Михаил Второевич")
    private String teacher2 = "Не указан"; // значение по умолчанию

    @Schema(description = "Заданный временной период, который может повлиять на ограничение.", example = "1")
    private Integer period = -1; // значение по умолчанию

    @Schema(description = "Название предмета, на который может повлиять на ограничение.", example = "изучение java")
    private String subjectName = "";
}
