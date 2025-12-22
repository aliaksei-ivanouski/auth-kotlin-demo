drop table if exists "users";
create table "users"
(
    "id"             bigserial primary key not null,
    "email"          text                  not null unique,
    "password"       text      default null,
    "first_name"     text      default null,
    "last_name"      text      default null,
    "role"           text                  not null,
    "enabled"        boolean   default false,
    "email_verified" boolean   default false,
    "created_at"     timestamp default now(),
    "updated_at"     timestamp default now()
);

create index "user_email_idx" on "users" ("email");
