create table card
(
	id uuid primary key default gen_random_uuid(),
	last_four_digits varchar(4) not null,
	card_type varchar(15) not null check ( card_type in ('CREDIT_CARD', 'DEBIT_CARD') ),
	app_user_id uuid not null references app_user (id),
	account_id uuid not null references account (id),
	created_at timestamp not null default current_timestamp,
	last_modified_at timestamp not null default current_timestamp
);