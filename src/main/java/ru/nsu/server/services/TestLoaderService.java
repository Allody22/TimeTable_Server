package ru.nsu.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.exception.NotInDataBaseException;
import ru.nsu.server.model.TestDataConfig;
import ru.nsu.server.model.constants.ERole;
import ru.nsu.server.model.constraints.UniversalConstraint;
import ru.nsu.server.model.study_plan.Group;
import ru.nsu.server.model.study_plan.Plan;
import ru.nsu.server.model.study_plan.Room;
import ru.nsu.server.model.study_plan.Subject;
import ru.nsu.server.model.user.Role;
import ru.nsu.server.model.user.User;
import ru.nsu.server.repository.*;
import ru.nsu.server.repository.constraints.UniversalConstraintRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestLoaderService {

    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final PlanRepository planRepository;
    private final UniversalConstraintRepository universalConstraintRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Value("${timetable.url.java.test}")
    private String javaTestResources;

    public TestLoaderService(GroupRepository groupRepository, RoomRepository roomRepository, PlanRepository planRepository, UniversalConstraintRepository universalConstraintRepository, SubjectRepository subjectRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.groupRepository = groupRepository;
        this.roomRepository = roomRepository;
        this.planRepository = planRepository;
        this.universalConstraintRepository = universalConstraintRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public TestDataConfig readTestDataConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String baseDir = System.getProperty("user.dir");
        String jsonFilePath = baseDir + javaTestResources;
        return mapper.readValue(new File(jsonFilePath), TestDataConfig.class);
    }

    @Transactional
    public void initializeDatabaseFromTestData() throws IOException {
        log.info("start of initializeDatabaseFromTestData");
        TestDataConfig config = readTestDataConfig();

        // Save groups
        config.getGroups().forEach(group -> {
            if (!groupRepository.existsByGroupNumber(String.valueOf(group))) {
                Group g = new Group();
                g.setGroupNumber(String.valueOf(group));
                g.setFaculty("ФИТ");
                g.setCourse(1);
                g.setStudentsNumber(40);
                groupRepository.save(g);
            }
        });

        // Save rooms
        config.getRooms().forEach((type, roomList) -> {
            roomList.forEach(room -> {
                if (!roomRepository.existsByName(String.valueOf(room))) {
                    Room r = new Room();
                    r.setName(String.valueOf(room));
                    r.setCapacity(100);
                    r.setType(type);
                    roomRepository.save(r);
                }
            });
        });

        // Save plans
        config.getPlan().forEach(planData -> {
            String groupString = planData.getGroups().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            if (!planRepository.existsByTeacherAndSubjectAndSubjectTypeAndGroupsAndTimesInAWeek(
                    planData.getTeacher(), planData.getSubject(), planData.getSubject_type(), groupString, planData.getTimes_in_a_week())) {
                Plan plan = new Plan();
                plan.setTeacher(planData.getTeacher());
                plan.setSubject(planData.getSubject());
                plan.setSubjectType(planData.getSubject_type());
                plan.setGroups(groupString);
                plan.setTimesInAWeek(planData.getTimes_in_a_week());

                planRepository.save(plan);
            }
        });

        log.info("time for constraints");

        // Save constraints
        config.getConstraints().forEach(constraintData -> {
            UniversalConstraint uc = new UniversalConstraint();
            String name = constraintData.getName();
            uc.setConstraintName(constraintData.getName());
            uc.setDateOfCreation(new Date());
            Map<String, Object> args = constraintData.getArgs();
            switch (name) {
                case "teachers_overlapping":
                    uc.setTeacher1((String) args.get("teacher_1"));
                    uc.setTeacher2((String) args.get("teacher_2"));
                    uc.setConstraintNameRu("Недопустимое пересечение преподавателей:");
                    break;
                case "number_of_teaching_days":
                    uc.setTeacher((String) args.get("teacher"));
                    uc.setNumber((Integer) args.get("number"));
                    uc.setConstraintNameRu("Максимальное кол-во рабочих дней:");

                    break;
                case "forbidden_period_for_teacher":
                case "forbidden_period_for_group":
                    uc.setDay((Integer) args.get("day"));
                    uc.setPeriod((Integer) args.get("period"));
                    if (args.containsKey("teacher")) {
                        uc.setTeacher((String) args.get("teacher"));
                    } else {
                        uc.setGroup((Integer) args.get("group"));
                    }
                    uc.setConstraintNameRu("Запрещенные порядковые номера пар для групп в определённый день: ");
                    break;
                case "forbidden_day_for_teacher":
                case "forbidden_day_for_group":
                    uc.setDay((Integer) args.get("day"));
                    if (args.containsKey("teacher")) {
                        uc.setTeacher((String) args.get("teacher"));
                    } else {
                        uc.setGroup((Integer) args.get("group"));
                    }
                    uc.setConstraintNameRu("Запрещенный день для преподавания для группы:");

                    break;
                case "exact_time":
                    uc.setTeacher((String) args.get("teacher"));
                    String subject = (String) args.get("subject");
                    String subjectName = " ";
                    String subjectType = " ";

                    int lastIndex = subject.lastIndexOf('_');

                    if (lastIndex != -1) { // Check if the underscore is found
                        subjectName = subject.substring(0, lastIndex); // Extract the main part
                        subjectType = subject.substring(lastIndex + 1); // Extract the type part after the underscore
                    } else {
                        log.error("No subject type");
                    }

                    uc.setSubject(subjectName);
                    uc.setSubjectType(subjectType);
                    uc.setDay((Integer) args.get("day"));
                    uc.setPeriod((Integer) args.get("period"));
                    uc.setRoom(args.get("room").toString());


                    @SuppressWarnings("unchecked")
                    List<Integer> groups = (List<Integer>) args.get("groups");
                    String groupString = groups.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                    uc.setGroups(groupString); // Используйте group как строку или другое поле для списка групп
                    uc.setConstraintNameRu("Обязательное время пары:");

                    break;
            }


            if (!universalConstraintRepository.existsByConstraintNameAndGroupAndGroup1AndGroup2AndTeacherAndTeacher1AndTeacher2AndDayAndPeriodAndNumberAndSubject(uc.getConstraintName(), uc.getGroup(),
                    uc.getGroup1(), uc.getGroup2(), uc.getTeacher(), uc.getTeacher1(), uc.getTeacher2(), uc.getDay(), uc.getPeriod(), uc.getNumber(), uc.getSubject())) {
                universalConstraintRepository.save(uc);
            }
        });
        // Now save subjects and teachers based on the plan data
        saveTestSubjects(config);
        saveTestTeachers(config);
    }


    @Transactional
    public void saveTestSubjects(TestDataConfig config) {
        Set<String> addedSubjects = new HashSet<>();
        for (TestDataConfig.PlanData planData : config.getPlan()) {
            if (!addedSubjects.contains(planData.getSubject()) && !subjectRepository.existsByName(planData.getSubject())) {
                Subject subject = new Subject();
                subject.setName(planData.getSubject());
                subject.setTimesInAWeek(planData.getTimes_in_a_week());
                subjectRepository.save(subject);
                addedSubjects.add(planData.getSubject());
            }
        }
    }

    @Transactional
    public void saveTestTeachers(TestDataConfig config) {
        Set<String> addedTeachers = new HashSet<>();
        for (TestDataConfig.PlanData planData : config.getPlan()) {
            if (!addedTeachers.contains(planData.getTeacher()) && !userRepository.existsByFullName(planData.getTeacher())) {
                User user = new User();
                user.setFullName(planData.getTeacher());
                user.setEmail(planData.getTeacher() + "@university.com"); // Example email creation
                user.setPhone("1234567890"); // Example phone number
                user.setPassword("securePassword123"); // Example password
                user.setDateOfCreation(new Date()); // Current date and time
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new NotInDataBaseException("В базе данных отсутствует роль " + ERole.ROLE_USER));
                roles.add(userRole);
                Role teacherRole = roleRepository.findByName(ERole.ROLE_TEACHER)
                        .orElseThrow(() -> new NotInDataBaseException("В базе данных отсутствует роль " + ERole.ROLE_TEACHER));
                roles.add(teacherRole);
                user.setRoles(roles);
                userRepository.save(user);
                addedTeachers.add(planData.getTeacher());
            }
        }
    }

}
