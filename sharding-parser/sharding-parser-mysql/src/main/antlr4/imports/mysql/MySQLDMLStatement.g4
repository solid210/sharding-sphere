grammar MySQLDMLStatement;

import MySQLKeyword, Keyword, Symbol, MySQLDQLStatement, MySQLBase, BaseRule, DataType;

insert
    : INSERT (LOW_PRIORITY | DELAYED | HIGH_PRIORITY)? IGNORE? INTO? tableName (PARTITION ignoredIdentifiers_)? (setClause | columnClause | selectClause) onDuplicateKeyClause?
    ;

setClause
    : SET assignmentList
    ;

columnClause
    : columnNames? valueClause
    ;

valueClause
    : (VALUES | VALUE) assignmentValueList (COMMA_ assignmentValueList)*
    ;

selectClause
    : columnNames? select
    ;

onDuplicateKeyClause
    : ON DUPLICATE KEY UPDATE assignmentList
    ;

update
    : updateClause setClause whereClause?
    ;

updateClause
    : UPDATE LOW_PRIORITY? IGNORE? tableReferences
    ;

delete
    : deleteClause whereClause?
    ;

deleteClause
    : DELETE LOW_PRIORITY? QUICK? IGNORE? (fromMulti | fromSingle) 
    ;

fromSingle
    : FROM tableName (PARTITION ignoredIdentifiers_)?
    ;

fromMulti
    : fromMultiTables FROM tableReferences | FROM fromMultiTables USING tableReferences
    ;

fromMultiTables
    : fromMultiTable (COMMA_ fromMultiTable)*
    ;

fromMultiTable
    : tableName DOT_ASTERISK_?
    ;
