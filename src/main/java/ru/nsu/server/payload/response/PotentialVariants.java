package ru.nsu.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PotentialVariants {

    private Long pairId;

    private Integer dayNumber;

    private String subjectName;

    private String groups;

    private String teacher;

    private String faculty;

    private int course;

    private String room;

    private Integer pairNumber;

    private String pairType;

}