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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.shardingproxy.backend.BackendDataSource;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Backend data source for JDBC.
 *
 * @author zhaojun
 * @author zhangliang
 * @author panjuan
 * @author maxiaoguang
 */
@NoArgsConstructor
public final class JDBCBackendDataSource implements BackendDataSource, AutoCloseable {
    
    private Map<String, DataSource> dataSources;
    
    private JDBCBackendDataSourceFactory hikariDataSourceFactory = JDBCRawBackendDataSourceFactory.getInstance();
    
    private ShardingTransactionManagerEngine shardingTransactionManagerEngine = GlobalContext.getInstance().getShardingTransactionManagerEngine();
    
    public JDBCBackendDataSource(final Map<String, YamlDataSourceParameter> dataSourceParameters) {
        createDataSourceMap(dataSourceParameters);
    }
    
    private void createDataSourceMap(final Map<String, YamlDataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, YamlDataSourceParameter> entry : dataSourceParameters.entrySet()) {
            try {
                dataSourceMap.put(entry.getKey(), hikariDataSourceFactory.build(entry.getKey(), entry.getValue()));
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new ShardingException(String.format("Can not build data source, name is `%s`.", entry.getKey()), ex);
            }
        }
        this.dataSources = dataSourceMap;
        shardingTransactionManagerEngine.init(LogicSchemas.getInstance().getDatabaseType(), dataSourceMap);
    }
    
    /**
     * Get connection.
     *
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection(final String dataSourceName) throws SQLException {
        return getConnections(ConnectionMode.MEMORY_STRICTLY, dataSourceName, 1).get(0);
    }
    
    /**
     * Get connections.
     *
     * @param connectionMode connection mode
     * @param dataSourceName data source name
     * @param connectionSize size of connections to get
     * @return connections
     * @throws SQLException SQL exception
     */
    public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        return getConnections(connectionMode, dataSourceName, connectionSize, TransactionType.LOCAL);
    }
    
    /**
     * Get connections.
     *
     * @param connectionMode  connection mode
     * @param dataSourceName  data source name
     * @param connectionSize  size of connections to be get
     * @param transactionType transaction type
     * @return connections
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize, final TransactionType transactionType) throws SQLException {
        DataSource dataSource = dataSources.get(dataSourceName);
        if (1 == connectionSize) {
            return Collections.singletonList(createConnection(transactionType, dataSourceName, dataSource));
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(transactionType, dataSourceName, dataSource, connectionSize);
        }
        synchronized (dataSource) {
            return createConnections(transactionType, dataSourceName, dataSource, connectionSize);
        }
    }
    
    private List<Connection> createConnections(final TransactionType transactionType, final String dataSourceName, final DataSource dataSource, final int connectionSize) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                result.add(createConnection(transactionType, dataSourceName, dataSource));
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new SQLException(String.format("Could't get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()), ex);
            }
        }
        return result;
    }
    
    private Connection createConnection(final TransactionType transactionType, final String dataSourceName, final DataSource dataSource) throws SQLException {
        ShardingTransactionManager shardingTransactionManager = shardingTransactionManagerEngine.getTransactionManager(transactionType);
        return isInShardingTransaction(shardingTransactionManager) ? shardingTransactionManager.getConnection(dataSourceName) : dataSource.getConnection();
    }
    
    private boolean isInShardingTransaction(final ShardingTransactionManager shardingTransactionManager) {
        return null != shardingTransactionManager && shardingTransactionManager.isInTransaction();
    }
    
    @Override
    public void close() throws Exception {
        if (null != dataSources) {
            closeDataSource(dataSources);
        }
        shardingTransactionManagerEngine.close();
    }
    
    private void closeDataSource(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            try {
                Method method = each.getClass().getDeclaredMethod("close");
                method.setAccessible(true);
                method.invoke(each);
            } catch (final ReflectiveOperationException ignored) {
            }
        }
    }
}
