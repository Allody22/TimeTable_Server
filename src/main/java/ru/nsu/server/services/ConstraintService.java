package ru.nsu.server.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.ConstraintsNames;
import ru.nsu.server.model.constraints.UniversalConstraint;
import ru.nsu.server.payload.response.ConstraintResponse;
import ru.nsu.server.repository.ConstraintNamesRepository;
import ru.nsu.server.repository.constraints.UniversalConstraintRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ConstraintService {


    private final UniversalConstraintRepository universalConstraintRepository;

    private final ConstraintNamesRepository constraintNamesRepository;

    public ConstraintService(ConstraintNamesRepository constraintNamesRepository,
                             UniversalConstraintRepository universalConstraintRepository) {
        this.universalConstraintRepository = universalConstraintRepository;
        this.constraintNamesRepository = constraintNamesRepository;
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
                                           Integer day, Integer period, Integer number) {
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
        universalConstraintRepository.save(universalConstraint);
    }

    public List<ConstraintResponse> getAllConstraints() {
        List<ConstraintResponse> constraintResponses = new ArrayList<>();
        List<UniversalConstraint> universalConstraintList = universalConstraintRepository.findAll();
        if (!universalConstraintList.isEmpty()) {
            for (var currentConstraint : universalConstraintList) {
                ConstraintResponse constraintResponse = new ConstraintResponse();
                constraintResponse.setConstraintNameRu(currentConstraint.getConstraintNameRu());
                constraintResponse.setConstraintNameEng(currentConstraint.getConstraintName());
                constraintResponse.setDay(currentConstraint.getDay());
                constraintResponse.setGroup(currentConstraint.getGroup());
                constraintResponse.setDateOfCreation(currentConstraint.getDateOfCreation());
                constraintResponse.setTeacher(currentConstraint.getTeacher());
                constraintResponse.setPeriod(currentConstraint.getPeriod());
                constraintResponse.setGroup1(currentConstraint.getGroup1());
                constraintResponse.setGroup2(currentConstraint.getGroup2());
                constraintResponse.setNumber(currentConstraint.getNumber());
                constraintResponse.setTeacher1(currentConstraint.getTeacher1());
                constraintResponse.setTeacher2(currentConstraint.getTeacher2());
                constraintResponse.setId(currentConstraint.getId());

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
