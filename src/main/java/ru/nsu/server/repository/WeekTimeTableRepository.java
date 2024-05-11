package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.actual.WeekTimetable;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeekTimeTableRepository extends JpaRepository<WeekTimetable, Long> {

    //TODO как вызвать блокировку?
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<WeekTimetable> findById(Long id);

    Optional<WeekTimetable> getWeekTimetableByRoom(String roomNumber);

    List<WeekTimetable> getWeekTimetablesByRoom(String roomNumber);

    List<WeekTimetable> getWeekTimetablesByDayNumber(int dayNumber);

    Optional<List<WeekTimetable>> findByTeacherAndDayNumberAndPairNumber(String teacher, int dayNumber, int pairNumber);

    Optional<List<WeekTimetable>> findByDayNumberAndPairNumberAndRoom(int dayNumber, int pairNumber, String roomNumber);


    List<WeekTimetable> getAllByGroupsContaining(String groupNumber);

    @Query(value = "SELECT * FROM week_timetable WHERE :group = ANY(string_to_array(groups, ','))", nativeQuery = true)
    List<WeekTimetable> getAllByExactGroup(@Param("group") String group);

    List<WeekTimetable> getAllByTeacher(String teacher);

    List<WeekTimetable> getAllByCourse(int course);

    List<WeekTimetable> getAllByFacultyContaining(String faculty);

    List<WeekTimetable> getAllByFaculty(String faculty);
}