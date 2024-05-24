drop table coordinates cascade;
drop table person cascade;
drop table dragon cascade;
drop table users;


create table coordinates(
	coordinates_id serial primary key,
	x bigint not null,
	y real	
);

create table person(
	person_id serial primary key,
	person_name varchar(255) not null,
	passport_id varchar(255),
	eye_color varchar(255) not null check (eye_color in ('GREEN', 'YELLOW', 'ORANGE', 'RED', 'BLACK', 'BLUE')),
	hair_color varchar(255) check (hair_color in ('GREEN', 'YELLOW', 'ORANGE', 'RED', 'BLACK', 'BLUE', null)),
	nationality varchar(255) not null check (nationality in ('USA', 'VATICAN', 'ITALY', 'THAILAND', 'SOUTH_KOREA')),
	countKilledDragons bigint check (countKilledDragons > 0)
);

create table users(
	user_id serial primary key,
	login text unique not null,
	password text not null
);

create table dragon(
	dragon_id serial primary key,
	dragon_name varchar(255) not null,
	coordinates integer references coordinates not null,
	creation_date date not null,
	age bigint not null check (age > 0),
	weight bigint not null check (age > 0),
	speaking boolean,
	type varchar(255) check (type in ('WATER', 'UNDERGROUND', 'AIR', 'FIRE', NULL)),
	killer integer references person,
	owner integer references users
);

insert into users(login, password)
values
    ('Kseniya123', '22e7e9d85b7fe6004f7b9f3aa592ea9ec9ce098682e8192fa83785f1784c768d1d1ac3b8afcae88666f66aec24739ac133e9d4adc7506f1a5f1f6078cb27c674');


insert into coordinates(x, y)
values 
	(1, 1),
	(2, 2),
	(10, 15),
	(100, 100),
	(10, 12);

insert into person (person_name, passport_id, eye_color, hair_color, nationality, countKilledDragons) 
values 
	('Vasya', 'wedgywq1231', 'RED', 'RED', 'USA', 100),
	('Petya', 'wedgy31', 'BLACK', 'ORANGE', 'ITALY', 4),
	('Sergey', 'djsfs9', 'YELLOW', 'RED', 'USA', 1);
	
insert into dragon (dragon_name, coordinates, creation_date, age, weight, speaking, type, killer, owner)
values
	('Katya', 1, '2024-03-13', 10, 23, true, 'AIR', 1, 1),
	('Oleg', 2, '2024-03-13', 10, 23, true, 'FIRE', 2, 1),
	('Artem', 3, '2024-03-13', 10, 23, true, 'WATER', null, 1),
	('Zhora', 4, '2024-03-13', 10, 23, true, 'AIR', 3, 1),
	('Kristina', 5, '2024-03-13', 10, 23, true, 'FIRE', null, 1);
	
	







