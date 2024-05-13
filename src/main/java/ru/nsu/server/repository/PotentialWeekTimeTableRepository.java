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

    @Query(value = "SELECT * FROM potential_week_timetable WHERE :group = ANY(string_to_array(groups, ','))", nativeQuery = true)
    List<PotentialWeekTimetable> getAllByExactGroup(@Param("group") String groupPattern);

    @Query(value = "SELECT * FROM potential_week_timetable WHERE :group = ANY(string_to_array(groups, ',')) AND day_number = :day AND pair_number = :pair ", nativeQuery = true)
    List<PotentialWeekTimetable> getAllByExactGroupAndDayNumberAndPairNumber(@Param("group") String groupPattern, @Param("day") Integer dayNumber,@Param("pair") Integer pairNumber);

    Optional<List<PotentialWeekTimetable>> findByTeacherAndDayNumberAndPairNumber(String teacher, int dayNumber, int pairNumber);

    Optional<List<PotentialWeekTimetable>> findByDayNumberAndPairNumberAndRoom(int dayNumber, int pairNumber, String roomNumber);

    List<PotentialWeekTimetable> getAllByTeacher(String teacher);

    List<PotentialWeekTimetable> getAllByFacultyContaining(String faculty);

    Optional<List<PotentialWeekTimetable>> findByTeacherAndDayNumberAndPairNumberAndRoom(String teacher, int dayNumber, int pairNumber, String room);
}
