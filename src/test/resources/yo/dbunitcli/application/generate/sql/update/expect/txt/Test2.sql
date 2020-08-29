update Test2 set PrimaryKey = '1' ,ColumnA = 'ああ' ,ColumnB = '10000'  where PrimaryKey = '1' ;
update Test2 set PrimaryKey = '15' ,ColumnA = 'テスト' ,ColumnB = ''  where PrimaryKey = '15' ;
update Test2 set PrimaryKey = '29' ,ColumnA = '' ,ColumnB = ''  where PrimaryKey = '29' ;
commit;