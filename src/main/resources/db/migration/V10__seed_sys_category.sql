INSERT INTO system_category (name)
VALUES ('Groceries'),
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
       ('Miscellaneous')
ON CONFLICT (name) DO NOTHING;