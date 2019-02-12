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

package org.apache.shardingsphere.shardingproxy.backend.communication.netty;

import com.google.common.base.Optional;
import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.merger.MergeEngineFactory;
import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.core.parsing.SQLJudgeEngine;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.routing.RouteUnit;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.core.routing.StatementRoutingEngine;
import org.apache.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.netty.client.BackendNettyClientManager;
import org.apache.shardingsphere.shardingproxy.backend.communication.netty.client.response.mysql.MySQLQueryResult;
import org.apache.shardingsphere.shardingproxy.backend.communication.netty.future.FutureRegistry;
import org.apache.shardingsphere.shardingproxy.backend.communication.netty.future.SynchronizedFuture;
import org.apache.shardingsphere.shardingproxy.runtime.ChannelRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.MySQLComPacketQuery;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Database access engine for Netty.
 *
 * @author wangkai
 * @author linjiaqi
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class NettyDatabaseCommunicationEngine implements DatabaseCommunicationEngine {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private static final BackendNettyClientManager CLIENT_MANAGER = BackendNettyClientManager.getInstance();
    
    private final LogicSchema logicSchema;
    
    private final int connectionId;
    
    private final int sequenceId;
    
    private final String sql;
    
    private final DatabaseType databaseType;
    
    private final Map<String, List<Channel>> channelMap = new HashMap<>();
    
    private SynchronizedFuture synchronizedFuture;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private MergedResult mergedResult;
    
    private DataHeaderPacket dataHeaderPacket;
    
    @Override
    public CommandResponsePackets execute() {
        try {
            return logicSchema instanceof MasterSlaveSchema ? executeForMasterSlave() : executeForSharding();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return new CommandResponsePackets(ex);
        }
    }
    
    private CommandResponsePackets executeForMasterSlave() throws InterruptedException, ExecutionException, TimeoutException {
        String dataSourceName = new MasterSlaveRouter(((MasterSlaveSchema) logicSchema).getMasterSlaveRule(),
                GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW)).route(sql).iterator().next();
        synchronizedFuture = new SynchronizedFuture(1);
        FutureRegistry.getInstance().put(connectionId, synchronizedFuture);
        executeSQL(dataSourceName, sql);
        List<QueryResult> queryResults = synchronizedFuture.get(
                GLOBAL_REGISTRY.getShardingProperties().<Long>getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS), TimeUnit.SECONDS);
        FutureRegistry.getInstance().delete(connectionId);
        List<CommandResponsePackets> packets = new LinkedList<>();
        for (QueryResult each : queryResults) {
            packets.add(((MySQLQueryResult) each).getCommandResponsePackets());
        }
        return merge(new SQLJudgeEngine(sql).judge(), packets, queryResults);
    }
    
    private CommandResponsePackets executeForSharding() throws InterruptedException, ExecutionException, TimeoutException {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(((ShardingSchema) logicSchema).getShardingRule(), 
                logicSchema.getMetaData(), databaseType, GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW));
        SQLRouteResult routeResult = routingEngine.route(sql);
        if (routeResult.getRouteUnits().isEmpty()) {
            return new CommandResponsePackets(new MySQLOKPacket(1));
        }
        synchronizedFuture = new SynchronizedFuture(routeResult.getRouteUnits().size());
        FutureRegistry.getInstance().put(connectionId, synchronizedFuture);
        for (RouteUnit each : routeResult.getRouteUnits()) {
            executeSQL(each.getDataSourceName(), each.getSqlUnit().getSql());
        }
        List<QueryResult> queryResults = synchronizedFuture.get(
                GLOBAL_REGISTRY.getShardingProperties().<Long>getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS), TimeUnit.SECONDS);
        FutureRegistry.getInstance().delete(connectionId);
        List<CommandResponsePackets> packets = new ArrayList<>(queryResults.size());
        for (QueryResult each : queryResults) {
            MySQLQueryResult queryResult = (MySQLQueryResult) each;
            if (0 == currentSequenceId) {
                currentSequenceId = queryResult.getCurrentSequenceId();
            }
            if (0 == columnCount) {
                columnCount = queryResult.getColumnCount();
            }
            packets.add(queryResult.getCommandResponsePackets());
        }
        SQLStatement sqlStatement = routeResult.getSqlStatement();
        CommandResponsePackets result = merge(sqlStatement, packets, queryResults);
        logicSchema.refreshTableMetaData(sqlStatement);
        return result;
    }
    
    private void executeSQL(final String dataSourceName, final String sql) throws InterruptedException, ExecutionException, TimeoutException {
        if (!channelMap.containsKey(dataSourceName)) {
            channelMap.put(dataSourceName, new ArrayList<Channel>());
        }
        SimpleChannelPool pool = CLIENT_MANAGER.getBackendNettyClient(logicSchema.getName()).getPoolMap().get(dataSourceName);
        Channel channel = pool.acquire().get(GLOBAL_REGISTRY.getShardingProperties().<Long>getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS), TimeUnit.SECONDS);
        channelMap.get(dataSourceName).add(channel);
        ChannelRegistry.getInstance().putConnectionId(channel.id().asShortText(), connectionId);
        channel.writeAndFlush(new MySQLComPacketQuery(sequenceId, sql));
    }
    
    private CommandResponsePackets merge(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<QueryResult> queryResults) {
        CommandResponsePackets headPackets = new CommandResponsePackets();
        for (CommandResponsePackets each : packets) {
            headPackets.getPackets().add(each.getHeadPacket());
        }
        for (DatabasePacket each : headPackets.getPackets()) {
            if (each instanceof MySQLErrPacket) {
                return new CommandResponsePackets(each);
            }
        }
        if (SQLType.TCL == sqlStatement.getType()) {
            channelRelease();
        }
        if (SQLType.DML == sqlStatement.getType()) {
            return mergeDML(headPackets);
        }
        if (SQLType.DQL == sqlStatement.getType() || SQLType.DAL == sqlStatement.getType()) {
            return mergeDQLorDAL(sqlStatement, packets, queryResults);
        }
        return packets.get(0);
    }
    
    private CommandResponsePackets mergeDML(final CommandResponsePackets firstPackets) {
        int affectedRows = 0;
        long lastInsertId = 0;
        for (DatabasePacket each : firstPackets.getPackets()) {
            if (each instanceof MySQLOKPacket) {
                MySQLOKPacket mySQLOKPacket = (MySQLOKPacket) each;
                affectedRows += mySQLOKPacket.getAffectedRows();
                lastInsertId = mySQLOKPacket.getLastInsertId();
            }
        }
        return new CommandResponsePackets(new MySQLOKPacket(1, affectedRows, lastInsertId));
    }
    
    private CommandResponsePackets mergeDQLorDAL(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<QueryResult> queryResults) {
        try {
            mergedResult = MergeEngineFactory.newInstance(
                GlobalRegistry.getInstance().getDatabaseType(), ((ShardingSchema) logicSchema).getShardingRule(), sqlStatement, logicSchema.getMetaData().getTable(), queryResults).merge();
            dataHeaderPacket = ((QueryResponsePackets) packets.get(0)).getDataHeaderPackets().iterator().next();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new MySQLErrPacket(1, ex.getErrorCode(), ex.getSQLState(), ex.getMessage()));
        }
        return packets.get(0);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (null == mergedResult || !mergedResult.next()) {
            channelRelease();
            return false;
        }
        return true;
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        List<Object> row = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            Object data = logicSchema instanceof MasterSlaveSchema ? mergedResult.getValue(columnIndex, Object.class) : decode(columnIndex, dataHeaderPacket.getTable(), dataHeaderPacket.getOrgName());
            row.add(data);
        }
        return new ResultPacket(++currentSequenceId, row, columnCount, Collections.<Integer>emptyList());
    }
    
    private void channelRelease() {
        for (Entry<String, List<Channel>> entry : channelMap.entrySet()) {
            for (Channel each : entry.getValue()) {
                CLIENT_MANAGER.getBackendNettyClient(logicSchema.getName()).getPoolMap().get(entry.getKey()).release(each);
            }
        }
    }
    
    @SneakyThrows
    private Object decode(final int columnIndex, final String tableName, final String columnName) {
        Object value = mergedResult.getValue(columnIndex, Object.class);
        Optional<ShardingEncryptor> shardingEncryptor = ((ShardingSchema) logicSchema).getShardingRule().getShardingEncryptorEngine().getShardingEncryptor(tableName, columnName);
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decrypt(value) : value;
    }
}
