INSERT INTO constraints_names (name, ru_name)
VALUES ('number_of_teaching_days', 'Максимальное кол-во рабочих дней'),
       ('forbidden_period_for_teacher', 'Запрещенный порядковый номер пары для преподавателя в определённый день'),
       ('forbidden_period_for_group', 'Запрещенные порядковый номер пары для групп в определённый день'),
       ('forbidden_day_for_teacher', 'Запрещенный день для преподавания для преподавателя'),
       ('forbidden_day_for_group', 'Запрещенный день для преподавания для группы'),
       ('exact_time', 'Обязательное время пары'),
       ('teachers_overlapping', 'Недопустимое пересечение преподавателей'),
       ('groups_overlapping', 'Недопустимое пересечение групп');