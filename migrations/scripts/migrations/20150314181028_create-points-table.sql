CREATE TABLE IF NOT EXISTS points (
  points_id       SERIAL PRIMARY KEY,
  company_id      INT REFERENCES company (company_id),
  client_id       INT REFERENCES client (client_id),
  sale_key        TEXT    NOT NULL,
  sale_amount     DECIMAL NOT NULL,
  points_to_earn  DECIMAL NOT NULL,
  required_amount DECIMAL NOT NULL,
  earned_points   DECIMAL NOT NULL,
  date            TIMESTAMP WITH TIME ZONE,
  UNIQUE (company_id, sale_key)
);