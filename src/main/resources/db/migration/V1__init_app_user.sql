create table app_user(
	id uuid primary key ,
	name varchar(255) not null ,
	email varchar(255) not null unique ,
	password varchar(255) not null ,
	created_at timestamp not null ,
    last_modified_at timestamp not null,
    is_onboarding_complete boolean not null default false
);

-- create table app_user_config(
--     id uuid primary key ,
--     language_preference varchar(2) not null check (language_preference IN ('en', 'hi')),
--     spend_limit int not null check ( spend_limit > 0 )
-- );
--
-- create table bank(
--   id uuid primary key ,
--   name varchar(50) not null unique
-- );
--
-- create table account();
