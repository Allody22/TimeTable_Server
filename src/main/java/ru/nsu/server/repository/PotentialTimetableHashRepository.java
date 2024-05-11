package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.potential.PotentialTimetableHash;

import java.util.Optional;

@Repository
public interface PotentialTimetableHashRepository extends JpaRepository<PotentialTimetableHash, Long> {

    Optional<PotentialTimetableHash> findByHashValue(String hashValue);

}
