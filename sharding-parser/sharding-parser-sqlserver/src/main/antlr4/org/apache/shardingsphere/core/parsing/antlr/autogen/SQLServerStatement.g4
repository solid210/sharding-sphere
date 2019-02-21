grammar SQLServerStatement;

import SQLServerKeyword, Keyword, Symbol, SQLServerBase, SQLServerDDLStatement, SQLServerTCLStatement, SQLServerDCLStatement;

execute
    : (createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | setTransaction
    | beginTransaction
    | setAutoCommit
    | commit
    | rollback
    | savepoint
    | grant
    | revoke
    | deny
    | createUser
    | dropUser
    | alterUser
    | createRole
    | dropRole
    | alterRole
    | createLogin
    | dropLogin
    | alterLogin
    ) SEMI_?
    ;
