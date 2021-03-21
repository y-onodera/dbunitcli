delete from Test2 where PrimaryKey = '1' ;
delete from Test2 where PrimaryKey = '15' ;
delete from Test2 where PrimaryKey = '29' ;

insert into Test2 (PrimaryKey ,ColumnA ,ColumnB ) values ('1' ,'ああ' ,'10000' );
insert into Test2 (PrimaryKey ,ColumnA ,ColumnB ) values ('15' ,'テスト' ,null );
insert into Test2 (PrimaryKey ,ColumnA ,ColumnB ) values ('29' ,null ,null );
commit;