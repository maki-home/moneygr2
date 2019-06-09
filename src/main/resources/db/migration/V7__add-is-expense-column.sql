ALTER TABLE outcome
    ADD is_expense BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE outcome
    ADD INDEX (is_expense);