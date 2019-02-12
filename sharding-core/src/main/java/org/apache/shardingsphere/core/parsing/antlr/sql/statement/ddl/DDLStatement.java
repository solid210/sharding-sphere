/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl;

import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.TokenType;
import org.apache.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;

import java.util.Arrays;
import java.util.Collection;

/**
 * DDL statement.
 *
 * @author zhangliang
 */
public class DDLStatement extends AbstractSQLStatement {
    
    private static final Collection<Keyword> PRIMARY_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.CREATE, DefaultKeyword.ALTER, DefaultKeyword.DROP, DefaultKeyword.TRUNCATE);
    
    private static final Collection<Keyword> NOT_SECONDARY_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.LOGIN, DefaultKeyword.USER, DefaultKeyword.ROLE);
    
    public DDLStatement() {
        super(SQLType.DDL);
    }
    
    /**
     * Is DDL statement.
     *
     * @param primaryTokenType primary token type
     * @param secondaryTokenType secondary token type
     * @return is DDL or not
     */
    public static boolean isDDL(final TokenType primaryTokenType, final TokenType secondaryTokenType) {
        return PRIMARY_STATEMENT_PREFIX.contains(primaryTokenType) && !NOT_SECONDARY_STATEMENT_PREFIX.contains(secondaryTokenType);
    }
}
