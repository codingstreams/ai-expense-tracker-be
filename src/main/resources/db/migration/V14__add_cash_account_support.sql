alter table account alter column bank_id drop not null;

alter table account drop constraint if exists account_account_type_check;

alter table account add constraint account_account_type_check check (account_type in ('SAVINGS', 'CREDIT', 'CASH'));

insert into account (last_four_digits, balance, account_type, app_user_id)
select 'CASH', 0, 'CASH', id
from app_user
where id not in (select app_user_id from account where account_type = 'CASH');
