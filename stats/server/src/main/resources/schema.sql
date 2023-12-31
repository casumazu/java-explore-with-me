DROP TABLE IF EXISTS hits;

create table if not exists hits
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app VARCHAR(30)  NOT NULL,
    uri VARCHAR(250) NOT NULL,
    ip VARCHAR(50)  NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);