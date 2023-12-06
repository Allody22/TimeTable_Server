package ru.nsu.server.model.config;

import lombok.Data;

import java.util.Map;

@Data
public class ConstraintModel {

    private String name;

    private Map<String, Object> args;
}
