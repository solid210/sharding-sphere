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

package org.apache.shardingsphere.core.parsing.parser.sql;

import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Conditions;
import org.apache.shardingsphere.core.parsing.parser.context.table.Tables;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;

import java.util.List;

/**
 * SQL statement.
 *
 * @author zhangliang
 */
public interface SQLStatement {
    
    /**
     * Get SQL type.
     *
     * @return SQL type
     */
    SQLType getType();
    
    /**
     * Get tables.
     * 
     * @return tables
     */
    Tables getTables();
    
    /**
     * Get route conditions.
     *
     * @return conditions
     */
    Conditions getRouteConditions();
    
    /**
     * Get encrypt conditions.
     *
     * @return conditions
     */
    Conditions getEncryptConditions();
    
    /**
     * Add SQL token.
     *
     * @param sqlToken SQL token
     */
    void addSQLToken(SQLToken sqlToken);
    
    /**
     * Get SQL tokens.
     * 
     * @return SQL tokens
     */
    List<SQLToken> getSQLTokens();
    
    /**
     * Get index of parameters.
     *
     * @return index of parameters
     */
    int getParametersIndex();
    
    /**
     * Increase parameters index.
     */
    void increaseParametersIndex();
    
    /**
     * Get logic SQL.
     * 
     * @return logic SQL
     */
    String getLogicSQL();
}
