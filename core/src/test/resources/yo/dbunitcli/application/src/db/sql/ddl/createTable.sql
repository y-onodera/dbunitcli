CREATE TABLE document (
    id integer primary key
  , name character varying(40)
  , owner character varying(40)
  , version integer
  , created timestamp
)