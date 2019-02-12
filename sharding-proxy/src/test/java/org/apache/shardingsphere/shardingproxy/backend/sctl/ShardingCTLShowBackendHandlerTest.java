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

package org.apache.shardingsphere.shardingproxy.backend.sctl;

import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseFailurePacket;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingCTLShowBackendHandlerTest {
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Test
    public void assertShowTransactionType() throws SQLException {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show transaction_type", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual, instanceOf(QueryResponsePackets.class));
        assertThat(actual.getHeadPacket(), instanceOf(DataHeaderPacket.class));
        assertThat(actual.getPackets().size(), is(1));
        backendHandler.next();
        ResultPacket resultPacket = backendHandler.getResultValue();
        assertThat(resultPacket.getData().iterator().next(), CoreMatchers.<Object>is("LOCAL"));
    }
    
    @Test
    public void assertShowCachedConnections() throws SQLException {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show cached_connections", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual, instanceOf(QueryResponsePackets.class));
        assertThat(actual.getHeadPacket(), instanceOf(DataHeaderPacket.class));
        assertThat(actual.getPackets().size(), is(1));
        backendHandler.next();
        ResultPacket resultPacket = backendHandler.getResultValue();
        assertThat(resultPacket.getData().iterator().next(), CoreMatchers.<Object>is(0));
    }
    
    @Test
    public void assertShowCachedConnectionFailed() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show cached_connectionss", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(DatabaseFailurePacket.class));
        DatabaseFailurePacket databaseFailurePacket = (DatabaseFailurePacket) actual.getHeadPacket();
        assertThat(databaseFailurePacket.getErrorMessage(), containsString(" could not support this sctl grammar "));
    }
    
    @Test
    public void assertShowCTLFormatError() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show=xx", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(DatabaseFailurePacket.class));
        DatabaseFailurePacket databaseFailurePacket = (DatabaseFailurePacket) actual.getHeadPacket();
        assertThat(databaseFailurePacket.getErrorMessage(), containsString(" please review your sctl format"));
    }
}
