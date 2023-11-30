package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.server.model.current.WeekTimetable;
import ru.nsu.server.repository.WeekTimeTableRepository;

import java.util.List;

@Service
public class TimetableService {

    private final WeekTimeTableRepository weekTimeTableRepository;

    @Autowired
    public TimetableService(WeekTimeTableRepository weekTimeTableRepository) {
        this.weekTimeTableRepository = weekTimeTableRepository;
    }

    public List<WeekTimetable> getGroupTimetable(String group) {
        return weekTimeTableRepository.getAllByGroupsContaining(group);
    }

    public List<WeekTimetable> getTeacherTimetable(String teacher) {
        return weekTimeTableRepository.getAllByTeacher(teacher);
    }

    public List<WeekTimetable> getFacultyTimetable(String faculty) {
        return weekTimeTableRepository.getAllByFaculty(faculty);
    }

    public List<WeekTimetable> getRoomTimetable(String room) {
        return weekTimeTableRepository.getWeekTimetablesByRoom(room);
    }

}
