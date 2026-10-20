alter table rounds add column owner_token varchar(128);
alter table rounds add column total_questions integer not null default 0;

create table round_questions (
    round_id integer not null references rounds(id),
    position integer not null,
    text varchar not null,
    option_1 varchar not null,
    option_2 varchar not null,
    option_3 varchar not null,
    option_4 varchar not null,
    answer varchar not null,
    served_at timestamp,
    submitted_answer varchar,
    correct boolean,
    answered_at timestamp,
    primary key (round_id, position),
    check (position > 0)
);
