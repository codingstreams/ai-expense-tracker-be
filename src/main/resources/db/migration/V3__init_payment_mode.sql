create table payment_mode
(
	id uuid primary key default gen_random_uuid(),
	name varchar(25) not null,
	created_at timestamp not null default current_timestamp,
	last_modified_at timestamp not null default current_timestamp
);