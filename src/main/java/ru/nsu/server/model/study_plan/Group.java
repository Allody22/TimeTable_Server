package ru.nsu.server.model.study_plan;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "group_university")
@NoArgsConstructor
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_number", unique = true)
    private String groupNumber;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "course")
    private int course;

    @Column(name = "students_number")
    private int studentsNumber;
}
