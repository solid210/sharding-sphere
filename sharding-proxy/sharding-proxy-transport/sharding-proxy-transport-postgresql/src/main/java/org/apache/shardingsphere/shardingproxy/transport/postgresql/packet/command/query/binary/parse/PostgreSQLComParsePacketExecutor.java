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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.parse;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.SQLParsingEngine;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;

import java.util.Collection;
import java.util.Collections;

/**
 * PostgreSQL command parse packet executor.
 *
 * @author zhangyonglun
 * @author zhangliang
 */
public final class PostgreSQLComParsePacketExecutor implements CommandPacketExecutor<PostgreSQLPacket> {
    
    @Override
    public Collection<PostgreSQLPacket> execute(final BackendConnection backendConnection, final CommandPacket commandPacket) {
        PostgreSQLComParsePacket comParsePacket = (PostgreSQLComParsePacket) commandPacket;
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        // TODO we should use none-sharding parsing engine in future.
        SQLParsingEngine sqlParsingEngine = new SQLParsingEngine(DatabaseType.PostgreSQL, comParsePacket.getSql(), getShardingRule(logicSchema), logicSchema.getMetaData().getTable());
        if (!comParsePacket.getSql().isEmpty()) {
            SQLStatement sqlStatement = sqlParsingEngine.parse(true);
            int parametersIndex = sqlStatement.getParametersIndex();
            comParsePacket.getBinaryStatementRegistry().register(comParsePacket.getStatementId(), comParsePacket.getSql(), parametersIndex, comParsePacket.getBinaryStatementParameterTypes());
        }
        return Collections.<PostgreSQLPacket>singletonList(new PostgreSQLParseCompletePacket());
    }
    
    private ShardingRule getShardingRule(final LogicSchema logicSchema) {
        return logicSchema instanceof MasterSlaveSchema ? ((MasterSlaveSchema) logicSchema).getDefaultShardingRule() : ((ShardingSchema) logicSchema).getShardingRule();
    }
}
