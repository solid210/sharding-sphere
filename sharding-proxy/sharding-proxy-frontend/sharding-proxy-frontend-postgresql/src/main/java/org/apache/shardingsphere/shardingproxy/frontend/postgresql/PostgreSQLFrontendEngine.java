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

package org.apache.shardingsphere.shardingproxy.frontend.postgresql;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.frontend.spi.DatabaseFrontendEngine;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.QueryCommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketExecutorFactory;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketTypeLoader;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLAuthenticationOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLComStartupPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLConnectionIdGenerator;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLSSLNegativePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.payload.PostgreSQLPacketPayload;

import java.sql.SQLException;
import java.util.Collection;

/**
 * PostgreSQL frontend engine.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
@Slf4j
public final class PostgreSQLFrontendEngine implements DatabaseFrontendEngine {
    
    private static final int SSL_REQUEST_PAYLOAD_LENGTH = 8;
    
    private static final int SSL_REQUEST_CODE = 80877103;
    
    private static final String DATABASE_NAME_KEYWORD = "database";
    
    @Override
    public String getDatabaseType() {
        return DatabaseType.PostgreSQL.name();
    }
    
    @Override
    public boolean isOccupyThreadForPerConnection() {
        return true;
    }
    
    @Override
    public void handshake(final ChannelHandlerContext context, final BackendConnection backendConnection) {
        int connectionId = PostgreSQLConnectionIdGenerator.getInstance().nextId();
        backendConnection.setConnectionId(connectionId);
        BinaryStatementRegistry.getInstance().register(connectionId);
    }
    
    @Override
    public boolean auth(final ChannelHandlerContext context, final ByteBuf message, final BackendConnection backendConnection) {
        if (SSL_REQUEST_PAYLOAD_LENGTH == message.markReaderIndex().readInt() && SSL_REQUEST_CODE == message.readInt()) {
            context.writeAndFlush(new PostgreSQLSSLNegativePacket());
            return false;
        }
        message.resetReaderIndex();
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(message)) {
            PostgreSQLComStartupPacket comStartupPacket = new PostgreSQLComStartupPacket(payload);
            String databaseName = comStartupPacket.getParametersMap().get(DATABASE_NAME_KEYWORD);
            if (!Strings.isNullOrEmpty(databaseName) && !LogicSchemas.getInstance().schemaExists(databaseName)) {
                // TODO send an error message
                return true;
            }
            backendConnection.setCurrentSchema(databaseName);
            // TODO send a md5 authentication request message
            context.write(new PostgreSQLAuthenticationOKPacket(true));
            context.write(new PostgreSQLParameterStatusPacket("server_version", "8.4"));
            context.write(new PostgreSQLParameterStatusPacket("client_encoding", "UTF8"));
            context.write(new PostgreSQLParameterStatusPacket("server_encoding", "UTF8"));
            context.writeAndFlush(new PostgreSQLReadyForQueryPacket());
            return true;
        }
    }
    
    @Override
    public void executeCommand(final ChannelHandlerContext context, final ByteBuf message, final BackendConnection backendConnection) {
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(message)) {
            writePackets(context, payload, backendConnection);
        } catch (final SQLException ex) {
            context.writeAndFlush(new PostgreSQLErrorResponsePacket());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Exception occur:", ex);
            context.writeAndFlush(new PostgreSQLErrorResponsePacket());
        }
    }
    
    private void writePackets(final ChannelHandlerContext context, final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        PostgreSQLCommandPacketType commandPacketType = PostgreSQLCommandPacketTypeLoader.getCommandPacketType(payload);
        PostgreSQLCommandPacket commandPacket = PostgreSQLCommandPacketFactory.newInstance(commandPacketType, payload, backendConnection.getConnectionId());
        CommandPacketExecutor<PostgreSQLPacket> commandPacketExecutor = PostgreSQLCommandPacketExecutorFactory.newInstance(commandPacketType);
        Collection<PostgreSQLPacket> responsePackets = commandPacketExecutor.execute(backendConnection, commandPacket);
        if (commandPacket instanceof PostgreSQLComSyncPacket) {
            context.write(new PostgreSQLCommandCompletePacket());
            context.writeAndFlush(new PostgreSQLReadyForQueryPacket());
            return;
        }
        if (responsePackets.isEmpty()) {
            return;
        }
        for (PostgreSQLPacket each : responsePackets) {
            context.write(each);
        }
        if (commandPacketExecutor instanceof QueryCommandPacketExecutor) {
            writeMoreResults(context, backendConnection, (QueryCommandPacketExecutor<PostgreSQLPacket>) commandPacketExecutor);
        }
        if (commandPacket instanceof PostgreSQLComQueryPacket) {
            context.write(new PostgreSQLCommandCompletePacket());
            context.writeAndFlush(new PostgreSQLReadyForQueryPacket());
        }
    }
    
    private void writeMoreResults(
            final ChannelHandlerContext context, final BackendConnection backendConnection, final QueryCommandPacketExecutor<PostgreSQLPacket> queryCommandPacketExecutor) throws SQLException {
        if (queryCommandPacketExecutor.isQuery() && !context.channel().isActive()) {
            return;
        }
        int count = 0;
        int proxyFrontendFlushThreshold = GlobalContext.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandPacketExecutor.next()) {
            count++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                context.flush();
                backendConnection.getResourceSynchronizer().doAwait();
            }
            DatabasePacket resultValue = queryCommandPacketExecutor.getQueryData();
            context.write(resultValue);
            if (proxyFrontendFlushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
    }
    
    @Override
    public void release(final BackendConnection backendConnection) {
        BinaryStatementRegistry.getInstance().unregister(backendConnection.getConnectionId());
    }
}
