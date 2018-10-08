CREATE TABLE IF NOT EXISTS income_category (
  category_id   INTEGER      NOT NULL,
  category_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (category_id)
)
  ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS income (
  income_id   INTEGER      NOT NULL AUTO_INCREMENT,
  amount      INTEGER      NOT NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by  VARCHAR(255) NOT NULL,
  income_by   VARCHAR(255) NOT NULL,
  income_date DATE         NOT NULL,
  income_name VARCHAR(255) NOT NULL,
  updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by  VARCHAR(255) NOT NULL,
  category_id INTEGER      NOT NULL,
  PRIMARY KEY (income_id),
  FOREIGN KEY (category_id) REFERENCES income_category (category_id)
)
  ENGINE = InnoDB;