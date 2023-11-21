package ru.nsu.server.model.potential;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "potential_sunday")
@NoArgsConstructor
@Data
public class PotentialSunday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
