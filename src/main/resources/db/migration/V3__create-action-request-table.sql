create table action_request (
  id varchar(255) primary key,
  source varchar(255),
  origin_id varchar(255),
  actions jsonb
);
