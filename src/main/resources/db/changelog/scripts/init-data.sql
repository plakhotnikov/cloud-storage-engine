
INSERT INTO role (role)
values ('ADMIN'),
       ('USER');




begin;
INSERT INTO users(username, password)
VALUES ('ADMIN', '{bcrypt}$2a$10$tHwHP4coM61zxJz4QrFUqOoWNVUFRuDBtAEdTmikMca9J4vLgrdqG');
INSERT INTO user_roles(user_id, role_id)
VALUES (1, 2),
       (1, 1);
commit;
