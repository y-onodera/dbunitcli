update Test2 set ColumnA = 'ああ' ,ColumnB = '10000'  where PrimaryKey = '1' ;
update Test2 set ColumnA = 'テスト' ,ColumnB = null  where PrimaryKey = '15' ;
update Test2 set ColumnA = null ,ColumnB = null  where PrimaryKey = '29' ;
commit;