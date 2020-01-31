CREATE TABLE ACCOUNT  (
id INTEGER IDENTITY not null ,
balance numeric , PRIMARY KEY (id)
);

CREATE TABLE TRANSACTION     (
id INTEGER IDENTITY not null,
originator_Id INTEGER not null,
beneficiary_Id INTEGER not null,
transfer_amount number not null,
status varchar2(30),
PRIMARY KEY ( id )
);
