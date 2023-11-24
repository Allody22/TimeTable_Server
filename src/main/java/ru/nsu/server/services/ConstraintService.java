package ru.nsu.server.services;

import org.springframework.stereotype.Service;
import ru.nsu.server.repository.ConstraintNamesRepository;
import ru.nsu.server.repository.ConstraintRepository;
import ru.nsu.server.repository.PotentialConstraintRepository;

import java.util.List;

@Service
public class ConstraintService {

    private final ConstraintRepository constraintRepository;

    private final PotentialConstraintRepository potentialConstraintRepository;

    private final ConstraintNamesRepository constraintNamesRepository;

    public ConstraintService(ConstraintRepository constraintRepository, PotentialConstraintRepository potentialConstraintRepository,
                             ConstraintNamesRepository constraintNamesRepository) {
        this.constraintRepository = constraintRepository;
        this.constraintNamesRepository = constraintNamesRepository;
        this.potentialConstraintRepository = potentialConstraintRepository;
    }

    public List<String> getAllConstraintsRu() {
        return constraintNamesRepository.getAllRuNames()
                .orElse(null);
    }

    public List<String> getAllConstraintsEng() {
        return constraintNamesRepository.getAllEngNames()
                .orElse(null);
    }
}
