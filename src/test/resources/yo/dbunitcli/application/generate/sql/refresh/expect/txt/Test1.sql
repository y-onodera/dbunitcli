delete from Test1 where Key = '1' ;
delete from Test1 where Key = '15' ;
delete from Test1 where Key = '29' ;

insert into Test1 (Key ,Column1 ,Column2 ,Column3 ,now ) values ('1' ,'ああ' ,'10000' ,'false' ,sysdate() );
insert into Test1 (Key ,Column1 ,Column2 ,Column3 ,now ) values ('15' ,'テスト' ,'' ,'true' ,sysdate() );
insert into Test1 (Key ,Column1 ,Column2 ,Column3 ,now ) values ('29' ,'' ,'' ,'' ,sysdate() );
commit;