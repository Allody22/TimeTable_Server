package ru.nsu.server.model.current;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "constraint")
@NoArgsConstructor
@Data
public class Constraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
