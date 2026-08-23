create table payment_mode(
    id uuid primary key default gen_random_uuid(),
    name varchar(25) not null ,
    txn_type varchar(10) not null check ( txn_type in ('ASSET', 'LIABILITY') ) ,
    created_at timestamp not null default current_timestamp,
    last_modified_at timestamp not null default current_timestamp
);

alter table app_user_config
add column payment_mode_id uuid references payment_mode(id);