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
