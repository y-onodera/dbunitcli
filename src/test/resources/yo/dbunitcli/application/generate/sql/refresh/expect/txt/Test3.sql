delete from Test3 where PrimaryKey = '1' and PrimaryKey2 = '1' ;
delete from Test3 where PrimaryKey = '1' and PrimaryKey2 = '2' ;
delete from Test3 where PrimaryKey = '29' and PrimaryKey2 = '1' ;

insert into Test3 (PrimaryKey ,PrimaryKey2 ,ColumnA ,ColumnB ) values ('1' ,'1' ,'ああ' ,'10000' );
insert into Test3 (PrimaryKey ,PrimaryKey2 ,ColumnA ,ColumnB ) values ('1' ,'2' ,'テスト' ,'' );
insert into Test3 (PrimaryKey ,PrimaryKey2 ,ColumnA ,ColumnB ) values ('29' ,'1' ,'' ,'' );
commit;