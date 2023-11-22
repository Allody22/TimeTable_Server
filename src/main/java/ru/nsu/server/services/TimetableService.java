package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.Group;
import ru.nsu.server.model.Room;
import ru.nsu.server.model.Subject;
import ru.nsu.server.model.constants.ERole;
import ru.nsu.server.model.current.WeekTimetable;
import ru.nsu.server.repository.GroupRepository;
import ru.nsu.server.repository.RoleRepository;
import ru.nsu.server.repository.RoomRepository;
import ru.nsu.server.repository.SubjectRepository;
import ru.nsu.server.repository.UserRepository;
import ru.nsu.server.repository.WeekTimeTableRepository;

import java.util.List;

@Service
public class TimetableService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RoomRepository roomRepository;

    private final WeekTimeTableRepository weekTimeTableRepository;

    private final GroupRepository groupRepository;

    private final SubjectRepository subjectRepository;

    @Autowired
    public TimetableService(UserRepository userRepository, RoleRepository roleRepository,
                            WeekTimeTableRepository weekTimeTableRepository, GroupRepository groupRepository,
                            SubjectRepository subjectRepository, RoomRepository roomRepository) {
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
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
    public void saveNewGroup(String groupNumber, String faculty, int course) {
        Group group = new Group();
        group.setGroupNumber(groupNumber);
        group.setCourse(course);
        group.setFaculty(faculty);
        groupRepository.save(group);
    }

    public List<Group> getAllGroups() {
        return groupRepository.getAll();
    }

    public List<Group> getAllGroupsByFaculty(String faculty) {
        return groupRepository.getAllByFacultyContaining(faculty);
    }

    public boolean ifExistByGroupNumber(String groupNumber) {
        return groupRepository.existsByGroupNumber(groupNumber);
    }

    @Transactional
    public void saveNewSubject(String name) {
        Subject subject = new Subject();
        subject.setName(name);
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

    public List<Room> getAllRooms() {
        return roomRepository.getAll();
    }

    public boolean ifExistByRoomName(String roomName) {
        return roomRepository.existsByName(roomName);
    }

    @Transactional
    public void saveNewRoom(String name, String purpose) {
        Room room = new Room();
        room.setName(name);
        room.setPurpose(purpose);
        roomRepository.save(room);
    }

}
