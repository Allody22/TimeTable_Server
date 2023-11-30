package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
}
