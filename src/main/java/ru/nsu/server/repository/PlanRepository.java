package ru.nsu.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.study_plan.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    boolean existsByTeacherAndSubjectAndSubjectTypeAndGroupsAndTimesInAWeek(String teacher, String subject, String subjectType, String groups, int timesInAWeek);
}
