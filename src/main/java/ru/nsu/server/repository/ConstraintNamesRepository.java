package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.constraints.ConstraintsNames;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConstraintNamesRepository extends JpaRepository<ConstraintsNames, Long> {

    @Transactional
    @Query(value = "SELECT ru_name FROM constraints_names", nativeQuery = true)
    Optional<List<String>> getAllRuNames();

    @Transactional
    @Query(value = "SELECT name FROM constraints_names", nativeQuery = true)
    Optional<List<String>> getAllEngNames();

    boolean existsByName(String name);

    boolean existsByRuName(String name);

    Optional<ConstraintsNames> findConstraintsNamesByRuName(String ruName);
}
