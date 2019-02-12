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

package org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause;

import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.parser.clause.TableReferencesClauseParser;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Table references clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerTableReferencesClauseParser extends TableReferencesClauseParser {
    
    public SQLServerTableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
        parseTableSampleClause();
        parseTableHint(sqlStatement);
    }
    
    private void parseTableSampleClause() {
        getLexerEngine().unsupportedIfEqual(SQLServerKeyword.TABLESAMPLE);
    }
    
    private void parseTableHint(final SQLStatement sqlStatement) {
        if (getLexerEngine().skipIfEqual(DefaultKeyword.WITH)) {
            getLexerEngine().skipParentheses(sqlStatement);
        }
    }
    
    @Override
    protected Keyword[] getKeywordsForJoinType() {
        return new Keyword[] {SQLServerKeyword.APPLY, SQLServerKeyword.REDUCE, SQLServerKeyword.REPLICATE, SQLServerKeyword.REDISTRIBUTE};
    }
}
