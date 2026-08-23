create table app_user(
	id uuid primary key ,
	name varchar(255) not null ,
	email varchar(255) not null unique ,
	password varchar(255) not null ,
    created_at timestamp not null default current_timestamp,
    last_modified_at timestamp not null default current_timestamp,
    is_onboarding_complete boolean not null default false
);
