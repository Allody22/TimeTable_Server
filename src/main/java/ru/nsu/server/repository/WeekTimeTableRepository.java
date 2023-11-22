package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.current.WeekTimetable;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeekTimeTableRepository extends JpaRepository<WeekTimetable, Long> {

    Optional<WeekTimetable> getWeekTimetableByRoom(String roomNumber);

    List<WeekTimetable> getWeekTimetablesByRoom(String roomNumber);

    List<WeekTimetable> getWeekTimetablesByDayNumber(int dayNumber);

    List<WeekTimetable> getAllByGroupsContaining(String groupNumber);

    List<WeekTimetable> getAllByTeacher(String teacher);

    List<WeekTimetable> getAllByCourse(int course);

    List<WeekTimetable> getAllByFacultyContaining(String faculty);

    List<WeekTimetable> getAllByFaculty(String faculty);
}
