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

package org.apache.shardingsphere.shardingproxy.backend.text.transaction;

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendTransactionManager;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.FailureResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.SuccessResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Do transaction operation.
 *
 * @author zhaojun
 */
public final class TransactionBackendHandler implements TextProtocolBackendHandler {
    
    private final TransactionOperationType operationType;
    
    private final BackendTransactionManager backendTransactionManager;
    
    public TransactionBackendHandler(final TransactionOperationType operationType, final BackendConnection backendConnection) {
        this.operationType = operationType;
        backendTransactionManager = new BackendTransactionManager(backendConnection);
    }
    
    @Override
    public BackendResponse execute() {
        try {
            return doTransaction();
        } catch (final SQLException ex) {
            return new FailureResponse(ex);
        }
    }
    
    private BackendResponse doTransaction() throws SQLException {
        switch (operationType) {
            case BEGIN:
                backendTransactionManager.begin();
                break;
            case COMMIT:
                backendTransactionManager.commit();
                break;
            case ROLLBACK:
                backendTransactionManager.rollback();
                break;
            default:
                throw new SQLFeatureNotSupportedException(operationType.name());
        }
        return new SuccessResponse();
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public QueryData getQueryData() {
        return null;
    }
}
