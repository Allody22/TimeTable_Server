package ru.nsu.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.Group;
import ru.nsu.server.model.Plan;
import ru.nsu.server.model.Room;
import ru.nsu.server.model.config.ConfigModel;
import ru.nsu.server.model.config.ConstraintModel;
import ru.nsu.server.model.config.PlanItem;
import ru.nsu.server.model.constraints.ForbiddenDayForGroup;
import ru.nsu.server.model.constraints.ForbiddenDayForTeacher;
import ru.nsu.server.model.constraints.ForbiddenPeriodForGroup;
import ru.nsu.server.model.constraints.ForbiddenPeriodForTeacher;
import ru.nsu.server.model.constraints.GroupsOverlapping;
import ru.nsu.server.model.constraints.NumberOfTeachingDays;
import ru.nsu.server.model.constraints.TeachersOverlapping;
import ru.nsu.server.model.current.WeekTimetable;
import ru.nsu.server.model.potential.PotentialWeekTimetable;
import ru.nsu.server.repository.GroupRepository;
import ru.nsu.server.repository.PlanRepository;
import ru.nsu.server.repository.PotentialWeekTimeTableRepository;
import ru.nsu.server.repository.RoomRepository;
import ru.nsu.server.repository.SubjectRepository;
import ru.nsu.server.repository.UserRepository;
import ru.nsu.server.repository.WeekTimeTableRepository;
import ru.nsu.server.repository.constraints.ForbiddenDayForGroupRepository;
import ru.nsu.server.repository.constraints.ForbiddenDayForTeacherRepository;
import ru.nsu.server.repository.constraints.ForbiddenPeriodForGroupRepository;
import ru.nsu.server.repository.constraints.ForbiddenPeriodForTeacherRepository;
import ru.nsu.server.repository.constraints.GroupsOverlappingRepository;
import ru.nsu.server.repository.constraints.NumberOfTeachingDaysRepository;
import ru.nsu.server.repository.constraints.TeachersOverlappingRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    private final ForbiddenDayForGroupRepository forbiddenDayForGroupRepository;

    private final ForbiddenDayForTeacherRepository forbiddenDayForTeacherRepository;

    private final ForbiddenPeriodForGroupRepository forbiddenPeriodForGroupRepository;

    private final ForbiddenPeriodForTeacherRepository forbiddenPeriodForTeacherRepository;

    private final GroupsOverlappingRepository groupsOverlappingRepository;

    private final NumberOfTeachingDaysRepository numberOfTeachingDaysRepository;

    private final TeachersOverlappingRepository teachersOverlappingRepository;

    private final WeekTimeTableRepository weekTimeTableRepository;

    private final PotentialWeekTimeTableRepository potentialWeekTimeTableRepository;

    private final UserRepository userRepository;

    private final RoomRepository roomRepository;

    private final SubjectRepository subjectRepository;

    private final PlanRepository planRepository;

    private final GroupRepository groupRepository;

    @Autowired
    public TimetableService(WeekTimeTableRepository weekTimeTableRepository, PotentialWeekTimeTableRepository potentialWeekTimeTableRepository,
                            UserRepository userRepository, RoomRepository roomRepository, SubjectRepository subjectRepository, PlanRepository planRepository,
                            GroupRepository groupRepository, ForbiddenDayForTeacherRepository forbiddenDayForTeacherRepository, ForbiddenDayForGroupRepository forbiddenDayForGroupRepository,
                            ForbiddenPeriodForTeacherRepository forbiddenPeriodForTeacherRepository, ForbiddenPeriodForGroupRepository forbiddenPeriodForGroupRepository,
                            GroupsOverlappingRepository groupsOverlappingRepository, NumberOfTeachingDaysRepository numberOfTeachingDaysRepository,
                            TeachersOverlappingRepository teachersOverlappingRepository) {
        this.forbiddenDayForGroupRepository = forbiddenDayForGroupRepository;
        this.forbiddenDayForTeacherRepository = forbiddenDayForTeacherRepository;
        this.forbiddenPeriodForGroupRepository = forbiddenPeriodForGroupRepository;
        this.forbiddenPeriodForTeacherRepository = forbiddenPeriodForTeacherRepository;
        this.groupsOverlappingRepository = groupsOverlappingRepository;
        this.numberOfTeachingDaysRepository = numberOfTeachingDaysRepository;
        this.teachersOverlappingRepository = teachersOverlappingRepository;
        this.weekTimeTableRepository = weekTimeTableRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.subjectRepository = subjectRepository;
        this.planRepository = planRepository;
        this.groupRepository = groupRepository;
        this.potentialWeekTimeTableRepository = potentialWeekTimeTableRepository;
    }

    public ConfigModel fillConfigFile() {
        ConfigModel configModel = new ConfigModel();
        List<Room> rooms = roomRepository.getAll();
        List<Group> groups = groupRepository.getAll();
        List<Plan> plans = planRepository.findAll();

        List<PlanItem> planItems = convertPlansToList(plans);
        Map<String, List<Integer>> roomsMap = convertRoomsToMap(rooms);
        List<Integer> groupNumbers = convertGroupsToList(groups);

        configModel.setGroups(groupNumbers);
        configModel.setRooms(roomsMap);
        configModel.setPlan(planItems);

        List<ConstraintModel> constraintModelList = new ArrayList<>();
        List<ForbiddenDayForGroup> forbiddenDaysForGroups = forbiddenDayForGroupRepository.findAll();
        if (!forbiddenDaysForGroups.isEmpty()) {
            for (var currentConstraint : forbiddenDaysForGroups) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName("forbidden_day_for_group");
                constraint.setArgs(Map.of(
                        "day", currentConstraint.getDay(),
                        "group", currentConstraint.getGroup()
                ));
                constraintModelList.add(constraint);
            }
        }
        List<ForbiddenDayForTeacher> forbiddenDayForTeachers = forbiddenDayForTeacherRepository.findAll();
        if (!forbiddenDayForTeachers.isEmpty()) {
            for (var currentConstraint : forbiddenDayForTeachers) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName("forbidden_day_for_teacher");
                constraint.setArgs(Map.of(
                        "day", currentConstraint.getDay(),
                        "teacher", currentConstraint.getTeacher()
                ));
                constraintModelList.add(constraint);
            }
        }

        List<ForbiddenPeriodForGroup> forbiddenPeriodForGroups = forbiddenPeriodForGroupRepository.findAll();
        if (!forbiddenPeriodForGroups.isEmpty()) {
            for (var currentConstraint : forbiddenPeriodForGroups) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName("forbidden_period_for_group");
                constraint.setArgs(Map.of(
                        "day", currentConstraint.getDay(),
                        "period", currentConstraint.getPeriod(),
                        "group", currentConstraint.getGroup()
                ));
                constraintModelList.add(constraint);
            }
        }

        List<ForbiddenPeriodForTeacher> forbiddenPeriodForTeachers = forbiddenPeriodForTeacherRepository.findAll();
        if (!forbiddenPeriodForTeachers.isEmpty()) {
            for (var currentConstraint : forbiddenPeriodForTeachers) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName("forbidden_period_for_teacher");
                constraint.setArgs(Map.of(
                        "day", currentConstraint.getDay(),
                        "period", currentConstraint.getPeriod(),
                        "teacher", currentConstraint.getTeacher()
                ));
                constraintModelList.add(constraint);
            }
        }

        List<GroupsOverlapping> groupsOverlapping = groupsOverlappingRepository.findAll();
        if (!groupsOverlapping.isEmpty()) {
            for (var currentConstraint : groupsOverlapping) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName("groups_overlapping");
                constraint.setArgs(Map.of(
                        "group_1", currentConstraint.getGroup1(),
                        "group_2", currentConstraint.getGroup2()
                ));
                constraintModelList.add(constraint);
            }
        }

        List<NumberOfTeachingDays> numberOfTeachingDays = numberOfTeachingDaysRepository.findAll();
        if (!numberOfTeachingDays.isEmpty()) {
            for (var currentConstraint : numberOfTeachingDays) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName("number_of_teaching_days");
                constraint.setArgs(Map.of(
                        "teacher", currentConstraint.getTeacher(),
                        "number", currentConstraint.getNumber()
                ));
                constraintModelList.add(constraint);
            }
        }

        List<TeachersOverlapping> teachersOverlapping = teachersOverlappingRepository.findAll();
        if (!teachersOverlapping.isEmpty()) {
            for (var currentConstraint : teachersOverlapping) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName("teachers_overlapping");
                constraint.setArgs(Map.of(
                        "teacher_1", currentConstraint.getTeacher1(),
                        "teacher_2", currentConstraint.getTeacher2()
                ));
                constraintModelList.add(constraint);
            }
        }

        configModel.setConstraints(constraintModelList);
        return configModel;
    }

    public static void toJson(ConfigModel configModel) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(configModel);

        String baseDir = System.getProperty("user.dir");

        String filePath = baseDir + "/Algo/my_config_example.json";

        Files.writeString(Paths.get(filePath), json);
    }

    public void saveConfigToFile() {
        try {
            ConfigModel configModel = fillConfigFile();
            toJson(configModel);
        } catch (IOException e) {
            // Обработка ошибок ввода-вывода
            e.printStackTrace();
        }
    }

    public Map<String, List<Integer>> convertRoomsToMap(List<Room> rooms) {
        Map<String, List<Integer>> roomsMap = new HashMap<>();

        for (Room room : rooms) {
            String type = room.getType();

            // Добавляем новый список для типа комнаты, если он еще не существует
            roomsMap.putIfAbsent(type, new ArrayList<>());

            // Получаем список идентификаторов для данного типа комнаты и добавляем в него текущий идентификатор
            roomsMap.get(type).add(Integer.parseInt(room.getName()));
        }

        return roomsMap;
    }

    public List<Integer> convertGroupsToList(List<Group> groups) {
        List<Integer> groupNumbers = new ArrayList<>();

        for (Group group : groups) {
            try {
                int groupNumber = Integer.parseInt(group.getGroupNumber());
                groupNumbers.add(groupNumber);
            } catch (NumberFormatException e) {
                // Например, залогировать ошибку или пропустить эту группу
            }
        }

        return groupNumbers;
    }

    public List<PlanItem> convertPlansToList(List<Plan> plans) {
        List<PlanItem> planItems = new ArrayList<>();

        for (Plan plan : plans) {
            PlanItem planItem = new PlanItem();
            planItem.setTeacher(plan.getTeacher());
            planItem.setSubject(plan.getSubject());
            planItem.setSubject_type(plan.getSubjectType());

            List<Integer> groupNumbers = Arrays.stream(plan.getGroups().split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            planItem.setGroups(groupNumbers);

            planItem.setTimes_in_a_week(plan.getTimesInAWeek());
            planItems.add(planItem);
        }

        return planItems;
    }

    @Transactional
    public void convertOptionalTimeTableToActual() {
        var potentialTimeTable = potentialWeekTimeTableRepository.findAll();
        weekTimeTableRepository.deleteAll();
        for (var subject : potentialTimeTable) {
            WeekTimetable weekTimetable = new WeekTimetable();
            weekTimetable.setDayNumber(subject.getDayNumber());
            weekTimetable.setSubjectName(subject.getSubjectName());
            weekTimetable.setGroups(subject.getGroups());
            weekTimetable.setTeacher(subject.getTeacher());
            weekTimetable.setFaculty(subject.getFaculty());
            weekTimetable.setCourse(subject.getCourse());
            weekTimetable.setPairNumber(subject.getPairNumber());
            weekTimetable.setRoom(subject.getRoom());
            weekTimetable.setStartTime(subject.getStartTime());
            weekTimetable.setEndTime(subject.getEndTime());
            weekTimetable.setPairType(subject.getPairType());
            weekTimeTableRepository.save(weekTimetable);
        }
        potentialWeekTimeTableRepository.deleteAll();
    }

    @Transactional
    public void saveNewPotentialTimeTable(List<String> newTimeTableList) {
        potentialWeekTimeTableRepository.deleteAll();
        for (String line : newTimeTableList) {
            line = line.substring(2, line.length() - 2);
            String[] mainParts = line.split("\\), \\[");

            String details = mainParts[0];
            String[] parts = details.split(", ");

            String teacher = parts[0].replaceAll("[()']", "").trim();

            String subjectWithType = parts[1].replaceAll("[()']", "").trim();
            int lastIndex = subjectWithType.lastIndexOf('_');
            String subjectName = subjectWithType.substring(0, lastIndex).replace("_", " ");
            String subjectType = subjectWithType.substring(lastIndex + 1);

            // Обработка дня и номера пары
            int day = Integer.parseInt(parts[2].trim());
            int pairNumber = Integer.parseInt(parts[3].trim());

            // Обработка номера аудитории
            String roomStr = parts[4].replaceAll("[()']", "").trim();
            int room = Integer.parseInt(roomStr);

            // Обработка групп
            String groupsPart = mainParts[1].replaceAll("[\\]]", "").trim();
            String[] groups = groupsPart.split(", ");
            String groupStr = String.join(",", groups);

            PotentialWeekTimetable potentialWeekTimetable = new PotentialWeekTimetable();
            potentialWeekTimetable.setTeacher(teacher);
            potentialWeekTimetable.setSubjectName(subjectName);
            potentialWeekTimetable.setPairType(subjectType);
            potentialWeekTimetable.setDayNumber(day);
            potentialWeekTimetable.setPairNumber(pairNumber);
            potentialWeekTimetable.setRoom(Integer.toString(room));
            potentialWeekTimetable.setGroups(groupStr);

            potentialWeekTimeTableRepository.save(potentialWeekTimetable);
        }
    }

    @Transactional
    public void saveNewActualTimeTable(List<String> newTimeTableList) {
        weekTimeTableRepository.deleteAll();
        for (String line : newTimeTableList) {
            line = line.substring(2, line.length() - 2);
            String[] mainParts = line.split("\\), \\[");

            String details = mainParts[0];
            String[] parts = details.split(", ");

            String teacher = parts[0].replaceAll("[()']", "").trim();

            String subjectWithType = parts[1].replaceAll("[()']", "").trim();
            int lastIndex = subjectWithType.lastIndexOf('_');
            String subjectName = subjectWithType.substring(0, lastIndex).replace("_", " ");
            String subjectType = subjectWithType.substring(lastIndex + 1);

            // Обработка дня и номера пары
            int day = Integer.parseInt(parts[2].trim());
            int pairNumber = Integer.parseInt(parts[3].trim());

            // Обработка номера аудитории
            String roomStr = parts[4].replaceAll("[()']", "").trim();
            int room = Integer.parseInt(roomStr);

            // Обработка групп
            String groupsPart = mainParts[1].replaceAll("[\\]]", "").trim();
            String[] groups = groupsPart.split(", ");
            String groupStr = String.join(",", groups);

            WeekTimetable weekTimetable = new WeekTimetable();
            weekTimetable.setTeacher(teacher);
            weekTimetable.setSubjectName(subjectName);
            weekTimetable.setPairType(subjectType);
            weekTimetable.setDayNumber(day);
            weekTimetable.setPairNumber(pairNumber);
            weekTimetable.setRoom(Integer.toString(room));
            weekTimetable.setGroups(groupStr);

            weekTimeTableRepository.save(weekTimetable);
        }
    }

    public List<WeekTimetable> getAllTimeTable() {
        return weekTimeTableRepository.findAll();
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

    public List<PotentialWeekTimetable> getAllPotentialTimeTable() {
        return potentialWeekTimeTableRepository.findAll();
    }

    public List<PotentialWeekTimetable> getPotentialGroupTimetable(String group) {
        return potentialWeekTimeTableRepository.getAllByGroupsContaining(group);
    }

    public List<PotentialWeekTimetable> getPotentialTeacherTimetable(String teacher) {
        return potentialWeekTimeTableRepository.getAllByTeacher(teacher);
    }

    public List<PotentialWeekTimetable> getPotentialFacultyTimetable(String faculty) {
        return potentialWeekTimeTableRepository.getAllByFacultyContaining(faculty);
    }

    public List<PotentialWeekTimetable> getPotentialRoomTimetable(String room) {
        return potentialWeekTimeTableRepository.getPotentialWeekTimetablesByRoom(room);
    }
}
