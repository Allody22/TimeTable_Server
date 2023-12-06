package ru.nsu.server.model.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConfigModel {

    private List<Integer> groups;

    private Map<String, List<Integer>> rooms;

    private List<PlanItem> plan;

    private List<ConstraintModel> constraints;
}
