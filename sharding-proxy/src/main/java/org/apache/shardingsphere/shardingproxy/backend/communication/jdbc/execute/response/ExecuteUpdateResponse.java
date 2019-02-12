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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteUpdateResponseUnit;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Execute update response.
 * 
 * @author zhangliang
 */
public final class ExecuteUpdateResponse implements ExecuteResponse {
    
    @Getter
    private final List<DatabaseSuccessPacket> packets = new LinkedList<>();
    
    public ExecuteUpdateResponse(final Collection<ExecuteResponseUnit> responseUnits) {
        for (ExecuteResponseUnit each : responseUnits) {
            packets.add(((ExecuteUpdateResponseUnit) each).getDatabaseSuccessPacket());
        }
    }
    
    /**
     * Merge packets.
     * 
     * @return merged packet.
     */
    public CommandResponsePackets merge() {
        int affectedRows = 0;
        long lastInsertId = 0;
        for (DatabaseSuccessPacket each : packets) {
            affectedRows += each.getAffectedRows();
            if (each.getLastInsertId() > lastInsertId) {
                lastInsertId = each.getLastInsertId();
            }
        }
        return new CommandResponsePackets(new DatabaseSuccessPacket(1, affectedRows, lastInsertId));
    }
}
