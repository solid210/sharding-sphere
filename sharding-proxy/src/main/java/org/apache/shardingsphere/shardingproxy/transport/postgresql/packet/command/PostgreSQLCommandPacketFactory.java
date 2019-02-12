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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.admin.PostgreSQLUnsupportedCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLComTerminationPacket;

import java.sql.SQLException;

/**
 * PostgreSQL command packet factory.
 *
 * @author zhangyonglun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLCommandPacketFactory {
    
    /**
     * Create new instance of PostgreSQL command packet.
     *
     * @param payload PostgreSQL packet payload
     * @param backendConnection backend connection
     * @return command packet
     * @throws SQLException SQL exception
     */
    public static PostgreSQLCommandPacket newInstance(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        int commandPacketTypeValue = payload.readInt1();
        PostgreSQLCommandPacketType type = PostgreSQLCommandPacketType.valueOf(commandPacketTypeValue);
        switch (type) {
            case QUERY:
                return new PostgreSQLComQueryPacket(payload, backendConnection);
            case PARSE:
                return new PostgreSQLComParsePacket(payload, backendConnection);
            case BIND:
                return new PostgreSQLComBindPacket(payload, backendConnection);
            case DESCRIBE:
                return new PostgreSQLComDescribePacket(payload);
            case EXECUTE:
                return new PostgreSQLComExecutePacket(payload);
            case SYNC:
                return new PostgreSQLComSyncPacket(payload);
            case TERMINATE:
                return new PostgreSQLComTerminationPacket(payload);
            default:
                return new PostgreSQLUnsupportedCommandPacket(type.getValue());
        }
    }
}
