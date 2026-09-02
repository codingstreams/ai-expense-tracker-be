alter table app_user_config
    add column payment_mode_id uuid references payment_mode (id);