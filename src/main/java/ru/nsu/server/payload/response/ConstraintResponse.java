package ru.nsu.server.payload.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintResponse {

    private String constraintNameRu;

    private String constraintNameEng;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    private Long id;

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
