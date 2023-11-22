package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.current.Constraint;

@Repository
public interface ConstraintRepository extends JpaRepository<Constraint, Long> {
}
