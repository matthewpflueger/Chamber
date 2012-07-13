--// Updated Image Table to handle Exhibit Size
-- Migration SQL that makes the change goes here.


alter table Image
    add column exhibitUrl varchar(255) null,
    add column exhibitWidth int not null default 0,
    add column exhibitHeight int not null default 0;


--//@UNDO
-- SQL to undo the change goes here.

alter table Image
    drop column exhibitUrl,
    drop column exhibitWidth,
    drop column exhibitHeight;



