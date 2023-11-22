package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.Group;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query(value = "SELECT * FROM group_university", nativeQuery = true)
    List<Group> getAll();

    List<Group> getAllByFacultyContaining(String faculty);

    boolean existsByGroupNumber(String groupNumber);
}
