package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.Group;
import ru.nsu.server.model.Operations;

import java.util.List;

@Repository
public interface OperationsRepository extends JpaRepository<Operations, Long> {
}
