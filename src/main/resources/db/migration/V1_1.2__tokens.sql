drop table if exists "refresh_tokens";
create table "refresh_tokens"
(
    "id"         bigserial primary key,
    "token"      text      not null unique,
    "user_id"    bigint    not null,
    "expires_at" timestamp not null,
    "created_at" timestamp not null default NOW(),
    "revoked"    boolean   not null default false,
    constraint "fk_refresh_tokens_user" foreign key ("user_id") references "users" ("id") on delete cascade
);

create index "idx_refresh_tokens_token" on "refresh_tokens" ("token");
create index "idx_refresh_tokens_user_id" on "refresh_tokens" ("user_id");


drop table if exists "email_verification_tokens";
create table "email_verification_tokens"
(
    "id"         bigserial primary key,
    "token"      text      not null unique,
    "user_id"    bigint    not null,
    "expires_at" timestamp not null,
    "created_at" timestamp not null default NOW(),
    "used"       boolean   not null default false,
    constraint "fk_email_verification_tokens_user" foreign key ("user_id") references "users" ("id") on delete cascade
);

create index "idx_email_verification_tokens_token" on "email_verification_tokens" ("token");
create index "idx_email_verification_tokens_user_id" on "email_verification_tokens" ("user_id");


drop table if exists "password_reset_tokens";
create table "password_reset_tokens"
(
    "id"         bigserial primary key,
    "token"      text      not null unique,
    "user_id"    bigint    not null,
    "expires_at" timestamp not null,
    "created_at" timestamp not null default NOW(),
    "used"       boolean   not null default false,
    constraint "fk_password_reset_tokens_user" foreign key ("user_id") references "users" ("id") on delete cascade
);

create index "idx_password_reset_tokens_token" on "password_reset_tokens" ("token");
create index "idx_password_reset_tokens_user_id" on "password_reset_tokens" ("user_id");
