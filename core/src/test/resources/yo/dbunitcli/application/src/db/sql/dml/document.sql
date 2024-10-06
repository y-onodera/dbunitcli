delete from document where id = 1 ;
delete from document where id = 2 ;
delete from document where id = 3 ;

insert into document (id ,name ,owner ,version ,created ) values (1 ,'test.xlsx' ,'山田　太郎' ,11 ,'2022-01-01 12:24:00.0' );
insert into document (id ,name ,owner ,version ,created ) values (2 ,'test.xls' ,'鈴木　一郎' ,12 ,'2022-02-01 02:12:20.0' );
insert into document (id ,name ,owner ,version ,created ) values (3 ,'test.csv' ,'山田　花子' ,3 ,'2021-12-11 10:04:15.0' );
commit;