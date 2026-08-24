CREATE TABLE transaction
(
    id                      UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    type                    varchar(15) NOT NULL check ( type in ('EXPENSE', 'INCOME', 'TRANSFER') ),
    amount                  FLOAT       NOT NULL,
    transaction_date        DATE        NOT NULL,
    transfer_id             UUID,
    description             VARCHAR(255),
    transaction_category_id UUID        REFERENCES system_category (id) ON DELETE SET NULL,
    app_user_id             UUID REFERENCES app_user (id),
    account_id              UUID REFERENCES account (id),
    payment_mode_id         UUID REFERENCES payment_mode (id),
    created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);