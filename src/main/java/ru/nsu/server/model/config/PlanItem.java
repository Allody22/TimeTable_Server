package ru.nsu.server.model.config;

import lombok.Data;

import java.util.List;

@Data
public class PlanItem {

    private String teacher;

    private String subject;

    private String subject_type;

    private List<Integer> groups;

    private int times_in_a_week;
}
