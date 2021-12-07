create schema socket_chat collate utf8mb4_0900_ai_ci;

create table log_types
(
    id   int auto_increment
        primary key,
    type varchar(60) null
);

create table logs
(
    id       int auto_increment
        primary key,
    type_id  int                                null,
    message  varchar(200)                       null,
    log_time datetime default CURRENT_TIMESTAMP null,
    constraint logs_log_types_id_fk
        foreign key (type_id) references log_types (id)
            on update cascade on delete set null
);

create table users
(
    id                 int auto_increment
        primary key,
    nick               varchar(250)                       null,
    user_password      text                               null,
    last_connection    datetime default CURRENT_TIMESTAMP null,
    port               int                                null,
    last_disconnection datetime                           null,
    constraint users_nick_uindex
        unique (nick)
);
