package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.potential.PotentialConstraint;

@Repository
public interface PotentialConstraintRepository extends JpaRepository<PotentialConstraint, Long> {
}
