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

package org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.clause.facade;

import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.parser.clause.InsertColumnsClauseParser;
import org.apache.shardingsphere.core.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertDuplicateKeyUpdateClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertIntoClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertSetClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertValuesClauseParser;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public PostgreSQLInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new PostgreSQLInsertValuesClauseParser(shardingRule, lexerEngine), new PostgreSQLInsertSetClauseParser(shardingRule, lexerEngine), 
                new PostgreSQLInsertDuplicateKeyUpdateClauseParser(shardingRule, lexerEngine));
    }
}
