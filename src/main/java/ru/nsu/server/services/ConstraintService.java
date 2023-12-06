package ru.nsu.server.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.ConstraintsNames;
import ru.nsu.server.model.constraints.ForbiddenDayForGroup;
import ru.nsu.server.model.constraints.ForbiddenDayForTeacher;
import ru.nsu.server.model.constraints.ForbiddenPeriodForGroup;
import ru.nsu.server.model.constraints.ForbiddenPeriodForTeacher;
import ru.nsu.server.model.constraints.GroupsOverlapping;
import ru.nsu.server.model.constraints.NumberOfTeachingDays;
import ru.nsu.server.model.constraints.TeachersOverlapping;
import ru.nsu.server.payload.response.ConstraintResponse;
import ru.nsu.server.repository.ConstraintNamesRepository;
import ru.nsu.server.repository.constraints.ForbiddenDayForGroupRepository;
import ru.nsu.server.repository.constraints.ForbiddenDayForTeacherRepository;
import ru.nsu.server.repository.constraints.ForbiddenPeriodForGroupRepository;
import ru.nsu.server.repository.constraints.ForbiddenPeriodForTeacherRepository;
import ru.nsu.server.repository.constraints.GroupsOverlappingRepository;
import ru.nsu.server.repository.constraints.NumberOfTeachingDaysRepository;
import ru.nsu.server.repository.constraints.TeachersOverlappingRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ConstraintService {

    private final ForbiddenDayForGroupRepository forbiddenDayForGroupRepository;

    private final ForbiddenDayForTeacherRepository forbiddenDayForTeacherRepository;

    private final ForbiddenPeriodForGroupRepository forbiddenPeriodForGroupRepository;

    private final ForbiddenPeriodForTeacherRepository forbiddenPeriodForTeacherRepository;

    private final GroupsOverlappingRepository groupsOverlappingRepository;

    private final NumberOfTeachingDaysRepository numberOfTeachingDaysRepository;

    private final TeachersOverlappingRepository teachersOverlappingRepository;

    private final ConstraintNamesRepository constraintNamesRepository;

    public ConstraintService(ForbiddenDayForTeacherRepository forbiddenDayForTeacherRepository, ForbiddenDayForGroupRepository forbiddenDayForGroupRepository,
                             ForbiddenPeriodForTeacherRepository forbiddenPeriodForTeacherRepository, ForbiddenPeriodForGroupRepository forbiddenPeriodForGroupRepository,
                             GroupsOverlappingRepository groupsOverlappingRepository, NumberOfTeachingDaysRepository numberOfTeachingDaysRepository,
                             TeachersOverlappingRepository teachersOverlappingRepository, ConstraintNamesRepository constraintNamesRepository) {
        this.forbiddenDayForGroupRepository = forbiddenDayForGroupRepository;
        this.forbiddenDayForTeacherRepository = forbiddenDayForTeacherRepository;
        this.forbiddenPeriodForGroupRepository = forbiddenPeriodForGroupRepository;
        this.forbiddenPeriodForTeacherRepository = forbiddenPeriodForTeacherRepository;
        this.groupsOverlappingRepository = groupsOverlappingRepository;
        this.numberOfTeachingDaysRepository = numberOfTeachingDaysRepository;
        this.teachersOverlappingRepository = teachersOverlappingRepository;
        this.constraintNamesRepository = constraintNamesRepository;
    }

    @Transactional
    public void saveNewNumberOfTeachingDays(String teacher, Integer number) {
        NumberOfTeachingDays numberOfTeachingDays = new NumberOfTeachingDays();
        numberOfTeachingDays.setNumber(number);
        numberOfTeachingDays.setTeacher(teacher);
        numberOfTeachingDays.setDateOfCreation(new Date());
        numberOfTeachingDaysRepository.save(numberOfTeachingDays);
    }

    @Transactional
    public void saveNewTeachersOverlapping(String teacher1, String teacher2) {
        TeachersOverlapping teachersOverlapping = new TeachersOverlapping();
        teachersOverlapping.setTeacher1(teacher1);
        teachersOverlapping.setTeacher2(teacher2);
        teachersOverlapping.setDateOfCreation(new Date());
        teachersOverlappingRepository.save(teachersOverlapping);
    }

    @Transactional
    public void saveNewGroupsOverlapping(Integer group1, Integer group2) {
        GroupsOverlapping groupsOverlapping = new GroupsOverlapping();
        groupsOverlapping.setGroup2(group2);
        groupsOverlapping.setGroup1(group1);
        groupsOverlapping.setDateOfCreation(new Date());
        groupsOverlappingRepository.save(groupsOverlapping);

    }

    @Transactional
    public void saveNewForbiddenPeriodForTeacher(Integer day, String teacher, Integer period) {
        ForbiddenPeriodForTeacher forbiddenPeriodForTeacher = new ForbiddenPeriodForTeacher();
        forbiddenPeriodForTeacher.setTeacher(teacher);
        forbiddenPeriodForTeacher.setDateOfCreation(new Date());
        forbiddenPeriodForTeacher.setPeriod(period);
        forbiddenPeriodForTeacher.setDay(day);
        forbiddenPeriodForTeacherRepository.save(forbiddenPeriodForTeacher);
    }

    @Transactional
    public void saveNewForbiddenPeriodForGroup(Integer day, Integer group, Integer period) {
        ForbiddenPeriodForGroup forbiddenPeriodForGroup = new ForbiddenPeriodForGroup();
        forbiddenPeriodForGroup.setPeriod(period);
        forbiddenPeriodForGroup.setGroup(group);
        forbiddenPeriodForGroup.setDay(day);
        forbiddenPeriodForGroup.setDateOfCreation(new Date());
        forbiddenPeriodForGroupRepository.save(forbiddenPeriodForGroup);
    }

    @Transactional
    public void saveNewForbiddenDayForTeachers(Integer day, String teacher) {
        ForbiddenDayForTeacher forbiddenDayForTeacher = new ForbiddenDayForTeacher();
        forbiddenDayForTeacher.setDateOfCreation(new Date());
        forbiddenDayForTeacher.setTeacher(teacher);
        forbiddenDayForTeacher.setDay(day);
        forbiddenDayForTeacherRepository.save(forbiddenDayForTeacher);
    }

    @Transactional
    public void saveNewForbiddenDayForGroups(Integer day, Integer group) {
        ForbiddenDayForGroup forbiddenDayForGroup = new ForbiddenDayForGroup();
        forbiddenDayForGroup.setGroup(group);
        forbiddenDayForGroup.setDay(day);
        forbiddenDayForGroup.setDateOfCreation(new Date());
        forbiddenDayForGroupRepository.save(forbiddenDayForGroup);
    }

    public List<ConstraintResponse> getAllConstraints() {
        List<ConstraintResponse> constraintResponses = new ArrayList<>();
        List<ForbiddenDayForGroup> forbiddenDaysForGroups = forbiddenDayForGroupRepository.findAll();
        if (!forbiddenDaysForGroups.isEmpty()) {
            for (var currentConstraint : forbiddenDaysForGroups) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu("Запрещенный день для преподавания для группы");
                constraintResponse.setDay(currentConstraint.getDay());
                constraintResponse.setGroup(currentConstraint.getGroup());
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponses.add(constraintResponse);
            }
        }
        List<ForbiddenDayForTeacher> forbiddenDayForTeachers = forbiddenDayForTeacherRepository.findAll();
        if (!forbiddenDayForTeachers.isEmpty()) {
            for (var currentConstraint : forbiddenDayForTeachers) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu("Запрещенный день для преподавания для препода");
                constraintResponse.setTeacher(currentConstraint.getTeacher());
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setDay(currentConstraint.getDay());
                constraintResponses.add(constraintResponse);
            }
        }

        List<ForbiddenPeriodForGroup> forbiddenPeriodForGroups = forbiddenPeriodForGroupRepository.findAll();
        if (!forbiddenPeriodForGroups.isEmpty()) {
            for (var currentConstraint : forbiddenPeriodForGroups) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu("Запрещенные порядковый номер пары для групп в определённый день");
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setDay(currentConstraint.getDay());
                constraintResponse.setGroup(currentConstraint.getGroup());
                constraintResponse.setPeriod(currentConstraint.getPeriod());
                constraintResponses.add(constraintResponse);
            }
        }

        List<ForbiddenPeriodForTeacher> forbiddenPeriodForTeachers = forbiddenPeriodForTeacherRepository.findAll();
        if (!forbiddenPeriodForTeachers.isEmpty()) {
            for (var currentConstraint : forbiddenPeriodForTeachers) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu("Запрещенный порядковый номер пары для препода в определённый день");
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setDay(currentConstraint.getDay());
                constraintResponse.setTeacher(currentConstraint.getTeacher());
                constraintResponse.setPeriod(currentConstraint.getPeriod());
                constraintResponses.add(constraintResponse);
            }
        }

        List<GroupsOverlapping> groupsOverlapping = groupsOverlappingRepository.findAll();
        if (!groupsOverlapping.isEmpty()) {
            for (var currentConstraint : groupsOverlapping) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu("Перегруз групп (??)");
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setGroup1(currentConstraint.getGroup1());
                constraintResponse.setGroup2(currentConstraint.getGroup2());
                constraintResponses.add(constraintResponse);
            }
        }

        List<NumberOfTeachingDays> numberOfTeachingDays = numberOfTeachingDaysRepository.findAll();
        if (!numberOfTeachingDays.isEmpty()) {
            for (var currentConstraint : numberOfTeachingDays) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu("Максимальное кол-во рабочих дней");
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setTeacher(currentConstraint.getTeacher());
                constraintResponse.setNumber(currentConstraint.getNumber());
                constraintResponses.add(constraintResponse);
            }
        }

        List<TeachersOverlapping> teachersOverlapping = teachersOverlappingRepository.findAll();
        if (!teachersOverlapping.isEmpty()) {
            for (var currentConstraint : teachersOverlapping) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu("Перегруз учителей (?)");
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setTeacher1(currentConstraint.getTeacher1());
                constraintResponse.setTeacher2(currentConstraint.getTeacher2());
                constraintResponses.add(constraintResponse);
            }
        }
        return constraintResponses;
    }

    public List<String> getAllConstraintsRu() {
        return constraintNamesRepository.getAllRuNames()
                .orElse(null);
    }

    public List<String> getAllConstraintsEng() {
        return constraintNamesRepository.getAllEngNames()
                .orElse(null);
    }

    public boolean ifExistConstraintRu(String name) {
        return constraintNamesRepository.existsByRuName(name);
    }

    public Optional<ConstraintsNames> findConstraintByRuName(String name) {
        return constraintNamesRepository.findConstraintsNamesByRuName(name);
    }
}
