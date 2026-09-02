create table system_category
(
    id               uuid primary key     default gen_random_uuid(),
    name             varchar(50) not null unique,
    created_at       timestamp   not null default current_timestamp,
    last_modified_at timestamp   not null default current_timestamp
);

INSERT INTO system_category (name)
VALUES
       ('Groceries'),
       ('Dining Out'),
       ('Rent/EMI'),
       ('Utilities (Electricity/Water)'),
       ('Fuel/Transportation'),
       ('Health & Medical'),
       ('Insurance'),
       ('Shopping (Clothing/Electronics)'),
       ('Entertainment & OTT'),
       ('Education'),
       ('Investments (SIP/Stocks)'),
       ('Gifts & Donations'),
       ('Travel & Vacation'),
       ('Maintenance & Repairs'),
       ('Miscellaneous'),

       ('Domestic Help & Services'),
       ('Festivals & Puja'),
       ('Society Maintenance & Taxes'),
       ('Broadband & Mobile Bills'),
       ('Personal Care & Grooming'),
       ('Pets & Animals'),
       ('Informal Loans & Borrowings'),

       ('Vegetables & Fruits'),
       ('Milk & Dairy'),
       ('Food & Snacks'),
       ('Household Supplies & Toiletries')
ON CONFLICT (name) DO NOTHING;