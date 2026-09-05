BEGIN;
UPDATE transaction
SET
    amount = ABS(amount),
    last_modified_at = CURRENT_TIMESTAMP
WHERE
    type = 'EXPENSE'
    AND amount < 0;

COMMIT;