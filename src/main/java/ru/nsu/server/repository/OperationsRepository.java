package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.Operations;

@Repository
public interface OperationsRepository extends JpaRepository<Operations, Long> {
}
