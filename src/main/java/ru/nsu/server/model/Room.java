package ru.nsu.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "rooms")
@NoArgsConstructor
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "type")
    private String type; //для лекции, терминальная и тп

    @Column(name = "capacity")
    private int capacity; //сколько людей влезает
}
