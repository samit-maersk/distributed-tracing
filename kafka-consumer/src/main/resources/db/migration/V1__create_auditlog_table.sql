CREATE TABLE IF NOT EXISTS AUDITLOG (
    id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    message VARCHAR(255) NOT NULL,
    date TIMESTAMP NOT NULL
);