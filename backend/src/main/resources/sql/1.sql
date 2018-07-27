-- auto-generated definition
create table issues
(
  id      varchar(255) default '' :: character varying not null
    constraint issues_pkey
    primary key,
  link    varchar(255) default '' :: character varying not null,
  is_done integer default 0                            not null
);


CREATE TABLE public.articles
(
  id VARCHAR(255) DEFAULT '' PRIMARY KEY NOT NULL,
  issue VARCHAR(255) DEFAULT '' NOT NULL,
  title VARCHAR(512) DEFAULT '' NOT NULL,
  authors text default '' not null ,
  authorInfo text default '' not null ,
  mail text default '' not null ,
  page varchar(255) default '' not null ,
  abs text default '' not null ,
  index text default '' not null ,
  fullText VARCHAR(255) default '' not null
);