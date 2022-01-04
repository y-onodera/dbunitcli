update Test1 set Column1 = 'ああ' ,Column2 = 10000 ,Column3 = 'false' ,now = sysdate()  where Key = '1' ;
update Test1 set Column1 = 'テスト' ,Column2 = null ,Column3 = 'true' ,now = sysdate()  where Key = '15' ;
update Test1 set Column1 = null ,Column2 = null ,Column3 = null ,now = sysdate()  where Key = '29' ;
commit;