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

package org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.facade;

import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.parser.clause.HavingClauseParser;
import org.apache.shardingsphere.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleGroupByClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleOrderByClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleSelectListClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleSelectRestClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleTableReferencesClauseParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Select clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public OracleSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleSelectListClauseParser(shardingRule, lexerEngine),
                new OracleTableReferencesClauseParser(shardingRule, lexerEngine), new OracleWhereClauseParser(lexerEngine), new OracleGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new OracleOrderByClauseParser(lexerEngine), new OracleSelectRestClauseParser(lexerEngine));
    }
}
