create table bank
(
    id               uuid primary key     default gen_random_uuid(),
    name             varchar(50) not null unique,
    created_at       timestamp   not null default current_timestamp,
    last_modified_at timestamp   not null default current_timestamp
);