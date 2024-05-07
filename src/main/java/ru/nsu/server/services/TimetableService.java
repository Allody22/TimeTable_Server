package ru.nsu.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.exception.ConflictChangesException;
import ru.nsu.server.exception.EmptyDataException;
import ru.nsu.server.exception.NotInDataBaseException;
import ru.nsu.server.model.config.ConfigModel;
import ru.nsu.server.model.config.PlanItem;
import ru.nsu.server.model.constraints.UniversalConstraint;
import ru.nsu.server.model.current.WeekTimetable;
import ru.nsu.server.model.dto.ConstraintModel;
import ru.nsu.server.model.potential.PotentialWeekTimetable;
import ru.nsu.server.model.study_plan.Group;
import ru.nsu.server.model.study_plan.Plan;
import ru.nsu.server.model.study_plan.Room;
import ru.nsu.server.model.user.Operations;
import ru.nsu.server.payload.requests.ChangeDayAndPairNumberAndRoomRequest;
import ru.nsu.server.payload.requests.ChangeDayAndPairNumberRequest;
import ru.nsu.server.payload.requests.ChangeRoomRequest;
import ru.nsu.server.payload.requests.ChangeTeacherRequest;
import ru.nsu.server.payload.response.FailureResponse;
import ru.nsu.server.repository.*;
import ru.nsu.server.repository.constraints.UniversalConstraintRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class TimetableService {

    private final WeekTimeTableRepository weekTimeTableRepository;

    private final PotentialWeekTimeTableRepository potentialWeekTimeTableRepository;

    private final RoomRepository roomRepository;

    private final PlanRepository planRepository;

    private final GroupRepository groupRepository;

    private final UniversalConstraintRepository universalConstraintRepository;

    private final OperationsRepository operationsRepository;

    private final UserRepository userRepository;

    @Transactional
    public String changeDayAndPairNumber(ChangeDayAndPairNumberRequest changeDayAndPairNumberRequest) {
        Long pairId = changeDayAndPairNumberRequest.getSubjectId();
        WeekTimetable foundedTimeTablePart = weekTimeTableRepository.findById(pairId)
                .orElseThrow(() -> new NotInDataBaseException("В базе данных отсутствует пара с айди " + pairId));

        int oldDayNumber = foundedTimeTablePart.getDayNumber();
        int oldPairNumber = foundedTimeTablePart.getPairNumber();
        String subjectName = foundedTimeTablePart.getSubjectName();
        String room = foundedTimeTablePart.getRoom();

        Integer newDayNumber = changeDayAndPairNumberRequest.getNewDayNumber();
        Integer newPairNumber = changeDayAndPairNumberRequest.getNewPairNumber();
        if (newDayNumber == oldDayNumber && newPairNumber == oldPairNumber) {
            throw new ConflictChangesException("При внесении изменений необходимо передать в запрос какие-то новые данные");
        }
        checkNewDayAndPairPeriod(newDayNumber, newPairNumber, foundedTimeTablePart);

        foundedTimeTablePart.setDayNumber(newDayNumber);
        foundedTimeTablePart.setPairNumber(newPairNumber);
        weekTimeTableRepository.save(foundedTimeTablePart);


        return String.format("Перенос пары %s в кабинете %s: с %s, %s на %s, %s.",
                subjectName, room,
                getDayName(oldDayNumber), getPairTime(oldPairNumber),
                getDayName(newDayNumber), getPairTime(newPairNumber));
    }

    @Transactional
    public String changeRoom(ChangeRoomRequest changeRoomRequest) {
        Long pairId = changeRoomRequest.getSubjectId();
        WeekTimetable foundedTimeTablePart = weekTimeTableRepository.findById(pairId)
                .orElseThrow(() -> new NotInDataBaseException("В базе данных отсутствует пара с айди " + pairId));

        String newRoomName = changeRoomRequest.getNewRoom();
        String oldRoom = foundedTimeTablePart.getRoom();
        if (Objects.equals(newRoomName, oldRoom)) {
            throw new ConflictChangesException("При внесении изменений необходимо передать в запрос какие-то новые данные");
        }
        checkNewRoom(newRoomName, foundedTimeTablePart);
        foundedTimeTablePart.setRoom(newRoomName);
        weekTimeTableRepository.save(foundedTimeTablePart);
        return String.format("Изменение кабинета пары %s в %s %s: с %s на %s.", foundedTimeTablePart.getSubjectName(), getDayName(foundedTimeTablePart.getDayNumber()), getPairTime(foundedTimeTablePart.getPairNumber()), oldRoom, newRoomName);
    }

    @Transactional
    public String changeTeacher(ChangeTeacherRequest changeTeacherRequest) {
        Long pairId = changeTeacherRequest.getSubjectId();
        WeekTimetable foundedTimeTablePart = weekTimeTableRepository.findById(pairId)
                .orElseThrow(() -> new NotInDataBaseException("В базе данных отсутствует пара с айди " + pairId));

        String newTeacher = changeTeacherRequest.getNewTeacherFullName();
        String oldTeacher = foundedTimeTablePart.getTeacher();
        checkNewTeacher(newTeacher, foundedTimeTablePart);

        if (Objects.equals(newTeacher, oldTeacher)) {
            throw new ConflictChangesException("При внесении изменений необходимо передать в запрос какие-то новые данные");
        }

        foundedTimeTablePart.setTeacher(newTeacher);
        weekTimeTableRepository.save(foundedTimeTablePart);
        return String.format("Изменение преподавателя пары %s в %s, %s: с %s на %s.", foundedTimeTablePart.getSubjectName(), getDayName(foundedTimeTablePart.getDayNumber()), getPairTime(foundedTimeTablePart.getPairNumber()), oldTeacher, newTeacher);
    }

    @Transactional
    public String changeDayAndPairNumberAndRoom(ChangeDayAndPairNumberAndRoomRequest ChangeDayAndPairNumberAndRoomRequest) {
        Long pairId = ChangeDayAndPairNumberAndRoomRequest.getSubjectId();
        WeekTimetable foundedTimeTablePart = weekTimeTableRepository.findById(pairId)
                .orElseThrow(() -> new NotInDataBaseException("В базе данных отсутствует пара с айди " + pairId));

        Integer newDayNumber = ChangeDayAndPairNumberAndRoomRequest.getNewDayNumber();
        Integer newPairNumber = ChangeDayAndPairNumberAndRoomRequest.getNewPairNumber();
        String newRoomName = ChangeDayAndPairNumberAndRoomRequest.getNewRoom();


        int oldDayNumber = foundedTimeTablePart.getDayNumber();
        int oldPairNumber = foundedTimeTablePart.getPairNumber();
        String oldRoom = foundedTimeTablePart.getRoom();

        if (newDayNumber == oldDayNumber && newPairNumber == oldPairNumber
                && Objects.equals(newRoomName, oldRoom)) {
            throw new ConflictChangesException("При внесении изменений необходимо передать в запрос какие-то новые данные");
        }
        //Время устанавливаем все данные в эту сущность, а если что-то пойдёт не так, то изменения просто откатятся
        foundedTimeTablePart.setDayNumber(newDayNumber);
        foundedTimeTablePart.setPairNumber(newPairNumber);
        foundedTimeTablePart.setRoom(newRoomName);

        //Сначала проверяем может ли быть существовать такое изменение по комнате, а потом по номеру дня и периоду
        checkNewRoom(newRoomName, foundedTimeTablePart);
        checkNewDayAndPairPeriod(newDayNumber, newPairNumber, foundedTimeTablePart);

        weekTimeTableRepository.save(foundedTimeTablePart);

        return String.format("Изменение дня и кабинета пары %s преподавателя %s: с %s, %s, %s на %s, %s, %s.",
                foundedTimeTablePart.getSubjectName(), foundedTimeTablePart.getTeacher(),
                getDayName(oldDayNumber), getPairTime(oldPairNumber), oldRoom,
                getDayName(newDayNumber), getPairTime(newPairNumber), newRoomName);
    }


    private void checkNewDayAndPairPeriod(Integer newDayNumber, Integer newPairNumber, WeekTimetable foundedTimeTablePart) {
        if (newDayNumber == null || newPairNumber == null) {
            throw new EmptyDataException("В ограничение на изменение дня и номера пары обязательно надо передать эти параметры!");
        }
        //Если преподаватель в этот день занят и в это время, то сразу лив
        Optional<List<WeekTimetable>> optionalListOfPairsByTeacher = weekTimeTableRepository.findByTeacherAndDayNumberAndPairNumber(foundedTimeTablePart.getTeacher(), newDayNumber, newPairNumber);
        if (optionalListOfPairsByTeacher.isPresent()) {
            for (var currentPair : optionalListOfPairsByTeacher.get()) {
                if (!currentPair.equals(foundedTimeTablePart)) {
                    throw new ConflictChangesException("Преподаватель '" + foundedTimeTablePart.getTeacher() + "' уже занят в это время на паре '" + currentPair.getSubjectName() + "'.");
                }
            }
        }

        Optional<List<WeekTimetable>> optionalListOfPairsByRoom = weekTimeTableRepository.findByDayNumberAndPairNumberAndRoom(newDayNumber, newPairNumber, foundedTimeTablePart.getRoom());
        //Если этот кабинет в этот день и пару занят то сразу лив
        if (optionalListOfPairsByRoom.isPresent()) {
            for (var currentPair : optionalListOfPairsByRoom.get()) {
                if (!currentPair.equals(foundedTimeTablePart)) {
                    throw new ConflictChangesException("В этом кабинете в этот день и период уже стоит пара '" + currentPair.getSubjectName() + "'.");
                }
            }
        }
    }

    private void checkNewTeacher(String newTeacher, WeekTimetable foundedTimeTablePart) {
        if (newTeacher == null || newTeacher.isBlank()) {
            throw new EmptyDataException("В ограничение на изменение комнаты обязательно надо передать комнату!");
        }
        if (!userRepository.existsByFullName(newTeacher)) {
            throw new ConflictChangesException("Преподавателя с данными '" + newTeacher + "' не существует в базе данных");
        }

        //Если у преподавателя уже
        Optional<List<WeekTimetable>> optionalListOfPairsByTeacher = weekTimeTableRepository.findByTeacherAndDayNumberAndPairNumber(newTeacher, foundedTimeTablePart.getDayNumber(), foundedTimeTablePart.getPairNumber());
        if (optionalListOfPairsByTeacher.isPresent()) {
            for (var currentPair : optionalListOfPairsByTeacher.get()) {
                if (!currentPair.equals(foundedTimeTablePart)) {
                    throw new ConflictChangesException("Преподаватель '" + foundedTimeTablePart.getTeacher() + "' уже занят в это время на паре '" + currentPair.getSubjectName() + "'.");
                }
            }
        }
    }

    private void checkNewRoom(String newRoomName, WeekTimetable foundedTimeTablePart) {
        if (newRoomName == null || newRoomName.isBlank()) {
            throw new EmptyDataException("В ограничение на изменение комнаты обязательно надо передать комнату!");
        }
        Room newRoomEntity = roomRepository.findRoomByName(newRoomName)
                .orElseThrow(() -> new NotInDataBaseException("В базе данных отсутствует комната с названием " + newRoomName));
        //Какая-то обработка вместимости, типа комнаты
        if (!newRoomEntity.getType().equals(foundedTimeTablePart.getPairType())) {
            throw new ConflictChangesException("Данный тип пары не соответствует типу новой комнаты");
        }

        Optional<List<WeekTimetable>> optionalListOfPairsByDayAndPairNumberAndRoom = weekTimeTableRepository.findByDayNumberAndPairNumberAndRoom(foundedTimeTablePart.getDayNumber(), foundedTimeTablePart.getPairNumber(), newRoomName);
        if (optionalListOfPairsByDayAndPairNumberAndRoom.isPresent()) {
            for (var currentPair : optionalListOfPairsByDayAndPairNumberAndRoom.get()) {
                if (!currentPair.equals(foundedTimeTablePart)) {
                    throw new ConflictChangesException("В этом '" + newRoomName + "' в этот день и период уже стоит пара '" + currentPair.getSubjectName() + "'.");
                }
            }
        }
    }

    private String getDayName(int dayNumber) {
        return switch (dayNumber) {
            case 1 -> "понедельник";
            case 2 -> "вторник";
            case 3 -> "среда";
            case 4 -> "четверг";
            case 5 -> "пятница";
            case 6 -> "суббота";
            case 7 -> "воскресенье";
            default -> "неизвестный день";
        };
    }

    private String getPairTime(int pairNumber) {
        String[] startTimes = {"09:00", "10:50", "12:40", "14:30", "16:20", "18:10", "20:00"};
        String[] endTimes = {"10:35", "12:25", "14:15", "16:05", "17:55", "19:45", "21:35"};

        if (pairNumber < 1 || pairNumber > 7) {
            return "неизвестное время";
        }

        return startTimes[pairNumber - 1] + " - " + endTimes[pairNumber - 1];
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
                        "teacher_2", currentConstraint.getTeacher2(),
                        "subject", currentConstraint.getSubject()
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

        String filePath = baseDir + "/TimeTable_Algo/src/resources/config_example.json";

        Files.writeString(Paths.get(filePath), json, Charset.forName("windows-1251"));

    }

    public void saveConfigToFile() {
        try {
            ConfigModel configModel = fillConfigFileUniversal();
            toJson(configModel);
        } catch (IOException e) {
            // Обработка ошибок ввода-вывода
            log.error("error with saving file to config: {}", e.getMessage());
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
                log.error("error converting group number: {}", group.getGroupNumber());
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

    public String convertFailureToJSON(String input) {
        List<FailureResponse> responses = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        // Регулярное выражение для извлечения данных об объектах FailureResponse
        Pattern pattern = Pattern.compile("FailureResponse\\((.*?)\\)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String[] properties = matcher.group(1).split(", ");
            FailureResponse response = new FailureResponse();

            for (String property : properties) {
                String[] keyValue = property.split("=");
                switch (keyValue[0]) {
                    case "teacher":
                        response.setTeacher(keyValue[1].equals("null") ? null : keyValue[1]);
                        break;
                    case "subject":
                        response.setSubject(keyValue[1].equals("null") ? null : keyValue[1]);
                        break;
                    case "subjectType":
                        response.setSubjectType(keyValue[1].equals("null") ? null : keyValue[1]);
                        break;
                    case "group":
                        response.setGroup(keyValue[1].equals("null") ? null : Integer.valueOf(keyValue[1]));
                        break;
                    case "timesInAWeek":
                        response.setTimesInAWeek(keyValue[1].equals("null") ? null : Integer.valueOf(keyValue[1]));
                        break;
                    case "day":
                        response.setDay(keyValue[1].equals("null") ? null : Integer.valueOf(keyValue[1]));
                        break;
                    case "period":
                        response.setPeriod(keyValue[1].equals("null") ? null : Integer.valueOf(keyValue[1]));
                        break;
                    case "room":
                        response.setRoom(keyValue[1].equals("null") ? null : Integer.valueOf(keyValue[1]));
                        break;
                }
            }
            responses.add(response);
        }

        // Сериализуем список объектов FailureResponse в JSON
        try {
            return mapper.writeValueAsString(responses);
        } catch (Exception e) {
            log.error("error with writing value as a string in converting failure to json method: {}", e.getMessage());
            return null;
        }
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
//        potentialWeekTimeTableRepository.deleteAll();
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
        return weekTimeTableRepository.getAllByExactGroup(group);
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
        return potentialWeekTimeTableRepository.getAllByExactGroup(group);
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
