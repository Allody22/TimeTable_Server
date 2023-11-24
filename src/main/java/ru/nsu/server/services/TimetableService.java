package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.Subject;
import ru.nsu.server.model.constants.ERole;
import ru.nsu.server.model.current.WeekTimetable;
import ru.nsu.server.repository.SubjectRepository;
import ru.nsu.server.repository.UserRepository;
import ru.nsu.server.repository.WeekTimeTableRepository;

import java.util.List;

@Service
public class TimetableService {

    private final UserRepository userRepository;

    private final WeekTimeTableRepository weekTimeTableRepository;

    private final SubjectRepository subjectRepository;

    @Autowired
    public TimetableService(UserRepository userRepository,
                            WeekTimeTableRepository weekTimeTableRepository,
                            SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public void saveNewSubject(String name, int timesInAWeek) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setTimesInAWeek(timesInAWeek);
        subjectRepository.save(subject);
    }

    public List<String> getAllSubjects() {
        return subjectRepository.getAll();
    }

    public boolean ifExistBySubjectName(String subjectName) {
        return subjectRepository.existsByName(subjectName);
    }

    public List<String> getAllTeachers() {
        return userRepository.findAllUsersByRole(ERole.ROLE_TEACHER);
    }
}
