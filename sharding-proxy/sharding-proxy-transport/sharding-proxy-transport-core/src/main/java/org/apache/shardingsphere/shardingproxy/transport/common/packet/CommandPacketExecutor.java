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

package org.apache.shardingsphere.shardingproxy.transport.common.packet;

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.DatabasePacket;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Command packet executor.
 *
 * @author zhangliang
 * 
 * @param <T> type of database packet
 */
public interface CommandPacketExecutor<T extends DatabasePacket> {
    
    /**
     * Execute command.
     *
     * @param backendConnection backend connection
     * @param commandPacket command packet
     * @return database packets to be sent
     * @throws SQLException SQL exception
     */
    Collection<T> execute(BackendConnection backendConnection, CommandPacket commandPacket) throws SQLException;
}
