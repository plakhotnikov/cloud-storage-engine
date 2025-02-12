
INSERT INTO role (role)
values ('ADMIN'),
       ('USER');




begin;
INSERT INTO users(email, password)
VALUES ('abc@abc.ru', '$2a$10$tHO8b6s9TzJQEt7oMFuUW.KnEVQS.SfhGgOQASTwIxkgzKS/0GmP6');
INSERT INTO user_roles(user_id, role_id)
VALUES (1, 2),
       (1, 1);
commit;
