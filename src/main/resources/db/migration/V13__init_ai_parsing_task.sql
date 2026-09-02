create table ai_parsing_task
(
    id             uuid primary key default gen_random_uuid(),
    raw_input      text,
    content        text,
    error_message  text,
    correlation_id uuid,
    status         varchar(15),
    app_user_id    uuid references app_user (id),
    transaction_id uuid references transaction (id),
    created_at          timestamp  not null default current_timestamp,
    last_modified_at    timestamp  not null default current_timestamp
);