-- DEMO-141: baseline of the schema that existed before access control (DEMO-129).
-- Reproduces what Hibernate generated for the User entity (a Panache entity, so its ids come
-- from the users_seq sequence with an increment of 50). V2 adds the access-control tables.
-- Existing databases skip this file: Flyway baselines them at version 1.

create sequence if not exists users_seq start with 1 increment by 50;

create table if not exists users (
    id bigint not null primary key,
    name varchar(255),
    email varchar(255),
    password_hash varchar(255),
    constraint uk_users_email unique (email)
);
