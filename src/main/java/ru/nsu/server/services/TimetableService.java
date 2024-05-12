package ru.nsu.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.exception.ConflictChangesException;
import ru.nsu.server.exception.EmptyDataException;
import ru.nsu.server.exception.NotInDataBaseException;
import ru.nsu.server.model.actual.WeekTimetable;
import ru.nsu.server.model.config.ConfigModel;
import ru.nsu.server.model.config.PlanItem;
import ru.nsu.server.model.constants.ERole;
import ru.nsu.server.model.constraints.UniversalConstraint;
import ru.nsu.server.model.dto.ConstraintModel;
import ru.nsu.server.model.operations.ActualTimetableLogs;
import ru.nsu.server.model.potential.PotentialWeekTimetable;
import ru.nsu.server.model.study_plan.Group;
import ru.nsu.server.model.study_plan.Plan;
import ru.nsu.server.model.study_plan.Room;
import ru.nsu.server.model.study_plan.Subject;
import ru.nsu.server.model.user.Operations;
import ru.nsu.server.model.user.Role;
import ru.nsu.server.model.user.User;
import ru.nsu.server.payload.requests.ChangeDayAndPairNumberAndRoomRequest;
import ru.nsu.server.payload.requests.ChangeDayAndPairNumberRequest;
import ru.nsu.server.payload.requests.ChangeRoomRequest;
import ru.nsu.server.payload.requests.ChangeTeacherRequest;
import ru.nsu.server.payload.response.FailureResponse;
import ru.nsu.server.repository.*;
import ru.nsu.server.repository.constraints.UniversalConstraintRepository;
import ru.nsu.server.repository.logs.ActualTimetableLogsRepository;

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
    private final ActualTimetableLogsRepository actualTimetableLogsRepository;
    private final SubjectRepository subjectRepository;
    private final RoleRepository roleRepository;

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


        String description = String.format("Перенос пары %s преподавателя %s в кабинете %s: %s, %s на %s, %s.",
                subjectName, foundedTimeTablePart.getTeacher(), room,
                getDayNameFromChange(oldDayNumber), getPairTime(oldPairNumber),
                getDayNameToChange(newDayNumber), getPairTime(newPairNumber));

        ActualTimetableLogs currentLog = new ActualTimetableLogs();
        currentLog.setDescription(description);
        currentLog.setDateOfCreation(new Date());
        currentLog.setUserAccount("admin");
        currentLog.setOperationName("/day_and_pair_number");
        currentLog.setSubjectId(pairId);
        currentLog.setNewDayNumber(newDayNumber);
        currentLog.setNewPairNumber(newPairNumber);
        currentLog.setNewRoom(foundedTimeTablePart.getRoom());
        currentLog.setNewTeacherFullName(foundedTimeTablePart.getTeacher());

        currentLog.setOldPairNumber(foundedTimeTablePart.getPairNumber());
        currentLog.setOldRoom(foundedTimeTablePart.getRoom());
        currentLog.setOldDayNumber(foundedTimeTablePart.getDayNumber());
        currentLog.setOldTeacherFullName(foundedTimeTablePart.getTeacher());
        actualTimetableLogsRepository.save(currentLog);

        return description;
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

        String description = String.format("Изменение кабинета пары %s в %s %s: с %s на %s.", foundedTimeTablePart.getSubjectName(),
                getDayNameToChange(foundedTimeTablePart.getDayNumber()), getPairTime(foundedTimeTablePart.getPairNumber()), oldRoom, newRoomName);

        ActualTimetableLogs currentLog = new ActualTimetableLogs();
        currentLog.setDescription(description);
        currentLog.setDateOfCreation(new Date());
        currentLog.setUserAccount("admin");
        currentLog.setOperationName("/room");
        currentLog.setSubjectId(pairId);
        currentLog.setNewDayNumber(foundedTimeTablePart.getDayNumber());
        currentLog.setNewPairNumber(foundedTimeTablePart.getPairNumber());
        currentLog.setNewRoom(newRoomName);
        currentLog.setNewTeacherFullName(foundedTimeTablePart.getTeacher());

        currentLog.setOldPairNumber(foundedTimeTablePart.getPairNumber());
        currentLog.setOldRoom(foundedTimeTablePart.getRoom());
        currentLog.setOldDayNumber(foundedTimeTablePart.getDayNumber());
        currentLog.setOldTeacherFullName(foundedTimeTablePart.getTeacher());
        actualTimetableLogsRepository.save(currentLog);
        return description;
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

        String description = String.format("Изменение преподавателя пары %s в %s, %s: с %s на %s.", foundedTimeTablePart.getSubjectName(), getDayNameToChange(foundedTimeTablePart.getDayNumber()), getPairTime(foundedTimeTablePart.getPairNumber()), oldTeacher, newTeacher);

        ActualTimetableLogs currentLog = new ActualTimetableLogs();
        currentLog.setDescription(description);
        currentLog.setDateOfCreation(new Date());
        currentLog.setUserAccount("admin");
        currentLog.setOperationName("/teacher");
        currentLog.setSubjectId(pairId);
        currentLog.setNewDayNumber(foundedTimeTablePart.getDayNumber());
        currentLog.setNewPairNumber(foundedTimeTablePart.getPairNumber());
        currentLog.setNewRoom(foundedTimeTablePart.getRoom());
        currentLog.setNewTeacherFullName(newTeacher);

        currentLog.setOldPairNumber(foundedTimeTablePart.getPairNumber());
        currentLog.setOldRoom(foundedTimeTablePart.getRoom());
        currentLog.setOldDayNumber(foundedTimeTablePart.getDayNumber());
        currentLog.setOldTeacherFullName(foundedTimeTablePart.getTeacher());
        actualTimetableLogsRepository.save(currentLog);
        return description;
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

        String description = String.format("Изменение дня, времени и кабинета пары %s преподавателя %s: %s, %s, %s на %s, %s, %s.",
                foundedTimeTablePart.getSubjectName(), foundedTimeTablePart.getTeacher(),
                getDayNameFromChange(oldDayNumber), getPairTime(oldPairNumber), oldRoom,
                getDayNameToChange(newDayNumber), getPairTime(newPairNumber), newRoomName);

        ActualTimetableLogs currentLog = new ActualTimetableLogs();
        currentLog.setDescription(description);
        currentLog.setDateOfCreation(new Date());
        currentLog.setUserAccount("admin");
        currentLog.setOperationName("/day_and_pair_number_and_room");

        currentLog.setSubjectId(pairId);
        currentLog.setNewDayNumber(newDayNumber);
        currentLog.setNewPairNumber(newPairNumber);
        currentLog.setNewRoom(newRoomName);
        currentLog.setNewTeacherFullName(foundedTimeTablePart.getTeacher());


        currentLog.setOldPairNumber(foundedTimeTablePart.getPairNumber());
        currentLog.setOldRoom(foundedTimeTablePart.getRoom());
        currentLog.setOldDayNumber(foundedTimeTablePart.getDayNumber());
        currentLog.setOldTeacherFullName(foundedTimeTablePart.getTeacher());
        actualTimetableLogsRepository.save(currentLog);
        return description;

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

    private String getDayNameToChange(int dayNumber) {
        return switch (dayNumber) {
            case 1 -> "понедельник";
            case 2 -> "вторник";
            case 3 -> "среду";
            case 4 -> "четверг";
            case 5 -> "пятницу";
            case 6 -> "субботу";
            case 7 -> "воскресенье";
            default -> "неизвестный день";
        };
    }

    private String getDayNameFromChange(int dayNumber) {
        return switch (dayNumber) {
            case 1 -> "с понедельника";
            case 2 -> "со вторника";
            case 3 -> "со среды";
            case 4 -> "с четверга";
            case 5 -> "с пятницы";
            case 6 -> "с субботы";
            case 7 -> "с воскресенья";
            default -> "неизвестный день";
        };
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

    public ConfigModel fillConfigFileUniversal(List<ConstraintModel> newConstraints) {
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
                List<Integer> groupsList = Optional.ofNullable(currentConstraint.getGroups())
                        .map(groupsForTest -> Arrays.stream(groupsForTest.split(","))
                                .map(Integer::parseInt)
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());  // Handles null groups

                String subject = null;
                if (currentConstraint.getSubject() != null && currentConstraint.getSubjectType() != null){
                    subject = currentConstraint.getSubject().replaceAll(" ","_") + "_" + currentConstraint.getSubjectType();
                }
                constraint.setArgs(Map.ofEntries(
                        Map.entry("day", Optional.ofNullable(currentConstraint.getDay()).orElse(0)),
                        Map.entry("group", Optional.ofNullable(currentConstraint.getGroup()).orElse(0)),
                        Map.entry("group_1", Optional.ofNullable(currentConstraint.getGroup1()).orElse(0)),
                        Map.entry("group_2", Optional.ofNullable(currentConstraint.getGroup2()).orElse(0)),
                        Map.entry("number", Optional.ofNullable(currentConstraint.getNumber()).orElse(0)),
                        Map.entry("teacher", Optional.ofNullable(currentConstraint.getTeacher()).orElse("")),
                        Map.entry("period", Optional.ofNullable(currentConstraint.getPeriod()).orElse(0)),
                        Map.entry("teacher_1", Optional.ofNullable(currentConstraint.getTeacher1()).orElse("")),
                        Map.entry("teacher_2", Optional.ofNullable(currentConstraint.getTeacher2()).orElse("")),
                        Map.entry("subject", Optional.ofNullable(subject).orElse("")),
                        Map.entry("groups", groupsList),
                        Map.entry("room", Optional.ofNullable(currentConstraint.getRoom()).map(Integer::parseInt).orElse(0))  // Default to 0 if null
                ));

                constraintModelList.add(constraint);
            }
        }


        if (newConstraints != null && !newConstraints.isEmpty()) {
            constraintModelList.addAll(newConstraints);
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
            ConfigModel configModel = fillConfigFileUniversal(null);
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
    public void saveTestData() {
        String[] groupsList = new String[]{"21201", "21213", "21214", "21215", "21216"};
        for (String group : groupsList) {
            if (!groupRepository.existsByGroupNumber(group)) {
                Group g = new Group();
                g.setGroupNumber(group);
                g.setFaculty("ФИТ");
                g.setCourse(1);
                g.setStudentsNumber(40);
                groupRepository.save(g);
            }
        }
        String[] lecRoomsList = new String[]{"118", "304", "402", "1274", "2128", "3107"};
        for (String lecRoom : lecRoomsList) {
            if (!roomRepository.existsByName(lecRoom)) {
                Room r = new Room();
                r.setName(lecRoom);
                r.setCapacity(100);
                r.setType("lec");
                roomRepository.save(r);
            }
        }
        String[] labRoomsList = new String[]{"306", "1156", "2213", "2221", "3212", "4218"};
        for (String labRoom : labRoomsList) {
            if (!roomRepository.existsByName(labRoom)) {
                Room r = new Room();
                r.setName(labRoom);
                r.setCapacity(100);
                r.setType("lab");
                roomRepository.save(r);

            }
        }

        String[] pracRoomsList = new String[]{"1155", "1156", "1311", "1326", "2253", "2266", "3120", "3212", "3218", "3317", "4220"};
        for (String pracRoom : pracRoomsList) {
            if (!roomRepository.existsByName(pracRoom)) {
                Room r = new Room();
                r.setName(pracRoom);
                r.setCapacity(100);
                r.setType("prac");
                roomRepository.save(r);

            }
        }
        saveTestSubjects();
        saveTestTeachers();
        saveTestPlans();
        saveConstraints();
    }


    @Transactional
    public void saveConstraints() {
        List<Map<String, Object>> constraints = Arrays.asList(
                Map.of("name", "teachers_overlapping", "args", Map.of("teacher_1", "permyakov", "teacher_2", "nedelko")),
                // Добавьте другие ограничения по аналогии
                Map.of("name", "teachers_overlapping", "args", Map.of("teacher_1", "balabanov", "teacher_2", "terenteva")),
                Map.of("name", "teachers_overlapping", "args", Map.of("teacher_1", "balabanov", "teacher_2", "sitnov")),
                Map.of("name", "teachers_overlapping", "args", Map.of("teacher_1", "terenteva", "teacher_2", "vaskevich")),
                Map.of("name", "number_of_teaching_days", "args", Map.of("teacher", "bukshev", "number", 1)),
                Map.of("name", "number_of_teaching_days", "args", Map.of("teacher", "terenteva", "number", 1)),
                Map.of("name", "number_of_teaching_days", "args", Map.of("teacher", "vaskevich", "number", 1)),
                Map.of("name", "number_of_teaching_days", "args", Map.of("teacher", "balabanov", "number", 1)),
                Map.of("name", "number_of_teaching_days", "args", Map.of("teacher", "kuzin", "number", 1)),
                Map.of("name", "forbidden_period_for_teacher", "args", Map.of("day", 1, "period", 1, "teacher", "balabanov")),
                Map.of("name", "forbidden_period_for_group", "args", Map.of("day", 6, "period", 7, "group", 21215)),
                Map.of("name", "forbidden_day_for_teacher", "args", Map.of("day", 2, "teacher", "balabanov")),
                Map.of("name", "forbidden_day_for_group", "args", Map.of("day", 3, "group", 21215)),
                Map.of("name", "exact_time", "args", Map.of("teacher", "nedelko", "subject", "machine_learning_methods", "day", 5, "period", 5, "room", 4220, "groups", List.of(21213), "subjectType", "prac")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 1, "period", 7, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 1, "period", 1, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 1, "period", 2, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 1, "period", 3, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 1, "period", 4, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 1, "period", 5, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 1, "period", 6, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 2, "period", 1, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "fizruk", "subject", "physical_culture", "day", 2, "period", 2, "room", 1274, "groups", List.of(21215, 21214, 21216, 21213), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "sitnov", "subject", "ctf", "day", 2, "period", 4, "room", 304, "groups", List.of(21213, 21214, 21215, 21216), "subjectType", "lec")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "zhidkov", "subject", "databases", "day", 1, "period", 2, "room", 2221, "groups", List.of(21201), "subjectType", "lab")),
                Map.of("name", "exact_time", "args", Map.of("teacher", "terenteva", "subject", "english", "day", 4, "period", 1, "room", 2266, "groups", List.of(21213), "subjectType", "prac"))
        );

        for (Map<String, Object> constraint : constraints) {
            String name = (String) constraint.get("name");
            Map<String, Object> args = (Map<String, Object>) constraint.get("args");

            UniversalConstraint uc = new UniversalConstraint();
            uc.setConstraintName(name);
            uc.setDateOfCreation(new Date()); // Установите текущую дату и время

            switch (name) {
                case "teachers_overlapping":
                    uc.setTeacher1((String) args.get("teacher_1"));
                    uc.setTeacher2((String) args.get("teacher_2"));
                    uc.setConstraintNameRu("Перегруз учителей (?)");
                    break;
                case "number_of_teaching_days":
                    uc.setTeacher((String) args.get("teacher"));
                    uc.setNumber((Integer) args.get("number"));
                    uc.setConstraintNameRu("Максимальное кол-во рабочих дней");

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
                    uc.setConstraintNameRu("Запрещенные порядковый номер пары для групп в определённый день");
                    break;
                case "forbidden_day_for_teacher":
                case "forbidden_day_for_group":
                    uc.setDay((Integer) args.get("day"));
                    if (args.containsKey("teacher")) {
                        uc.setTeacher((String) args.get("teacher"));
                    } else {
                        uc.setGroup((Integer) args.get("group"));
                    }
                    uc.setConstraintNameRu("Запрещенный день для преподавания для группы");

                    break;
                case "exact_time":
                    uc.setTeacher((String) args.get("teacher"));
                    uc.setSubject((String) args.get("subject"));
                    uc.setSubjectType((String) args.get("subjectType"));
                    uc.setDay((Integer) args.get("day"));
                    uc.setPeriod((Integer) args.get("period"));
                    uc.setRoom(args.get("room").toString());


                    @SuppressWarnings("unchecked")
                    List<Integer> groups = (List<Integer>) args.get("groups");
                    String groupString = groups.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                    uc.setGroups(groupString); // Используйте group как строку или другое поле для списка групп
                    uc.setConstraintNameRu("Обязательное время пары");

                    break;
            }

            if (!universalConstraintRepository.existsByConstraintNameAndGroupAndGroup1AndGroup2AndTeacherAndTeacher1AndTeacher2AndDayAndPeriodAndNumberAndSubject(uc.getConstraintName(), uc.getGroup(),
                    uc.getGroup1(), uc.getGroup2(), uc.getTeacher(), uc.getTeacher1(), uc.getTeacher2(), uc.getDay(), uc.getPeriod(), uc.getNumber(), uc.getSubject())) {
                universalConstraintRepository.save(uc);
            }
        }
    }

    @Transactional
    public void saveTestPlans() {
        List<Map<String, Object>> plans = Arrays.asList(
                Map.of("teacher", "maryasov", "subject", "methods_of_translation_and_compilation", "subjectType", "lec", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "gorchakov", "subject", "electronics", "subjectType", "lec", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "gorchakov", "subject", "electronics", "subjectType", "lab", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "ostapenko", "subject", "computational_math", "subjectType", "prac", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "ostapenko", "subject", "computational_math", "subjectType", "lec", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "psychology", "subject", "computational_math", "subjectType", "lec", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "andreev", "subject", "networks", "subjectType", "lab", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "ippolitov", "subject", "networks", "subjectType", "lec", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "grishkin", "subject", "operating_systems", "subjectType", "lab", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "pischik", "subject", "databases", "subjectType", "lec", "groups", List.of(21201), "timesInAWeek", 1),
                // Добавьте все остальные планы, используя тот же формат
                Map.of("teacher", "rutman", "subject", "operating_systems", "subjectType", "lec", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "zligostev", "subject", "methods_of_translation_and_compilation", "subjectType", "prac", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "zhidkov", "subject", "databases", "subjectType", "lab", "groups", List.of(21201), "timesInAWeek", 1),
                Map.of("teacher", "permyakov", "subject", "cybersecurity", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 1),
                Map.of("teacher", "nedelko", "subject", "machine_learning_methods", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 1),
                Map.of("teacher", "nedelko", "subject", "machine_learning_methods", "subjectType", "prac", "groups", List.of(21213), "timesInAWeek", 1),
                Map.of("teacher", "vaskevich", "subject", "computational_math", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 1),
                Map.of("teacher", "vaskevich", "subject", "computational_math", "subjectType", "prac", "groups", List.of(21213), "timesInAWeek", 1),
                Map.of("teacher", "patrushev", "subject", "data_processing_and_storage", "subjectType", "lab", "groups", List.of(21215), "timesInAWeek", 1),
                Map.of("teacher", "bukshev", "subject", "introduction_to_the_mobile_development", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 1),
                Map.of("teacher", "miginsky", "subject", "data_processing_and_storage", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 1),
                Map.of("teacher", "miginsky", "subject", "software_engineering", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 1),
                Map.of("teacher", "miginsky", "subject", "software_engineering", "subjectType", "lab", "groups", List.of(21213), "timesInAWeek", 1),
                Map.of("teacher", "kuzin", "subject", "software_engineering", "subjectType", "lab", "groups", List.of(21215), "timesInAWeek", 1),
                Map.of("teacher", "vlasov", "subject", "team_work", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 1),
                Map.of("teacher", "vlasov", "subject", "team_work", "subjectType", "prac", "groups", List.of(21213), "timesInAWeek", 1),
                Map.of("teacher", "balabanov", "subject", "cybersecurity", "subjectType", "lab", "groups", List.of(21215, 21213, 21214, 21216), "timesInAWeek", 1),
                Map.of("teacher", "bogdanov", "subject", "computational_math", "subjectType", "prac", "groups", List.of(21215), "timesInAWeek", 1),
                Map.of("teacher", "terenteva", "subject", "english", "subjectType", "prac", "groups", List.of(21213, 21215), "timesInAWeek", 2),
                Map.of("teacher", "sitnov", "subject", "ctf", "subjectType", "lec", "groups", List.of(21213, 21215, 21214, 21216), "timesInAWeek", 2),
                Map.of("teacher", "zligostev", "subject", "data_processing_and_storage", "subjectType", "lab", "groups", List.of(21213), "timesInAWeek", 1),
                Map.of("teacher", "kutalev", "subject", "data_processing_and_storage", "subjectType", "lab", "groups", List.of(21214), "timesInAWeek", 1),
                Map.of("teacher", "shvab", "subject", "computational_math", "subjectType", "prac", "groups", List.of(21214), "timesInAWeek", 1),
                Map.of("teacher", "golosov", "subject", "software_engineering", "subjectType", "lab", "groups", List.of(21214), "timesInAWeek", 1),
                Map.of("teacher", "brek", "subject", "software_engineering", "subjectType", "lab", "groups", List.of(21216), "timesInAWeek", 1),
                Map.of("teacher", "plusnin", "subject", "data_processing_and_storage", "subjectType", "lab", "groups", List.of(21216), "timesInAWeek", 1),
                Map.of("teacher", "levikin", "subject", "computational_math", "subjectType", "prac", "groups", List.of(21216), "timesInAWeek", 1),
                Map.of("teacher", "fizruk", "subject", "physical_culture", "subjectType", "lec", "groups", List.of(21213, 21214, 21215, 21216), "timesInAWeek", 10)
        );

        for (Map<String, Object> planData : plans) {
            String teacherUsername = (String) planData.get("teacher");
            String subjectName = (String) planData.get("subject");
            String subjectType = (String) planData.get("subjectType");
            @SuppressWarnings("unchecked")
            List<Integer> groups = (List<Integer>) planData.get("groups");
            Integer timesInAWeek = (Integer) planData.get("timesInAWeek");

            String groupString = groups.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));

            if (!planRepository.existsByTeacherAndSubjectAndSubjectTypeAndGroupsAndTimesInAWeek(
                    teacherUsername, subjectName, subjectType, groupString, timesInAWeek)) {
                Plan plan = new Plan();
                plan.setTeacher(teacherUsername);
                plan.setSubject(subjectName);
                plan.setSubjectType(subjectType);
                plan.setGroups(groupString); // Сохранение списка групп как строки с разделением запятыми
                plan.setTimesInAWeek(timesInAWeek);

                planRepository.save(plan);
            }
        }
    }

    @Transactional
    public void saveTestSubjects() {
        List<Map<String, Object>> subjects = Arrays.asList(
                // Здесь представлены данные предметов, каждый предмет описан в форме Map
                Map.of("name", "methods_of_translation_and_compilation", "timesInAWeek", 1),
                Map.of("name", "electronics", "timesInAWeek", 1),
                Map.of("name", "computational_math", "timesInAWeek", 1),
                Map.of("name", "networks", "timesInAWeek", 1),
                Map.of("name", "operating_systems", "timesInAWeek", 1),
                Map.of("name", "databases", "timesInAWeek", 1),
                Map.of("name", "cybersecurity", "timesInAWeek", 1),
                Map.of("name", "machine_learning_methods", "timesInAWeek", 1),
                Map.of("name", "data_processing_and_storage", "timesInAWeek", 1),
                Map.of("name", "software_engineering", "timesInAWeek", 1),
                Map.of("name", "team_work", "timesInAWeek", 1),
                Map.of("name", "physical_culture", "timesInAWeek", 10)
        );

        for (Map<String, Object> subjectData : subjects) {
            String subjectName = (String) subjectData.get("name");
            Integer timesInAWeek = (Integer) subjectData.get("timesInAWeek");
            if (!subjectRepository.existsByName(subjectName)) {
                Subject subject = new Subject();
                subject.setName(subjectName);
                subject.setTimesInAWeek(timesInAWeek);
                subjectRepository.save(subject);
            }
        }
    }

    @Transactional
    public void saveTestTeachers() {
        List<String> teacherNames = Arrays.asList(
                "maryasov", "gorchakov", "ostapenko", "psychology", "andreev",
                "ippolitov", "grishkin", "pischik", "rutman", "zligostev",
                "zhidkov", "permyakov", "nedelko", "vaskevich", "patrushev",
                "bukshev", "miginsky", "kuzin", "vlasov", "balabanov",
                "bogdanov", "terenteva", "sitnov", "kutalev", "shvab",
                "golosov", "brek", "plusnin", "levikin", "fizruk"
        );

        for (String fullName : teacherNames) {
            if (!userRepository.existsByFullName(fullName)) {
                User user = new User();
                user.setFullName(fullName);
                user.setEmail(fullName + "@university.com"); // Пример создания электронной почты
                user.setPhone("1234567890"); // Пример телефонного номера
                user.setPassword("securePassword123"); // Пример пароля
                user.setDateOfCreation(new Date()); // Текущая дата и время
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow();
                roles.add(userRole);
                Role teacherRole = roleRepository.findByName(ERole.ROLE_TEACHER)
                        .orElseThrow();
                roles.add(teacherRole);
                user.setRoles(roles);
                userRepository.save(user);
            }
        }
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
