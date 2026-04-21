DROP TABLE IF EXISTS person_report;

CREATE TABLE person_report (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name VARCHAR(100),
    department VARCHAR(50),
    salary    INT,
    bonus     INT
);
