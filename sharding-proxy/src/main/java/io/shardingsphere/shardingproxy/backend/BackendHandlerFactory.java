/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.SetStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowOtherStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.shardingproxy.backend.jdbc.JDBCBackendHandler;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.backend.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.shardingproxy.backend.jdbc.wrapper.PreparedStatementExecutorWrapper;
import io.shardingsphere.shardingproxy.backend.jdbc.wrapper.StatementExecutorWrapper;
import io.shardingsphere.shardingproxy.backend.netty.NettyBackendHandler;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Backend handler factory.
 *
 * @author zhangliang
 * @author panjuan
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendHandlerFactory {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param connectionId connection ID of database connected
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public static BackendHandler newTextProtocolInstance(
            final int connectionId, final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        return GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)
                ? new NettyBackendHandler(logicSchema, connectionId, sequenceId, sql, databaseType)
                : new JDBCBackendHandler(logicSchema, sql, new JDBCExecuteEngine(backendConnection, new StatementExecutorWrapper(logicSchema)));
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param connectionId connection ID of database connected
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param parameters SQL parameters
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public static BackendHandler newBinaryProtocolInstance(final int connectionId, final int sequenceId, final String sql, final List<Object> parameters,
                                                           final BackendConnection backendConnection, final DatabaseType databaseType) {
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        return GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)
                ? new NettyBackendHandler(logicSchema, connectionId, sequenceId, sql, databaseType)
                : new JDBCBackendHandler(logicSchema, sql, new JDBCExecuteEngine(backendConnection, new PreparedStatementExecutorWrapper(logicSchema, parameters)));
    }
    
    /**
     * Create new instance by sql judge.
     * 
     * @param connectionId connection ID of database connected
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @param frontendHandler frontend handler
     * @return instance of backend handler
     */
    public static BackendHandler createBackendHandler(
            final int connectionId, final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType, final FrontendHandler frontendHandler) {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        if (SQLType.DCL == sqlStatement.getType() || sqlStatement instanceof SetStatement) {
            return new SchemaBroadcastBackendHandler(connectionId, sequenceId, sql, backendConnection, databaseType);
        }
        
        if (sqlStatement instanceof UseStatement || sqlStatement instanceof ShowDatabasesStatement) {
            return new SchemaIgnoreBackendHandler(sqlStatement, frontendHandler);
        }
        
        if (sqlStatement instanceof ShowOtherStatement) {
            return new SchemaUnicastBackendHandler(connectionId, sequenceId, sql, backendConnection, DatabaseType.MySQL);
        }
        
        return newTextProtocolInstance(connectionId, sequenceId, sql, backendConnection, DatabaseType.MySQL);
    }
}
