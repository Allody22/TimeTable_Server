package ru.nsu.server.services;

import org.springframework.stereotype.Service;
import ru.nsu.server.repository.ConstraintRepository;
import ru.nsu.server.repository.PotentialConstraintRepository;

@Service
public class ConstraintService {

    private final ConstraintRepository constraintRepository;

    private final PotentialConstraintRepository potentialConstraintRepository;

    public ConstraintService(ConstraintRepository constraintRepository, PotentialConstraintRepository potentialConstraintRepository) {
        this.constraintRepository = constraintRepository;
        this.potentialConstraintRepository = potentialConstraintRepository;
    }
}
