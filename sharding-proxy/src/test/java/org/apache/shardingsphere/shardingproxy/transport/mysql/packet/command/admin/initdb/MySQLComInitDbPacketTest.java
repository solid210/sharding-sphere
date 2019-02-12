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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb;

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComInitDbPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    @SneakyThrows
    public void setUp() {
        Map<String, LogicSchema> logicSchemas = Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, mock(LogicSchema.class));
        Field field = GlobalRegistry.class.getDeclaredField("logicSchemas");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), logicSchemas);
    }
    
    @Test
    public void assertExecuteWithValidSchemaName() {
        when(payload.readStringEOF()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
        Optional<CommandResponsePackets> actual = new MySQLComInitDbPacket(1, payload, backendConnection).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(2));
        assertThat(((MySQLOKPacket) actual.get().getHeadPacket()).getAffectedRows(), is(0L));
        assertThat(((MySQLOKPacket) actual.get().getHeadPacket()).getLastInsertId(), is(0L));
        assertThat(((MySQLOKPacket) actual.get().getHeadPacket()).getWarnings(), is(0));
        assertThat(((MySQLOKPacket) actual.get().getHeadPacket()).getInfo(), is(""));
    }
    
    @Test
    public void assertExecuteWithInvalidSchemaName() {
        String invalidSchema = "invalid_schema";
        when(payload.readStringEOF()).thenReturn(invalidSchema);
        Optional<CommandResponsePackets> actual = new MySQLComInitDbPacket(1, payload, backendConnection).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(2));
        assertThat(((MySQLErrPacket) actual.get().getHeadPacket()).getErrorCode(), is(MySQLServerErrorCode.ER_BAD_DB_ERROR.getErrorCode()));
        assertThat(((MySQLErrPacket) actual.get().getHeadPacket()).getSqlState(), is(MySQLServerErrorCode.ER_BAD_DB_ERROR.getSqlState()));
        assertThat(((MySQLErrPacket) actual.get().getHeadPacket()).getErrorMessage(), is(String.format(MySQLServerErrorCode.ER_BAD_DB_ERROR.getErrorMessage(), invalidSchema)));
    }
    
    @Test
    public void assertWrite() {
        when(payload.readStringEOF()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
        MySQLComInitDbPacket actual = new MySQLComInitDbPacket(1, payload, backendConnection);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeInt1(MySQLCommandPacketType.COM_INIT_DB.getValue());
        verify(payload).writeStringEOF(ShardingConstant.LOGIC_SCHEMA_NAME);
    }
}
