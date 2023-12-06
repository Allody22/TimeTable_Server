package ru.nsu.server.repository.constraints;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.constraints.TeachersOverlapping;

@Repository
public interface TeachersOverlappingRepository extends JpaRepository<TeachersOverlapping, Long> {

}
