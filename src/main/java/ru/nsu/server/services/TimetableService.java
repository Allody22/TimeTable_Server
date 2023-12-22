package ru.nsu.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.Group;
import ru.nsu.server.model.Operations;
import ru.nsu.server.model.Plan;
import ru.nsu.server.model.Room;
import ru.nsu.server.model.config.ConfigModel;
import ru.nsu.server.model.config.ConstraintModel;
import ru.nsu.server.model.config.PlanItem;
import ru.nsu.server.model.constraints.UniversalConstraint;
import ru.nsu.server.model.current.WeekTimetable;
import ru.nsu.server.model.potential.PotentialWeekTimetable;
import ru.nsu.server.repository.GroupRepository;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.repository.PlanRepository;
import ru.nsu.server.repository.PotentialWeekTimeTableRepository;
import ru.nsu.server.repository.RoomRepository;
import ru.nsu.server.repository.WeekTimeTableRepository;
import ru.nsu.server.repository.constraints.UniversalConstraintRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    private final WeekTimeTableRepository weekTimeTableRepository;

    private final PotentialWeekTimeTableRepository potentialWeekTimeTableRepository;

    private final RoomRepository roomRepository;

    private final PlanRepository planRepository;

    private final GroupRepository groupRepository;

    private final UniversalConstraintRepository universalConstraintRepository;

    private final OperationsRepository operationsRepository;

    @Autowired
    public TimetableService(WeekTimeTableRepository weekTimeTableRepository, PotentialWeekTimeTableRepository potentialWeekTimeTableRepository,
                            RoomRepository roomRepository, PlanRepository planRepository, OperationsRepository operationsRepository,
                            GroupRepository groupRepository, UniversalConstraintRepository universalConstraintRepository) {
        this.universalConstraintRepository = universalConstraintRepository;
        this.weekTimeTableRepository = weekTimeTableRepository;
        this.operationsRepository = operationsRepository;
        this.roomRepository = roomRepository;
        this.planRepository = planRepository;
        this.groupRepository = groupRepository;
        this.potentialWeekTimeTableRepository = potentialWeekTimeTableRepository;
    }

    public ConfigModel fillConfigFileUniversal() {
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

        List<UniversalConstraint> universalConstraints = universalConstraintRepository.findAll();
        List<ConstraintModel> constraintModelList = new ArrayList<>();

        if (!universalConstraints.isEmpty()) {
            for (var currentConstraint : universalConstraints) {
                ConstraintModel constraint = new ConstraintModel();
                constraint.setName(currentConstraint.getConstraintName());
                constraint.setArgs(Map.of(
                        "day", currentConstraint.getDay(),
                        "group", currentConstraint.getGroup(),
                        "group_1", currentConstraint.getGroup1(),
                        "group_2", currentConstraint.getGroup2(),
                        "number", currentConstraint.getNumber(),
                        "teacher", currentConstraint.getTeacher(),
                        "period", currentConstraint.getPeriod(),
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

        Files.writeString(Paths.get(filePath), json, Charset.forName("windows-1251"));

    }

    public void saveConfigToFile() {
        try {
            ConfigModel configModel = fillConfigFileUniversal();
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
                System.out.println("Чзх " + e);
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
        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        operations.setDescription("Потенциальное расписание превратилось в актуальное");
        operationsRepository.save(operations);
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
        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        operations.setDescription("Сохранено новое потенциальное расписание");
        operationsRepository.save(operations);
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
