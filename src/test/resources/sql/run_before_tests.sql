-- demo users (password is "password" for all)
insert into "users" (email, password, first_name, last_name, role, enabled, email_verified)
values ('admin@test.com', '$2a$10$5sqnIYhRQgNrUZ9sOn8Qzegze1B4lzzJecknIHdp4IzWlMRFdcoUi', 'John', 'Smith', 'ADMIN',
        true, true),
       ('user@test.com', '$2a$10$5sqnIYhRQgNrUZ9sOn8Qzegze1B4lzzJecknIHdp4IzWlMRFdcoUi', 'Kelly', 'Done', 'USER', true,
        true),
       ('moderator@test.com', '$2a$10$5sqnIYhRQgNrUZ9sOn8Qzegze1B4lzzJecknIHdp4IzWlMRFdcoUi', 'Jeffrey', 'Stone',
        'MODERATOR', true, true);