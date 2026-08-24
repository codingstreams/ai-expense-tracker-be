create table ai_insight
(
    id                      uuid primary key default gen_random_uuid(),
    period                  varchar(50) not null,
    summary                 text,
    top_spending_category   varchar(100),
    top_spending_percentage float4,
    top_spending_insight    text,
    anomalies               text,
    actionable_tips         text,
    app_user_id             uuid        not null references app_user (id),
    created_at              timestamp   not null default current_timestamp,
    last_modified_at        timestamp   not null default current_timestamp
);
