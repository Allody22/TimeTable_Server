package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.Group;
import ru.nsu.server.model.Plan;
import ru.nsu.server.model.Room;
import ru.nsu.server.model.Subject;
import ru.nsu.server.model.constants.ERole;
import ru.nsu.server.repository.GroupRepository;
import ru.nsu.server.repository.PlanRepository;
import ru.nsu.server.repository.RoomRepository;
import ru.nsu.server.repository.SubjectRepository;
import ru.nsu.server.repository.UserRepository;

import java.util.List;

@Service
public class RoomGroupTeacherSubjectPlanService {

    private final UserRepository userRepository;

    private final RoomRepository roomRepository;

    private final SubjectRepository subjectRepository;

    private final PlanRepository planRepository;

    private final GroupRepository groupRepository;

    @Autowired
    public RoomGroupTeacherSubjectPlanService(UserRepository userRepository, PlanRepository planRepository,
                                              RoomRepository roomRepository, GroupRepository groupRepository,
                                              SubjectRepository subjectRepository) {
        this.roomRepository = roomRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    @Transactional
    public void saveNewGroup(String groupNumber, String faculty, int course, int studentsNumber) {
        Group group = new Group();
        group.setGroupNumber(groupNumber);
        group.setCourse(course);
        group.setStudentsNumber(studentsNumber);
        group.setFaculty(faculty);
        groupRepository.save(group);
    }

    @Transactional
    public void deleteGroupByNumber(String groupNumber) {
        groupRepository.deleteGroupByGroupNumber(groupNumber);
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

    public List<Room> getAllRooms() {
        return roomRepository.getAll();
    }

    public boolean ifExistByRoomName(String roomName) {
        return roomRepository.existsByName(roomName);
    }

    @Transactional
    public void saveNewRoom(String name, String type, int capacity) {
        Room room = new Room();
        room.setName(name);
        room.setCapacity(capacity);
        room.setType(type);
        roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(String name) {
        roomRepository.deleteRoomByName(name);
    }

    @Transactional
    public void saveNewPlan(String teacher, String subjectName, String subjectType,
                            String groups, int timesInAWeek) {
        Plan plan = new Plan();
        plan.setGroups(groups);
        plan.setTimesInAWeek(timesInAWeek);
        plan.setTeacher(teacher);
        plan.setSubject(subjectName);
        plan.setSubjectType(subjectType);
        planRepository.save(plan);
    }

    @Transactional
    public void deletePlanById(Long id) {
        planRepository.deleteById(id);
    }

    public boolean ifExistPlanById(Long id) {
        return planRepository.existsById(id);
    }

    public List<Plan> getAllPlan() {
        return planRepository.findAll();
    }


    @Transactional
    public void saveNewSubject(String name, int timesInAWeek) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setTimesInAWeek(timesInAWeek);
        subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(String name) {
        subjectRepository.deleteSubjectByName(name);
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
