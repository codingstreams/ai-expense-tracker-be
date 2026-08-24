create table app_user_config
(
    id                  uuid primary key    default gen_random_uuid(),
    language_preference varchar(2) not null check (language_preference IN ('EN', 'HI')),
    spend_limit         int        not null default 0 check ( spend_limit >= 0 ),
    currency            varchar(3) not null default 'INR' check ( currency in ('INR') ),
    app_user_id         uuid       not null references app_user (id),
    created_at          timestamp  not null default current_timestamp,
    last_modified_at    timestamp  not null default current_timestamp
);