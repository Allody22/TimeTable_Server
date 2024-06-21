package ru.nsu.server.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nsu.server.model.constants.ERole;
import ru.nsu.server.model.user.Operations;
import ru.nsu.server.model.user.Role;
import ru.nsu.server.model.user.User;
import ru.nsu.server.repository.OperationsRepository;
import ru.nsu.server.repository.RoleRepository;
import ru.nsu.server.repository.UserRepository;

import java.util.*;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder encoder;

    private final OperationsRepository operationsRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder encoder, OperationsRepository operationsRepository) {
        this.roleRepository = roleRepository;
        this.operationsRepository = operationsRepository;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public boolean existByEmailCheck(String email) {
        return userRepository.existsByEmail(email);
    }


    public User findById(Long id) {
        return userRepository.findById(id).get();
    }

    public String generateRandomPassword() {
        Random random = new Random();
        int randomPassword = 100000 + random.nextInt(900000);
        return String.valueOf(randomPassword);
    }

    public String changeUserRoles(String email, Set<String> stringRoles) {
        User userByEmail = userRepository.findByEmail(email).
                orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> roles = new HashSet<>();
        for (var sRole : stringRoles) {
            Role userRole = roleRepository.findByName(ERole.valueOf(sRole))
                    .orElseThrow();
            roles.add(userRole);

        }
        userByEmail.setRoles(roles);

        userRepository.save(userByEmail);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Пользователь с почтой '" + email + "' сменил роли ";
        operations.setDescription(description);
        operationsRepository.save(operations);

        return description + ". Операция сделана пользователем " + operations.getUserAccount();
    }

    public String saveNewAdmin(String email, String fullName, String phone) {
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setDateOfCreation(new Date());
        newUser.setPhone(phone);

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow();
        roles.add(userRole);
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMINISTRATOR)
                .orElseThrow();
        roles.add(adminRole);
        newUser.setRoles(roles);

        String userPassword = generateRandomPassword();
        newUser.setPassword(encoder.encode(userPassword));

        userRepository.save(newUser);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Зарегистрирован новый админ с именем '" + fullName + "' и почтой " + email;
        operations.setDescription(description);
        operationsRepository.save(operations);
        log.info("admin password:{}", userPassword);

        return description + ". Операция сделана пользователем " + operations.getUserAccount();
    }

    public String saveNewTeacher(String email, String fullName, String phone) {
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setDateOfCreation(new Date());
        newUser.setPhone(phone);

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow();
        roles.add(userRole);
        Role teacherRole = roleRepository.findByName(ERole.ROLE_TEACHER)
                .orElseThrow();
        roles.add(teacherRole);
        newUser.setRoles(roles);

        String userPassword = generateRandomPassword();
        newUser.setPassword(encoder.encode(userPassword));

        userRepository.save(newUser);


        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Зарегистрирован новый учитель с именем '" + fullName + "' и почтой " + email;
        operations.setDescription(description);
        operationsRepository.save(operations);

        log.info("teacher password:{}", userPassword);

        return description + ". Операция сделана пользователем " + operations.getUserAccount();
    }

    public String saveNewUser(String email, String fullName, String phone) {
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setDateOfCreation(new Date());
        newUser.setPhone(phone);

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow();
        roles.add(userRole);
        newUser.setRoles(roles);

        String userPassword = generateRandomPassword();
        newUser.setPassword(encoder.encode(userPassword));

        userRepository.save(newUser);

        Operations operations = new Operations();
        operations.setDateOfCreation(new Date());
        operations.setUserAccount("Админ");
        String description = "Зарегистрирован новый пользователь с именем '" + fullName + "' и почтой " + email;
        operations.setDescription(description);
        operationsRepository.save(operations);
        log.info("User password:{}", userPassword);

        return description + ". Операция сделана пользователем " + operations.getUserAccount();
    }
}
