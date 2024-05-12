package ru.nsu.server.repository.constraints;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.constraints.UniversalConstraint;

@Repository
public interface UniversalConstraintRepository extends JpaRepository<UniversalConstraint, Long> {

    boolean existsByConstraintNameAndGroupAndGroup1AndGroup2AndTeacherAndTeacher1AndTeacher2AndDayAndPeriodAndNumberAndSubject(String constraintName, Integer group, Integer group1, Integer group2, String teacher, String teacher1,
                                                                                                                               String teacher2, Integer day, Integer period, Integer number, String subject);
}
