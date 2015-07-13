#SJON-SQL spec

The SJON-SQL is a utility for convertion of the SJON format files to an (sqlite) database.

For this we need to describe the schema of the database and transfer the data to the database.
The only current parameter of the utility is a folder, containing the files to be converted to the database (in SJON format).
The output of the utility is a stream of SQL statements for creation of the database schema and data insertion. These are just raw statements and will be used
according to your requirements; for example, currently, I separate these statements (also separated by newline if there are for different tables and schema) 
into different sql files for better reuse.

One file should be named "schema.sjon", and unsuprisingly, contains the schema description, which will subsequently be converted to SQL CREATE TABLE statements. The format of each of its SJON records is:

```
schema-definition-record := {type:relation,name:<table-name>,fields:<domain-definition>,(<field-name>:<foreign-key-definition>)*}
domain-definition := [<field-definition-commalist>]
field-definition := [<field-name>,<field-type>,<constraint>*]
field-type := int | string | boolean
constraint := "primary key" | "foreign key"
foreign-key-definition := {table:<table-name>,references:<field-name>}
```

The only "type" of database object currently supported is that of a table (relation in formal terminology). The types of database values currently supported
are those described in the grammar. The boolean values represented in an SJON file are expected to be either "true" or "false", and in the actual SQL statement
will be converted to 1 and 0 integer values respectively. Also quotes will be added to the string values in the SQL statements.

In case of a foreign key constraint, we need to add a foreign key definition for each field that is a foreign key.

The convention is that, for each table that is defined in the schema.sjon file, a separate <table-name>.sjon file is expected to be found in the directory.
The definition of each of the records in these table data files are custom to each table and straightforward, and will be converted to separate SQL INSERTION statements.
