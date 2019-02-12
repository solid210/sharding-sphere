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

package org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.sql;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerOffsetClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.SQLServerTopClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.facade.SQLServerSelectClauseParserFacade;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.AbstractSelectParser;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Select parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerSelectParser extends AbstractSelectParser {
    
    private final SQLServerTopClauseParser topClauseParser;
    
    private final SQLServerOffsetClauseParser offsetClauseParser;
    
    public SQLServerSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        super(shardingRule, lexerEngine, new SQLServerSelectClauseParserFacade(shardingRule, lexerEngine), shardingTableMetaData);
        topClauseParser = new SQLServerTopClauseParser(lexerEngine);
        offsetClauseParser = new SQLServerOffsetClauseParser(lexerEngine);
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseTop(selectStatement);
        parseSelectList(selectStatement, getItems());
        parseFrom(selectStatement);
        parseWhere(getShardingRule(), selectStatement, getItems());
        parseGroupBy(selectStatement);
        parseHaving();
        parseOrderBy(selectStatement);
        parseOffset(selectStatement);
        parseSelectRest();
    }
    
    private void parseTop(final SelectStatement selectStatement) {
        topClauseParser.parse(selectStatement);
    }
    
    private void parseOffset(final SelectStatement selectStatement) {
        offsetClauseParser.parse(selectStatement);
    }
}
