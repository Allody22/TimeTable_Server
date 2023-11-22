package ru.nsu.server.model.current;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "constraint")
@NoArgsConstructor
@Data
public class Constraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_number")
    private int dayNumber;

    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "groups")
    private String groups;

    @Column(name = "teacher")
    private String teacher;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "course")
    private int course;

    @Column(name = "room")
    private String room;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    @Column(name = "start_time")
    private Date startTime;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "pair_type")
    private String pairType;
}
