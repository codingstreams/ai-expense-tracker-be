create table bank
(
	id uuid primary key default gen_random_uuid(),
	name varchar(50) not null unique,
	created_at timestamp not null default current_timestamp,
	last_modified_at timestamp not null default current_timestamp
);

INSERT INTO bank (name)
VALUES ('State Bank of India'),
       ('HDFC Bank'),
       ('ICICI Bank'),
       ('Axis Bank'),
       ('Punjab National Bank'),
       ('Bank of Baroda'),
       ('Canara Bank'),
       ('Central Bank of India'),
       ('Union Bank of India'),
       ('IndusInd Bank'),
       ('Kotak Mahindra Bank');