update Test3 set ColumnA = 'ああ' ,ColumnB = '10000'  where PrimaryKey = '1' and PrimaryKey2 = '1' ;
update Test3 set ColumnA = 'テスト' ,ColumnB = null  where PrimaryKey = '1' and PrimaryKey2 = '2' ;
update Test3 set ColumnA = null ,ColumnB = null  where PrimaryKey = '29' and PrimaryKey2 = '1' ;
commit;