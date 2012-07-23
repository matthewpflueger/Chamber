
-- use a self join
update EchoedUser as EU inner join EchoedUser as EU1 on EU.id = EU1.id set
    EU.updatedOnLong = EU1.updatedOn + 0,
    EU.createdOnLong = EU1.createdOn + 0;