UPDATE outcome
SET outcome_date = ADDDATE(outcome_date, 1);
UPDATE income
SET income_date = ADDDATE(income_date, 1);