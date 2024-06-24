package ru.nsu.server.payload.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintResponse {

    private String constraintNameRu;

    private String constraintNameEng;

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

    private String subject;

    private String subjectType;

    private String groups;

    private String room;
}
