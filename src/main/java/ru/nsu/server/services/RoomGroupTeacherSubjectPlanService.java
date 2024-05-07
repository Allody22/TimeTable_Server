package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.constants.ERole;
import ru.nsu.server.model.study_plan.Group;
import ru.nsu.server.model.study_plan.Plan;
import ru.nsu.server.model.study_plan.Room;
import ru.nsu.server.model.study_plan.Subject;
import ru.nsu.server.model.user.Operations;
import ru.nsu.server.repository.*;

import java.util.Date;
import java.util.List;

@Service
public class RoomGroupTeacherSubjectPlanService {

    private final UserRepository userRepository;

    private final RoomRepository roomRepository;

    private final SubjectRepository subjectRepository;

    private final PlanRepository planRepository;

    private final GroupRepository groupRepository;

    private final OperationsRepository operationsRepository;

    @Autowired
    public RoomGroupTeacherSubjectPlanService(UserRepository userRepository, PlanRepository planRepository,
                                              RoomRepository roomRepository, GroupRepository groupRepository,
                                              SubjectRepository subjectRepository, OperationsRepository operationsRepository) {
        this.roomRepository = roomRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.operationsRepository = operationsRepository;
    }

    @Transactional
    public String saveNewGroup(String groupNumber, String faculty, int course, int studentsNumber) {
        Group group = new Group();
        group.setGroupNumber(groupNumber);
        group.setCourse(course);
        group.setStudentsNumber(studentsNumber);
        group.setFaculty(faculty);
        groupRepository.save(group);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Сохранена группа с номером '" + groupNumber + "' для курса '" + course + "' и факультета '"
                + faculty + "' с количеством студентов " + studentsNumber;
        operations.setDescription(description);
        operationsRepository.save(operations);
        return description + ". Операция сделана пользователем пользователем " + operations.getUserAccount();
    }

    @Transactional
    public String deleteGroupByNumber(String groupNumber) {
        groupRepository.deleteGroupByGroupNumber(groupNumber);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Удалена группа с номером '" + groupNumber + "'";
        operations.setDescription(description);
        operationsRepository.save(operations);
        return description + ". Операция сделана пользователем пользователем " + operations.getUserAccount();
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
    public String saveNewRoom(String name, String type, int capacity) {
        Room room = new Room();
        room.setName(name);
        room.setCapacity(capacity);
        room.setType(type);
        roomRepository.save(room);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Сохранена комната с номером '" + name + "' на '" + capacity + "' людей с типом" + type;
        operations.setDescription(description);
        operationsRepository.save(operations);
        return description + ". Операция сделана пользователем пользователем " + operations.getUserAccount();
    }

    @Transactional
    public String deleteRoom(String name) {
        roomRepository.deleteRoomByName(name);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Удалена комната с номером " + name;
        operations.setDescription(description);
        operationsRepository.save(operations);
        return description + ". Операция сделана пользователем пользователем " + operations.getUserAccount();
    }

    @Transactional
    public String saveNewPlan(String teacher, String subjectName, String subjectType,
                              String groups, int timesInAWeek) {
        Plan plan = new Plan();
        plan.setGroups(groups);
        plan.setTimesInAWeek(timesInAWeek);
        plan.setTeacher(teacher);
        plan.setSubject(subjectName);
        plan.setSubjectType(subjectType);
        plan.setUserName("Admin");
        planRepository.save(plan);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Добавлен новый план для групп '" + groups + "' на '" + timesInAWeek + "' раз в неделю с преподавателем '"
                + teacher + "' и предметом '" + subjectName + "' и типом предмета '" + subjectType + "' с созданным айди плана " + plan.getId();
        operations.setDescription(description);
        operationsRepository.save(operations);
        return description + ". Операция сделана пользователем пользователем " + operations.getUserAccount();
    }

    @Transactional
    public String deletePlanById(Long id) {
        planRepository.deleteById(id);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Удалён план с айди " + id;
        operations.setDescription(description);
        operationsRepository.save(operations);
        return description + ". Операция сделана пользователем пользователем " + operations.getUserAccount();
    }

    public boolean ifExistPlanById(Long id) {
        return planRepository.existsById(id);
    }

    public List<Plan> getAllPlan() {
        return planRepository.findAll();
    }


    @Transactional
    public String saveNewSubject(String name, int timesInAWeek) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setTimesInAWeek(timesInAWeek);
        subjectRepository.save(subject);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Добавлен новый предмет '" + name + "' на столько раз в неделю " + timesInAWeek;
        operations.setDescription(description);
        operationsRepository.save(operations);
        return description;
    }

    @Transactional
    public String deleteSubject(String name) {
        subjectRepository.deleteSubjectByName(name);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Удалён предмет '" + name + "'";
        operations.setDescription(description);
        return description;
    }

    public List<String> getAllSubjects() {
        return subjectRepository.getAll();
    }

    public boolean ifExistBySubjectName(String subjectName) {
        return subjectRepository.existsByName(subjectName);
    }

    public boolean ifExistTeacherByFullName(String fullName) {
        return userRepository.existsByFullName(fullName);
    }

    public List<String> getAllTeachers() {
        return userRepository.findAllUsersByRole(ERole.ROLE_TEACHER);
    }
}
