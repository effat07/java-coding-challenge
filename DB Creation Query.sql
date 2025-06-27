create database ordermanagementsystem;
use ordermanagementsystem;

create table Product (
    productid int primary key,
    productname varchar(100),
    description text,
    price double,
    quantityinstock int,
    type enum('electronics', 'clothing')
);

create table Electronics (
    productid int primary key,
    brand varchar(100),
    warrantyperiod int,
    foreign key (productid) references Product(productid)
);

create table Clothing (
    productid int primary key,
    size varchar(10),
    color varchar(30),
    foreign key (productid) references Product(productid)
);

create table User (
    userid int primary key,
    username varchar(100) unique,
    password varchar(100),
    role enum ('admin', 'user')
);

create table Orders (
    orderid int primary key auto_increment,
    userid int,
    orderdate timestamp default current_timestamp,
    foreign key (userid) references User(userid)
);

create table OrderProduct (
    orderid int,
    productid int,
    quantity int,
    primary key (orderid, productid),
    foreign key (orderid) references Orders(orderid),
    foreign key (productid) references Product(productid)
);

insert into Product values (1, 'iphone 14', 'apple smartphone', 79999, 10, 'electronics'),
 (2, 'sony headphones', 'noise cancelling headphones', 9999, 25, 'electronics'),
 (3, 't-shirt', 'cotton t-shirt', 599, 50, 'clothing'),
(4, 'jeans', 'denim jeans', 1199, 30, 'clothing');

insert into Electronics values (1, 'apple', 12),(2, 'sony', 24);
insert into Clothing values (3, 'm', 'blue'), (4, '32', 'black');

insert into User values (101, 'Riya Bose', 'admin123', 'admin'), (102, 'John David', 'john123', 'user'), (103, 'Alice Smith', 'alice456', 'user');

insert into Orders (userid) values (102), (103);

insert into OrderProduct values (1, 1, 1), (2, 3, 2), (2, 4, 1);


select * from Product;
desc Product;
select * from Electronics;
select * from Clothing;
select * from User;
select * from Orders;
select * from OrderProduct;

