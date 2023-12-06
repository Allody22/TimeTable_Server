package ru.nsu.server.repository.constraints;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.constraints.ForbiddenDayForGroup;

@Repository
public interface ForbiddenDayForGroupRepository extends JpaRepository<ForbiddenDayForGroup, Long> {

}
