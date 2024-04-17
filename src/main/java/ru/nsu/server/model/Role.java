package ru.nsu.server.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.server.model.constants.ERole;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private ERole name;

    public Role(ERole name) {
        this.name = name;
    }
}
