select * from databasechangelog;
DELETE FROM DATABASECHANGELOGLOCK where ID = 1;

select * from items_in_order;
select * from orders;
select * from items;
delete from items_in_order;