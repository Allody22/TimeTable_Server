package ru.nsu.server.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ConstraintModel {

    private String name;

    private Map<String, Object> args;
}
