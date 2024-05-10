package ru.nsu.server.repository.logs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.server.model.dto.TimetableLogsDTO;
import ru.nsu.server.model.operations.ActualTimetableLogs;

import java.util.List;

@Repository
public interface ActualTimetableLogsRepository extends JpaRepository<ActualTimetableLogs, Long> {

    @Query("SELECT new ru.nsu.server.model.dto.TimetableLogsDTO(l.dateOfCreation, l.description, l.userAccount,l.operationName,l.subjectId, " +
            "l.newDayNumber, l.newPairNumber, l.newRoom, l.newTeacherFullName, l.oldDayNumber, l.oldPairNumber, l.oldRoom, l.oldTeacherFullName) FROM ActualTimetableLogs l")
    List<TimetableLogsDTO> findAllActualDto();
}
