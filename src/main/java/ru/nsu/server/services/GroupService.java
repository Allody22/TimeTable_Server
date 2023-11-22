package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.Group;
import ru.nsu.server.repository.GroupRepository;
import ru.nsu.server.repository.RoleRepository;
import ru.nsu.server.repository.RoomRepository;
import ru.nsu.server.repository.SubjectRepository;
import ru.nsu.server.repository.UserRepository;
import ru.nsu.server.repository.WeekTimeTableRepository;

import java.util.List;

@Service
public class GroupService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RoomRepository roomRepository;

    private final WeekTimeTableRepository weekTimeTableRepository;

    private final GroupRepository groupRepository;

    private final SubjectRepository subjectRepository;

    @Autowired
    public GroupService(UserRepository userRepository, RoleRepository roleRepository,
                        WeekTimeTableRepository weekTimeTableRepository, GroupRepository groupRepository,
                        SubjectRepository subjectRepository, RoomRepository roomRepository) {
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.weekTimeTableRepository = weekTimeTableRepository;
    }

    @Transactional
    public void saveNewGroup(String groupNumber, String faculty, int course, int studentsNumber) {
        Group group = new Group();
        group.setGroupNumber(groupNumber);
        group.setCourse(course);
        group.setStudentsNumber(studentsNumber);
        group.setFaculty(faculty);
        groupRepository.save(group);
    }

    public List<Group> getAllGroups() {
        return groupRepository.getAll();
    }

    public List<Group> getAllGroupsByFaculty(String faculty) {
        return groupRepository.getAllByFacultyContaining(faculty);
    }

    public boolean ifExistByGroupNumber(String groupNumber) {
        return groupRepository.existsByGroupNumber(groupNumber);
    }
}
