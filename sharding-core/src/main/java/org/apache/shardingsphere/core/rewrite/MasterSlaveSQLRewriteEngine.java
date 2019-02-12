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

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;
import org.apache.shardingsphere.core.parsing.parser.token.SchemaToken;
import org.apache.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;

import java.util.Collections;
import java.util.List;

/**
 * SQL rewrite engine for master slave rule.
 * 
 * <p>should rewrite schema name.</p>
 * 
 * @author chenqingyang
 * @author panjuan
 */
public final class MasterSlaveSQLRewriteEngine {
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final String originalSQL;
    
    private final List<SQLToken> sqlTokens;
    
    private final ShardingMetaData metaData;
    
    /**
     * Constructs master slave SQL rewrite engine.
     * 
     * @param masterSlaveRule master slave rule
     * @param originalSQL original SQL
     * @param sqlStatement SQL statement
     * @param metaData meta data
     */
    public MasterSlaveSQLRewriteEngine(final MasterSlaveRule masterSlaveRule, final String originalSQL, final SQLStatement sqlStatement, final ShardingMetaData metaData) {
        this.masterSlaveRule = masterSlaveRule;
        this.originalSQL = originalSQL;
        sqlTokens = sqlStatement.getSQLTokens();
        this.metaData = metaData;
    }
    
    /**
     * Rewrite SQL.
     * 
     * @return SQL
     */
    public String rewrite() {
        if (sqlTokens.isEmpty()) {
            return originalSQL;
        }
        SQLBuilder result = new SQLBuilder(Collections.emptyList());
        int count = 0;
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                result.appendLiterals(originalSQL.substring(0, each.getStartIndex()));
            }
            if (each instanceof SchemaToken) {
                appendSchemaPlaceholder(originalSQL, result, (SchemaToken) each, count);
            }
            count++;
        }
        return result.toSQL(masterSlaveRule, metaData.getDataSource());
    }
    
    private void appendSchemaPlaceholder(final String sql, final SQLBuilder sqlBuilder, final SchemaToken schemaToken, final int count) {
        String schemaName = originalSQL.substring(schemaToken.getStartIndex(), schemaToken.getStopIndex() + 1);
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder(schemaName.toLowerCase(), null));
        int endPosition = sqlTokens.size() - 1 == count ? sql.length() : sqlTokens.get(count + 1).getStartIndex();
        sqlBuilder.appendLiterals(sql.substring(schemaToken.getStopIndex() + 1, endPosition));
    }
}
