drop table users;
select * from databasechangelog;
DELETE FROM DATABASECHANGELOGLOCK where ID = 1;

select * from items_in_order;
select * from orders;
select * from items;
delete from items_in_order;

select * from users;
select * from items_in_cart;
delete from users;
delete from items_in_cart;
update orders set login='user';

SELECT items_in_cart.id, items_in_cart.title, items_in_cart.count, items_in_cart.price, items_in_cart.description, items_in_cart.image_path, items_in_cart.login, items_in_cart.item_id
FROM items_in_cart WHERE items_in_cart.login = 'user';

insert into items_in_cart(title, description, price, count, item_id, login, image_path)
   values('Товар№1', 'Тестовый товар номер один', 10, 4, 1, 'user', 'http://localhost:8084/items/image/1');

merge into users as u
using (select 'user3' login, '' password, 'USER' roles) as new_user
on u.login = new_user.login
when not matched then
    insert(login, password, roles)
    values(new_user.login, new_user.password, new_user.roles)
when matched then
update set login=new_user.login, password=new_user.password, roles=new_user.roles;

merge into items_in_cart as c
using (select 'Товар№1' title, 'Тестовый товар номер один' description, 10 price, 2 count, 1 item_id, '' image_path, 'user' login) as i
on c.login = i.login and c.item_id = i.item_id
when not matched then
    insert(title, description, price, count, item_id, image_path, login)
    values(i.title, i.description, i.price, i.count, i.item_id, i.image_path, i.login)
when matched then
    update set count=i.count;

SELECT items_in_cart.id, items_in_cart.title, items_in_cart.count, items_in_cart.price, items_in_cart.description, items_in_cart.image_path, items_in_cart.login, items_in_cart.item_id
FROM items_in_cart WHERE items_in_cart.item_id = 1 AND (UPPER(items_in_cart.login) = UPPER('user'))