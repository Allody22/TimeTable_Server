package ru.nsu.server.payload.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintTimeRequest {

    private String constraintName;

    private String subjectName;

    private List<Integer> allowedPairNumber;

    private List<Integer> notAllowedPairNumber;

    private int weekNumber;

    private String teacherName;

    private String roomName;
}
