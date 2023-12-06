package ru.nsu.server.repository.constraints;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.constraints.ForbiddenPeriodForGroup;

@Repository
public interface ForbiddenPeriodForGroupRepository extends JpaRepository<ForbiddenPeriodForGroup, Long> {

}
