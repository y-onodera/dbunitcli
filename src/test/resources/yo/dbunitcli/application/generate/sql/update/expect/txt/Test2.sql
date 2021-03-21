update Test2 set PrimaryKey = '1' ,ColumnA = 'ああ' ,ColumnB = '10000'  where PrimaryKey = '1' ;
update Test2 set PrimaryKey = '15' ,ColumnA = 'テスト' ,ColumnB = null  where PrimaryKey = '15' ;
update Test2 set PrimaryKey = '29' ,ColumnA = null ,ColumnB = null  where PrimaryKey = '29' ;
commit;