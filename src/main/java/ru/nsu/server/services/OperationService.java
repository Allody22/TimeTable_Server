package ru.nsu.server.services;

import org.springframework.stereotype.Service;
import ru.nsu.server.model.dto.TimetableLogsDTO;
import ru.nsu.server.repository.logs.ActualTimetableLogsRepository;
import ru.nsu.server.repository.logs.PotentialTimetableLogsRepository;

import java.util.List;

@Service
public class OperationService {

    private final PotentialTimetableLogsRepository potentialTimetableLogsRepository;

    private final ActualTimetableLogsRepository actualTimetableLogsRepository;

    public OperationService( PotentialTimetableLogsRepository potentialTimetableLogsRepository, ActualTimetableLogsRepository actualTimetableLogsRepository) {
        this.potentialTimetableLogsRepository = potentialTimetableLogsRepository;
        this.actualTimetableLogsRepository = actualTimetableLogsRepository;
    }

    public List<TimetableLogsDTO> getAllPotentialTimetableLogs() {
        return potentialTimetableLogsRepository.findAllPotentialDto();
    }

    public List<TimetableLogsDTO> getAllActualTimetableLogs() {
        return actualTimetableLogsRepository.findAllActualDto();
    }

    public List<TimetableLogsDTO> getAllTimetableLogs() {
        List<TimetableLogsDTO> universalTimetableLogs = potentialTimetableLogsRepository.findAllPotentialDto();
        universalTimetableLogs.addAll(actualTimetableLogsRepository.findAllActualDto());
        return universalTimetableLogs;
    }
}
