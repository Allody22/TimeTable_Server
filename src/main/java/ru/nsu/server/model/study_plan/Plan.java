package ru.nsu.server.model.study_plan;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    @Column(name = "user_name")
    private String userName;
}
