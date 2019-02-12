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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic;

import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLErrPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNewErrPacketWithServerErrorCode() {
        MySQLErrPacket actual = new MySQLErrPacket(1, MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR, "root", "localhost", "root");
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode()));
        assertThat(actual.getSqlState(), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState()));
        assertThat(actual.getErrorMessage(), is(String.format(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root")));
    }
    
    @Test
    public void assertNewErrPacketWithException() {
        MySQLErrPacket actual = new MySQLErrPacket(1, new SQLException("no reason", "X999", -1));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(-1));
        assertThat(actual.getSqlState(), is("X999"));
        assertThat(actual.getErrorMessage(), is("no reason"));
    }
    
    @Test
    public void assertWrite() {
        new MySQLErrPacket(1, new SQLException("no reason", "X999", -1)).write(payload);
        verify(payload).writeInt1(MySQLErrPacket.HEADER);
        verify(payload).writeInt2(-1);
        verify(payload).writeStringFix("#");
        verify(payload).writeStringFix("X999");
        verify(payload).writeStringEOF("no reason");
    }
}
