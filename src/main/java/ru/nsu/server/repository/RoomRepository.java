package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.study_plan.Room;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query(value = "SELECT * FROM rooms", nativeQuery = true)
    List<Room> getAll();

    boolean existsByName(String name);

    void deleteRoomByName(String name);

    Optional<Room> findRoomByName(String name);
}
