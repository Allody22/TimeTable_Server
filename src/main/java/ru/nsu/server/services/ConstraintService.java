package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.constraints.ConstraintsNames;
import ru.nsu.server.model.constraints.UniversalConstraint;
import ru.nsu.server.model.user.Operations;
import ru.nsu.server.payload.response.ConstraintResponse;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.repository.constraints.ConstraintNamesRepository;
import ru.nsu.server.repository.constraints.UniversalConstraintRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ConstraintService {


    private final UniversalConstraintRepository universalConstraintRepository;

    private final ConstraintNamesRepository constraintNamesRepository;

    private final OperationsRepository operationsRepository;
    private final PotentialTimetableService potentialTimetableService;
    private final TimetableService timetableService;


    @Autowired
    public ConstraintService(ConstraintNamesRepository constraintNamesRepository, OperationsRepository operationsRepository,
                             UniversalConstraintRepository universalConstraintRepository, PotentialTimetableService potentialTimetableService, TimetableService timetableService) {
        this.universalConstraintRepository = universalConstraintRepository;
        this.constraintNamesRepository = constraintNamesRepository;
        this.operationsRepository = operationsRepository;
        this.potentialTimetableService = potentialTimetableService;
        this.timetableService = timetableService;
    }

    @Transactional
    public void deleteUniversalConstraint(Long id) {
        if (!universalConstraintRepository.existsById(id)) {
            return;
        }
        universalConstraintRepository.deleteById(id);
    }

    public boolean existById(Long id) {
        return universalConstraintRepository.existsById(id);
    }

    @Transactional
    public void saveNewUniversalConstraint(String constraintNameRu, String constraintNameEng, Integer
            group, Integer group1, Integer group2, String teacher, String teacher1, String teacher2,
                                           Integer day, Integer period, Integer number, String subjectName,
                                           String room, String groups, String subjectType) {
        UniversalConstraint universalConstraint = new UniversalConstraint();
        universalConstraint.setDateOfCreation(new Date());
        universalConstraint.setConstraintName(constraintNameEng);
        universalConstraint.setConstraintNameRu(constraintNameRu);
        universalConstraint.setGroup(group);
        universalConstraint.setGroup1(group1);
        universalConstraint.setGroup2(group2);
        universalConstraint.setTeacher(teacher);
        universalConstraint.setTeacher1(teacher1);
        universalConstraint.setTeacher2(teacher2);
        universalConstraint.setDay(day);
        universalConstraint.setPeriod(period);
        universalConstraint.setNumber(number);
        universalConstraint.setSubject(subjectName);
        universalConstraint.setGroups(groups);
        universalConstraint.setRoom(room);
        universalConstraint.setSubjectType(subjectType);
        universalConstraintRepository.save(universalConstraint);

        StringBuilder sb = new StringBuilder();

        sb.append("Создано новое ограничение").append(" '").append(constraintNameRu).append("', с условием");

        int initialLength = sb.length();

        if (group1 != null && group1 != -1) {
            sb.append(" первая группа: '").append(group1).append("',");
        }
        if (group2 != null && group2 != -1) {
            sb.append(" вторая группа: '").append(group2).append("',");
        }
        if (number != null && number != -1) {
            sb.append(" кол-во: '").append(number).append("',");
        }
        if (day != null && day != -1) {
            sb.append(" номером дня недели: '").append(day).append("',");
        }
        if (teacher != null && teacher.isBlank() && teacher.equals("Не указан")) {
            sb.append(" преподавателем: '").append(teacher).append("',");
        }
        if (teacher1 != null && teacher1.isBlank() && teacher1.equals("Не указан")) {
            sb.append(" первым преподавателем: '").append(teacher1).append("',");
        }

        if (teacher2 != null && teacher2.isBlank() && teacher2.equals("Не указан")) {
            sb.append(" вторым преподавателем: '").append(teacher2).append("',");
        }

        if (period != null && period != -1) {
            sb.append(" номером пары: '").append(teacher1).append("',");
        }

        if (subjectName != null && !subjectName.isBlank()) {
            sb.append(" названием пары: '").append(subjectName).append("',");
        }
        if (sb.length() > initialLength) {
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setLength(sb.length() - 1);
            }
        }

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setDescription(sb.toString());
        operations.setUserAccount("Администратор");
        operationsRepository.save(operations);
    }

    public List<ConstraintResponse> getAllConstraints() {
        List<ConstraintResponse> constraintResponses = new ArrayList<>();
        List<UniversalConstraint> universalConstraintList = universalConstraintRepository.findAll();
        if (!universalConstraintList.isEmpty()) {
            for (var currentConstraint : universalConstraintList) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu(currentConstraint.getConstraintNameRu());
                constraintResponse.setConstraintNameEng(currentConstraint.getConstraintName());
                constraintResponse.setTeacher(currentConstraint.getTeacher());
                constraintResponse.setDay(currentConstraint.getDay());
                constraintResponse.setGroup(currentConstraint.getGroup());
//                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setPeriod(currentConstraint.getPeriod());
                constraintResponse.setGroup1(currentConstraint.getGroup1());
                constraintResponse.setGroup2(currentConstraint.getGroup2());
                constraintResponse.setNumber(currentConstraint.getNumber());
                constraintResponse.setTeacher1(currentConstraint.getTeacher1());
                constraintResponse.setTeacher2(currentConstraint.getTeacher2());
                constraintResponse.setId(currentConstraint.getId());
                constraintResponse.setSubject(currentConstraint.getSubject());
                constraintResponse.setRoom(currentConstraint.getRoom());
                constraintResponse.setSubjectType(currentConstraint.getSubjectType());
                constraintResponse.setGroups(currentConstraint.getGroups());
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
