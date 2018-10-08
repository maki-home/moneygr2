CREATE TABLE IF NOT EXISTS parent_outcome_category (
  parent_category_id   INTEGER      NOT NULL,
  parent_category_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (parent_category_id)
)
  ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS outcome_category (
  category_id        INTEGER      NOT NULL,
  category_name      VARCHAR(255) NOT NULL,
  parent_category_id INTEGER      NOT NULL,
  PRIMARY KEY (category_id),
  FOREIGN KEY (parent_category_id) REFERENCES parent_outcome_category (parent_category_id)
)
  ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS outcome (
  outcome_id     INTEGER      NOT NULL AUTO_INCREMENT,
  amount         INTEGER      NOT NULL,
  created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by     VARCHAR(255) NOT NULL,
  is_credit_card BOOLEAN      NOT NULL,
  outcome_by     VARCHAR(255) NOT NULL,
  outcome_date   DATE         NOT NULL,
  outcome_name   VARCHAR(255) NOT NULL,
  quantity       INTEGER      NOT NULL,
  updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by     VARCHAR(255) NOT NULL,
  category_id    INTEGER      NOT NULL,
  PRIMARY KEY (outcome_id),
  FOREIGN KEY (category_id) REFERENCES outcome_category (category_id)
)
  ENGINE = InnoDB;