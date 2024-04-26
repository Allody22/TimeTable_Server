package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.potential.PotentialWeekTimetable;

import java.util.List;
import java.util.Optional;

@Repository
public interface PotentialWeekTimeTableRepository extends JpaRepository<PotentialWeekTimetable, Long> {
    List<PotentialWeekTimetable> getPotentialWeekTimetablesByRoom(String room);

    List<PotentialWeekTimetable> getAllByGroupsContaining(String group);

    @Query(value = "SELECT * FROM potential_week_timetable WHERE groups SIMILAR TO :groupPattern", nativeQuery = true)
    List<PotentialWeekTimetable> getAllByExactGroup(@Param("groupPattern") String groupPattern);

    Optional<List<PotentialWeekTimetable>> findByTeacherAndDayNumberAndPairNumber(String teacher, int dayNumber, int pairNumber);

    Optional<List<PotentialWeekTimetable>> findByDayNumberAndPairNumberAndRoom(int dayNumber, int pairNumber, String roomNumber);


    List<PotentialWeekTimetable> getAllByTeacher(String teacher);

    List<PotentialWeekTimetable> getAllByFacultyContaining(String faculty);

    Optional<List<PotentialWeekTimetable>> findByTeacherAndDayNumberAndPairNumberAndRoom(String teacher, int dayNumber, int pairNumber, String room);
}
