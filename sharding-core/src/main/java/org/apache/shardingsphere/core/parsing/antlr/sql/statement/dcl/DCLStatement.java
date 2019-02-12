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

package org.apache.shardingsphere.core.parsing.antlr.sql.statement.dcl;

import lombok.ToString;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.lexer.token.TokenType;
import org.apache.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;

import java.util.Arrays;
import java.util.Collection;

/**
 * DCL statement.
 *
 * @author panjuan
 */
@ToString(callSuper = true)
public class DCLStatement extends AbstractSQLStatement {
    
    private static final Collection<Keyword> STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.GRANT, DefaultKeyword.REVOKE, DefaultKeyword.DENY);
    
    private static final Collection<Keyword> PRIMARY_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.CREATE, DefaultKeyword.ALTER, DefaultKeyword.DROP, DefaultKeyword.RENAME);
    
    private static final Collection<Keyword> SECONDARY_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.LOGIN, DefaultKeyword.USER, DefaultKeyword.ROLE);
    
    public DCLStatement() {
        super(SQLType.DCL);
    }
    
    /**
     * Is DCL statement.
     *
     * @param primaryTokenType primary token type
     * @param secondaryTokenType secondary token type
     * @return is DCL or not
     */
    public static boolean isDCL(final TokenType primaryTokenType, final TokenType secondaryTokenType) {
        return STATEMENT_PREFIX.contains(primaryTokenType) || (PRIMARY_STATEMENT_PREFIX.contains(primaryTokenType) && SECONDARY_STATEMENT_PREFIX.contains(secondaryTokenType));
    }
    
    /**
     * Judge whether contain star for table.
     *
     * @return contain star or not.
     */
    public boolean isGrantForSingleTable() {
        return !getTables().isEmpty() && !getTables().getSingleTableName().equals(Symbol.STAR.getLiterals());
    }
}
