--// add image grab fields
-- Migration SQL that makes the change goes here.

alter table Echo
    add imageWidth int,
    add imageHeight int,
    add originalImageUrl varchar(1024),
    add imageGrabbedOn timestamp;

alter table EchoPossibility
    add imageWidth int,
    add imageHeight int,
    add originalImageUrl varchar(1024),
    add imageGrabbedOn timestamp;

--//@UNDO
-- SQL to undo the change goes here.

alter table Echo
    drop imageWidth,
    drop imageHeight,
    drop originalImageUrl,
    drop imageGrabbedOn;

alter table EchoPossibility
    drop imageWidth,
    drop imageHeight,
    drop originalImageUrl,
    drop imageGrabbedOn;
