--INSERT INTO system_category (name)
--VALUES ('Groceries'),
--       ('Dining Out'),
--       ('Rent/EMI'),
--       ('Utilities (Electricity/Water)'),
--       ('Fuel/Transportation'),
--       ('Health & Medical'),
--       ('Insurance'),
--       ('Shopping (Clothing/Electronics)'),
--       ('Entertainment & OTT'),
--       ('Education'),
--       ('Investments (SIP/Stocks)'),
--       ('Gifts & Donations'),
--       ('Travel & Vacation'),
--       ('Maintenance & Repairs'),
--       ('Miscellaneous')
--ON CONFLICT (name) DO NOTHING;

INSERT INTO system_category (name)
VALUES
       -- Core / Original Categories
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

       -- India-Specific Lifestyle & Services
       ('Domestic Help & Services'),
       ('Festivals & Puja'),
       ('Society Maintenance & Taxes'),
       ('Broadband & Mobile Bills'),
       ('Personal Care & Grooming'),
       ('Pets & Animals'),
       ('Informal Loans & Borrowings'),

       -- Detailed Food & Daily Living
       ('Vegetables & Fruits'),
       ('Milk & Dairy'),
       ('Food Delivery & Snacks'),
       ('Household Supplies & Toiletries')
ON CONFLICT (name) DO NOTHING;