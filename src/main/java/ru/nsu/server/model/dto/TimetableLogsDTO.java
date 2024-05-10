package ru.nsu.server.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimetableLogsDTO {

    @JsonFormat(timezone = "Asia/Novosibirsk")
    private Date dateOfCreation;

    private String description;

    private String userAccount;

    private String operationName;

    private Long subjectId;

    private Integer newDayNumber;

    private Integer newPairNumber;

    private String newRoom;

    private String newTeacherFullName;

    private Integer oldDayNumber;

    private Integer oldPairNumber;

    private String oldRoom;

    private String oldTeacherFullName;
}
