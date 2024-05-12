package ru.nsu.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class TestDataConfig {
    private List<Integer> groups;
    private Map<String, List<Integer>> rooms;
    private List<PlanData> plan;
    private List<ConstraintData> constraints;

    @Data
    public static class PlanData {
        private String teacher;
        private String subject;
        private String subject_type;
        private List<Integer> groups;
        private int times_in_a_week;
    }

    @Data
    public static class ConstraintData {
        private String name;
        private Map<String, Object> args;
    }
}
