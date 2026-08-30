create table account
(
    id uuid primary key default gen_random_uuid(),
    last_four_digits varchar(4) not null,
    balance float4 default 0 check ( balance >= 0 ),
    account_type varchar(10) not null check ( account_type in ('SAVINGS', 'CREDIT', 'CASH') ),
    app_user_id uuid not null references app_user (id),
    bank_id uuid not null references bank (id),
    is_active boolean not null default true,
    is_upi_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    is_net_banking_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at timestamp not null default current_timestamp,
    last_modified_at timestamp not null default current_timestamp
);