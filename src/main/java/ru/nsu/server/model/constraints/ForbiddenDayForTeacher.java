package ru.nsu.server.model.constraints;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "forbidden_day_for_teacher")
@NoArgsConstructor
@Data
public class ForbiddenDayForTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    @Column(name = "creation_time")
    private Date dateOfCreation;

    @Column(name = "teacher")
    @NotNull
    private String teacher;

    @Column(name = "day")
    @NotNull
    private int day;
}
