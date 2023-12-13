package ru.nsu.server.payload.response;

import lombok.Data;

@Data
public class FailureResponse {

    private String teacher;

    private String subject;

    private String subjectType;

    private Integer integer;

    private Integer timesInAWeek;

    private Integer day;

    private Integer period;

    private Integer room;
}
