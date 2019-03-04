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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind;

import com.google.common.base.Optional;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.QueryCommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.constant.PostgreSQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLErrorResponsePacket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * PostgreSQL command bind packet executor.
 *
 * @author zhangyonglun
 * @author zhangliang
 */
public final class PostgreSQLComBindPacketExecutor implements QueryCommandPacketExecutor<PostgreSQLPacket> {
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    private boolean isBinaryRowData;
    
    private boolean isQuery;
    
    @Override
    public Collection<PostgreSQLPacket> execute(final BackendConnection backendConnection, final CommandPacket commandPacket) {
        PostgreSQLComBindPacket comBindPacket = (PostgreSQLComBindPacket) commandPacket;
        if (GlobalContext.getInstance().isCircuitBreak()) {
            return Collections.<PostgreSQLPacket>singletonList(new PostgreSQLErrorResponsePacket());
        }
        List<PostgreSQLPacket> result = new LinkedList<>();
        result.add(new PostgreSQLBindCompletePacket());
        isBinaryRowData = comBindPacket.isBinaryRowData();
        if (null != comBindPacket.getBinaryStatement() && null != comBindPacket.getBinaryStatement().getSql()) {
            databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(
                    backendConnection.getLogicSchema(), comBindPacket.getBinaryStatement().getSql(), comBindPacket.getParameters(), backendConnection);
            BackendResponse backendResponse = databaseCommunicationEngine.execute();
            if (backendResponse instanceof ErrorResponse) {
                result.add(createErrorPacket((ErrorResponse) backendResponse));
            }
            if (backendResponse instanceof UpdateResponse) {
                result.add(createUpdatePacket((UpdateResponse) backendResponse));
            }
            if (backendResponse instanceof QueryResponse) {
                Optional<PostgreSQLRowDescriptionPacket> postgreSQLRowDescriptionPacket = createQueryPacket((QueryResponse) backendResponse);
                if (postgreSQLRowDescriptionPacket.isPresent()) {
                    result.add(postgreSQLRowDescriptionPacket.get());
                }
            }
        }
        return result;
    }
    
    private PostgreSQLErrorResponsePacket createErrorPacket(final ErrorResponse errorResponse) {
        return new PostgreSQLErrorResponsePacket();
    }
    
    private PostgreSQLCommandCompletePacket createUpdatePacket(final UpdateResponse updateResponse) {
        return new PostgreSQLCommandCompletePacket();
    }
    
    private Optional<PostgreSQLRowDescriptionPacket> createQueryPacket(final QueryResponse queryResponse) {
        List<PostgreSQLColumnDescription> columnDescriptions = getPostgreSQLColumnDescriptions(queryResponse);
        isQuery = !columnDescriptions.isEmpty();
        if (columnDescriptions.isEmpty() || isBinaryRowData) {
            return Optional.absent();
        }
        return Optional.of(new PostgreSQLRowDescriptionPacket(columnDescriptions.size(), columnDescriptions));
    }
    
    private List<PostgreSQLColumnDescription> getPostgreSQLColumnDescriptions(final QueryResponse queryResponse) {
        List<PostgreSQLColumnDescription> result = new LinkedList<>();
        int columnIndex = 0;
        for (QueryHeader each : queryResponse.getQueryHeaders()) {
            result.add(new PostgreSQLColumnDescription(each, ++columnIndex));
        }
        return result;
    }
    
    @Override
    public boolean isQuery() {
        return isQuery;
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next();
    }
    
    @Override
    public PostgreSQLPacket getQueryData() throws SQLException {
        QueryData queryData = databaseCommunicationEngine.getQueryData();
        return isBinaryRowData ? new PostgreSQLBinaryResultSetRowPacket(queryData.getData(), getPostgreSQLColumnTypes(queryData)) : new PostgreSQLDataRowPacket(queryData.getData());
    }
    
    private List<PostgreSQLColumnType> getPostgreSQLColumnTypes(final QueryData queryData) {
        List<PostgreSQLColumnType> result = new ArrayList<>(queryData.getColumnTypes().size());
        for (int i = 0; i < queryData.getColumnTypes().size(); i++) {
            result.add(PostgreSQLColumnType.valueOfJDBCType(queryData.getColumnTypes().get(i)));
        }
        return result;
    }
}
