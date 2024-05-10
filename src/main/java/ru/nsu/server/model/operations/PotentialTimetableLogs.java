package ru.nsu.server.model.operations;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "potential_timetable_logs")
@NoArgsConstructor
@Data
public class PotentialTimetableLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    @Column(name = "creation_time")
    private Date dateOfCreation;

    @Column(name = "description")
    private String description;

    @Column(name = "username")
    private String userAccount;

    @Column(name = "operation_name")
    private String operationName;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "new_day_number")
    private Integer newDayNumber;

    @Column(name = "new_pair_number")
    private Integer newPairNumber;

    @Column(name = "new_room")
    private String newRoom;

    @Column(name = "new_teacher_full_name")
    private String newTeacherFullName;

    @Column(name = "old_day_number")
    private Integer oldDayNumber;

    @Column(name = "old_pair_number")
    private Integer oldPairNumber;

    @Column(name = "old_room")
    private String oldRoom;

    @Column(name = "old_teacher_full_name")
    private String oldTeacherFullName;
}