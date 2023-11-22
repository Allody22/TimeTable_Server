CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE users
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP WITH TIME ZONE,
    email         VARCHAR(255) UNIQUE NOT NULL,
    phone         VARCHAR(255) UNIQUE,
    full_name     VARCHAR(255) UNIQUE,
    password      VARCHAR(255)        NOT NULL
);

CREATE TABLE user_roles
(
    user_id BIGINT REFERENCES users (id),
    role_id INT REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);