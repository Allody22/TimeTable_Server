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
import java.util.Date;

@Entity
@Table(name = "universal_constraint")
@NoArgsConstructor
@Data
public class UniversalConstraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(timezone = "Asia/Novosibirsk")
    @Column(name = "creation_time")
    private Date dateOfCreation;

    @Column(name = "constraint_name_ru")
    private String constraintNameRu;

    @Column(name = "constraint_name")
    private String constraintName;

    @Column(name = "group_university")
    private Integer group;

    @Column(name = "group_1")
    private Integer group1;

    @Column(name = "group_2")
    private Integer group2;

    @Column(name = "teacher")
    private String teacher;

    @Column(name = "teacher_1")
    private String teacher1;

    @Column(name = "teacher_2")
    private String teacher2;

    @Column(name = "day")
    private Integer day;

    @Column(name = "period")
    private Integer period;

    @Column(name = "number")
    private Integer number;
}
