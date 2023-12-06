package ru.nsu.server.repository.constraints;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.constraints.NumberOfTeachingDays;

@Repository
public interface NumberOfTeachingDaysRepository extends JpaRepository<NumberOfTeachingDays, Long> {

}
