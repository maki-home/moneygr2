UPDATE outcome AS o, outcome_category AS c
SET is_expense = true
WHERE c.parent_category_id = 13
  AND o.category_id = c.category_id;