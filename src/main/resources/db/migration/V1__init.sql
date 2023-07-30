create table auth_user
(
    id    uuid         not null default gen_random_uuid() primary key,
    name  varchar(255) not null,
    email varchar(511) not null unique
);

create table authenticator
(
    id              uuid   not null default gen_random_uuid() primary key,
    user_id         uuid   not null references auth_user (id),
    key_id          bytea  not null,
    cose            bytea  not null,
    signature_count bigint not null
);
