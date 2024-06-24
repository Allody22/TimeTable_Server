-- Вставка пользователя
INSERT INTO users (creation_time, email, phone, full_name, password)
-- пароль admin
VALUES (CURRENT_TIMESTAMP, 'admin@gmail.com', '1234567890', 'Admin User',
        '$2a$10$YrLOEZ78QgwmyLcYF/M9y.4/kGU9djHyfLv7n8fCA.Cp1Of3Aatka');

-- Добавление ролей пользователю
DO
$$
    DECLARE
        last_user_id BIGINT;
    BEGIN
        -- Получение последнего добавленного ID пользователя
        SELECT id INTO last_user_id FROM users ORDER BY id DESC LIMIT 1;

        -- Добавление записей в user_roles для этого пользователя
        INSERT INTO user_roles (user_id, role_id)
        SELECT last_user_id, id
        FROM roles
        WHERE name IN ('ROLE_USER', 'ROLE_GUEST', 'ROLE_TEACHER', 'ROLE_ADMINISTRATOR');

--         WHERE name IN ('ROLE_USER', 'ROLE_GUEST', 'ROLE_TEACHER', 'ROLE_ADMINISTRATOR','ROLE_DISPATCHER');
    END
$$;
