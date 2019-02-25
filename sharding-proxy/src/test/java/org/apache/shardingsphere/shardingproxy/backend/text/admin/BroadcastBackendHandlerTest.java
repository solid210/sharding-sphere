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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingproxy.backend.MockGlobalRegistryUtil;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.FailureResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.SuccessResponse;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BroadcastBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Test
    public void assertExecuteSuccess() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 10);
        mockDatabaseCommunicationEngine(new SuccessResponse(0L, 0L));
        BroadcastBackendHandler broadcastBackendHandler = new BroadcastBackendHandler("SET timeout = 1000", backendConnection);
        setBackendHandlerFactory(broadcastBackendHandler);
        BackendResponse actual = broadcastBackendHandler.execute();
        assertThat(actual, instanceOf(SuccessResponse.class));
        assertThat(((SuccessResponse) actual).getAffectedRows(), is(0L));
        assertThat(((SuccessResponse) actual).getLastInsertId(), is(0L));
        verify(databaseCommunicationEngine, times(10)).execute();
    }
    
    @Test
    public void assertExecuteFailure() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 10);
        FailureResponse failureResponse = new FailureResponse(new SQLException("no reason", "X999", -1));
        mockDatabaseCommunicationEngine(failureResponse);
        BroadcastBackendHandler broadcastBackendHandler = new BroadcastBackendHandler("SET timeout = 1000", backendConnection);
        setBackendHandlerFactory(broadcastBackendHandler);
        assertThat(broadcastBackendHandler.execute(), instanceOf(FailureResponse.class));
        verify(databaseCommunicationEngine, times(10)).execute();
    }
    
    private void mockDatabaseCommunicationEngine(final BackendResponse backendResponse) {
        when(databaseCommunicationEngine.execute()).thenReturn(backendResponse);
        when(databaseCommunicationEngineFactory.newTextProtocolInstance((LogicSchema) any(), anyString(), (BackendConnection) any())).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows
    private void setBackendHandlerFactory(final BroadcastBackendHandler schemaBroadcastBackendHandler) {
        Field field = schemaBroadcastBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(schemaBroadcastBackendHandler, databaseCommunicationEngineFactory);
    }
}
