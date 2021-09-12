CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY,
    login VARCHAR(50) unique not null,
    password varchar(50) not null,
    email varchar(50) unique not null,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    birthday date not null,
    role INT NOT NULL,
    FOREIGN KEY (role) REFERENCES roles(id)
);