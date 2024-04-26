package ru.nsu.server.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConstraintModelForVariants {

    private Long pairId;

    private Integer dayNumber;

    private Integer pairNumber;

    private String subjectName;

    private String groups;

    private String teacher;

    private String room;

    private String pairType;

    private String faculty;

    private Integer course;

    private List<ConstraintModel> constraintModels;
}