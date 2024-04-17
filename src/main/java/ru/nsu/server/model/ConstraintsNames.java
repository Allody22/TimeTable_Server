package ru.nsu.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "constraints_names")
@NoArgsConstructor
@Data
public class ConstraintsNames {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "ru_name")
    private String ruName;
}
