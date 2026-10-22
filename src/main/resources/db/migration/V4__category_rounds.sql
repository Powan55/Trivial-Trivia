alter table rounds add column selected_category varchar(60);
alter table rounds add column selected_difficulty varchar(6);

insert into categories (name) select 'Arts and entertainment' where not exists (select 1 from categories where name = 'Arts and entertainment');
insert into categories (name) select 'Geography' where not exists (select 1 from categories where name = 'Geography');
insert into categories (name) select 'History' where not exists (select 1 from categories where name = 'History');
insert into categories (name) select 'Science' where not exists (select 1 from categories where name = 'Science');
insert into categories (name) select 'Uncategorized' where not exists (select 1 from categories where name = 'Uncategorized');

update questions set category_id = (select id from categories where name = 'Geography'), difficulty = 'EASY'
where text = 'What is the capital of France?' and option_1 = 'London' and option_2 = 'Berlin' and option_3 = 'Madrid' and option_4 = 'Paris' and answer = 'Paris'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Science'), difficulty = 'EASY'
where text = 'Which planet is known as the Red Planet?' and option_1 = 'Venus' and option_2 = 'Jupiter' and option_3 = 'Mars' and option_4 = 'Saturn' and answer = 'Mars'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Arts and entertainment'), difficulty = 'EASY'
where text = 'Who is the author of "Romeo and Juliet"?' and option_1 = 'Jane Austen' and option_2 = 'Charles Dickens' and option_3 = 'Mark Twain' and option_4 = 'William Shakespeare' and answer = 'William Shakespeare'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Science'), difficulty = 'EASY'
where text = 'Which gas do plants absorb from the atmosphere?' and option_1 = 'Oxygen (O2)' and option_2 = 'Nitrogen (N2)' and option_3 = 'Carbon dioxide (CO2)' and option_4 = 'Hydrogen (H2)' and answer = 'Carbon dioxide (CO2)'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Science'), difficulty = 'EASY'
where text = 'What is the largest mammal in the world?' and option_1 = 'African Elephant' and option_2 = 'Giraffe' and option_3 = 'Blue Whale' and option_4 = 'Lion' and answer = 'Blue Whale'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'History'), difficulty = 'EASY'
where text = 'In which year did Christopher Columbus first arrive in the Americas?' and option_1 = '1492' and option_2 = '1776' and option_3 = '1066' and option_4 = '1969' and answer = '1492'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Science'), difficulty = 'EASY'
where text = 'What is the chemical symbol for gold?' and option_1 = 'Ag' and option_2 = 'Ge' and option_3 = 'Au' and option_4 = 'Fe' and answer = 'Au'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Arts and entertainment'), difficulty = 'EASY'
where text = 'Who painted the Mona Lisa?' and option_1 = 'Vincent van Gogh' and option_2 = 'Pablo Picasso' and option_3 = 'Michelangelo' and option_4 = 'Leonardo da Vinci' and answer = 'Leonardo da Vinci'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Science'), difficulty = 'EASY'
where text = 'What is the largest organ in the human body?' and option_1 = 'Heart' and option_2 = 'Brain' and option_3 = 'Skin' and option_4 = 'Liver' and answer = 'Skin'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));

update questions set category_id = (select id from categories where name = 'Science'), difficulty = 'EASY'
where text = 'Which gas do humans exhale when they breathe out?' and option_1 = 'Oxygen (O2)' and option_2 = 'Nitrogen (N2)' and option_3 = 'Carbon dioxide (CO2)' and option_4 = 'Hydrogen (H2)' and answer = 'Carbon dioxide (CO2)'
and difficulty = 'MEDIUM' and (category_id is null or category_id = (select id from categories where name = 'Uncategorized'));
