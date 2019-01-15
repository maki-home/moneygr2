INSERT INTO parent_outcome_category (parent_category_id, parent_category_name) VALUES
  (13, '経費');
INSERT INTO outcome_category (category_id, category_name, parent_category_id) VALUES
  (80, '交通費', 13),
  (81, '交際費', 13),
  (82, '雑費', 13),
  (83, '修繕費', 13),
  (84, 'その他', 13);