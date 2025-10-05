create table action (
  id varchar(255),
  recipient_id varchar(255),
  category varchar(255),
  provider varchar(255),
  name varchar(255),
  amount varchar(255),
  game varchar(255),
  payload jsonb
);
