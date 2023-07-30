create table auth_user
(
    id    uuid         not null default gen_random_uuid() primary key,
    name  varchar(255) not null,
    email varchar(511) not null unique
);
