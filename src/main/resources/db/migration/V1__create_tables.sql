CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE constraints_names
(
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    ru_name VARCHAR(100) NOT NULL
);

CREATE TABLE faculties
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL
);

CREATE TABLE users
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP WITH TIME ZONE,
    email         VARCHAR(255),
    phone         VARCHAR(255),
    full_name     VARCHAR(255) UNIQUE,
    password      VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles
(
    user_id BIGINT REFERENCES users (id),
    role_id INT REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);