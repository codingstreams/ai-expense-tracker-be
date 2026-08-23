create table account(
    id uuid primary key default gen_random_uuid(),
    last_four_digits varchar(4) not null,
    balance float4 default 0 check ( balance >= 0 ),
    account_type varchar(10) not null check ( account_type in ('SAVINGS', 'CREDIT') ),
    app_user_id uuid not null references app_user(id),
    bank_id uuid not null references bank(id),
    created_at timestamp not null default current_timestamp,
    last_modified_at timestamp not null default current_timestamp
);