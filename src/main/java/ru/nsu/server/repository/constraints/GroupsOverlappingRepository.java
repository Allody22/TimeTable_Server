package ru.nsu.server.repository.constraints;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.constraints.GroupsOverlapping;

@Repository
public interface GroupsOverlappingRepository extends JpaRepository<GroupsOverlapping, Long> {

}
