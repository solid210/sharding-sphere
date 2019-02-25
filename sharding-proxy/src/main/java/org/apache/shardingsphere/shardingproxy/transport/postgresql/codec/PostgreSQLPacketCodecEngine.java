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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.transport.common.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLSSLNegativePacket;

import java.util.List;

/**
 * PostgreSQL packet codec.
 *
 * @author zhangyonglun
 */
public final class PostgreSQLPacketCodecEngine implements DatabasePacketCodecEngine {
    
    @Override
    public String getDatabaseType() {
        return DatabaseType.PostgreSQL.name();
    }
    
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= PostgreSQLPacket.MESSAGE_TYPE_LENGTH + PostgreSQLPacket.PAYLOAD_LENGTH;
    }
    
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out, final int readableBytes) {
        int messageTypeLength = 0;
        if ('\0' == in.markReaderIndex().readByte()) {
            in.resetReaderIndex();
        } else {
            messageTypeLength = PostgreSQLPacket.MESSAGE_TYPE_LENGTH;
        }
        int payloadLength = in.readInt();
        int realPacketLength = payloadLength + messageTypeLength;
        if (readableBytes < realPacketLength) {
            in.resetReaderIndex();
            return;
        }
        in.resetReaderIndex();
        out.add(in.readRetainedSlice(payloadLength + messageTypeLength));
    }
    
    @Override
    public void encode(final ChannelHandlerContext context, final DatabasePacket message, final ByteBuf out) {
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(context.alloc().buffer())) {
            ((PostgreSQLPacket) message).write(payload);
            if (!(message instanceof PostgreSQLSSLNegativePacket)) {
                out.writeByte(((PostgreSQLPacket) message).getMessageType());
                out.writeInt(payload.getByteBuf().readableBytes() + ((PostgreSQLPacket) message).PAYLOAD_LENGTH);
            }
            out.writeBytes(payload.getByteBuf());
        }
    }
}
