package ru.nsu.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "plan")
@NoArgsConstructor
@Data
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher")
    private String teacher;

    @Column(name = "subject")
    private String subject;

    @Column(name = "subject_type")
    private String subjectType;

    @Column(name = "groups")
    private String groups;

    @Column(name = "times_in_a_week")
    private int timesInAWeek;
}
