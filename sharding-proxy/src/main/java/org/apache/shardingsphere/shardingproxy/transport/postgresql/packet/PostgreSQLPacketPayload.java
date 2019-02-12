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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Payload operation for PostgreSQL packet data types.
 *
 * @see <a href="https://www.postgresql.org/docs/current/protocol-message-types.html">Message Data Types</a>
 * 
 * @author zhangyonglun
 */
@RequiredArgsConstructor
@Getter
public final class PostgreSQLPacketPayload implements AutoCloseable {
    
    private final ByteBuf byteBuf;
    
    /**
     * Read 1 byte fixed length integer from byte buffers.
     *
     * @return 1 byte fixed length integer
     */
    public int readInt1() {
        return byteBuf.readByte() & 0xff;
    }
    
    /**
     * Write 1 byte fixed length integer to byte buffers.
     *
     * @param value 1 byte fixed length integer
     */
    public void writeInt1(final int value) {
        byteBuf.writeByte(value);
    }
    
    /**
     * Read 2 byte fixed length integer from byte buffers.
     *
     * @return 2 byte fixed length integer
     */
    public int readInt2() {
        return byteBuf.readShort() & 0xffff;
    }
    
    /**
     * Write 2 byte fixed length integer to byte buffers.
     *
     * @param value 2 byte fixed length integer
     */
    public void writeInt2(final int value) {
        byteBuf.writeShort(value);
    }
    
    /**
     * Read 3 byte fixed length integer from byte buffers.
     *
     * @return 3 byte fixed length integer
     */
    public int readInt3() {
        return byteBuf.readMedium() & 0xffffff;
    }
    
    /**
     * Write 3 byte fixed length integer to byte buffers.
     *
     * @param value 3 byte fixed length integer
     */
    public void writeInt3(final int value) {
        byteBuf.writeMedium(value);
    }
    
    /**
     * Read 4 byte fixed length integer from byte buffers.
     *
     * @return 4 byte fixed length integer
     */
    public int readInt4() {
        return byteBuf.readInt();
    }
    
    /**
     * Write 4 byte fixed length integer to byte buffers.
     *
     * @param value 4 byte fixed length integer
     */
    public void writeInt4(final int value) {
        byteBuf.writeInt(value);
    }
    
    /**
     * Read 8 byte fixed length integer from byte buffers.
     *
     * @return 8 byte fixed length integer
     */
    public long readInt8() {
        return byteBuf.readLong();
    }
    
    /**
     * Write 8 byte fixed length integer to byte buffers.
     *
     * @param value 8 byte fixed length integer
     */
    public void writeInt8(final long value) {
        byteBuf.writeLong(value);
    }
    
    /**
     * Read lenenc integer from byte buffers.
     *
     * @return lenenc integer
     */
    public long readIntLenenc() {
        int firstByte = readInt1();
        if (firstByte < 0xfb) {
            return firstByte;
        }
        if (0xfb == firstByte) {
            return 0;
        }
        if (0xfc == firstByte) {
            return byteBuf.readShortLE();
        }
        if (0xfd == firstByte) {
            return byteBuf.readMediumLE();
        }
        return byteBuf.readLongLE();
    }
    
    /**
     * Write lenenc integer to byte buffers.
     *
     * @param value lenenc integer
     */
    public void writeIntLenenc(final long value) {
        if (value < 0xfb) {
            byteBuf.writeByte((int) value);
            return;
        }
        if (value < Math.pow(2, 16)) {
            byteBuf.writeByte(0xfc);
            byteBuf.writeShortLE((int) value);
            return;
        }
        if (value < Math.pow(2, 24)) {
            byteBuf.writeByte(0xfd);
            byteBuf.writeMediumLE((int) value);
            return;
        }
        byteBuf.writeByte(0xfe);
        byteBuf.writeLongLE(value);
    }
    
    /**
     * Read lenenc string from byte buffers.
     *
     * @return lenenc string
     */
    public String readStringLenenc() {
        int length = (int) readIntLenenc();
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return new String(result);
    }
    
    /**
     * Read lenenc string from byte buffers for bytes.
     *
     * @return lenenc bytes
     */
    public byte[] readStringLenencByBytes() {
        int length = (int) readIntLenenc();
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }
    
    /**
     * Write lenenc string to byte buffers.
     * 
     * @param value fixed length string
     */
    public void writeStringLenenc(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            byteBuf.writeByte(0);
            return;
        }
        writeIntLenenc(value.getBytes().length);
        byteBuf.writeBytes(value.getBytes());
    }
    
    /**
     * Write lenenc bytes to byte buffers.
     *
     * @param value fixed length bytes
     */
    public void writeBytesLenenc(final byte[] value) {
        if (0 == value.length) {
            byteBuf.writeByte(0);
            return;
        }
        writeIntLenenc(value.length);
        byteBuf.writeBytes(value);
    }
    
    /**
     * Read fixed length string from byte buffers.
     * 
     * @param length length of fixed string
     * 
     * @return fixed length string
     */
    public String readStringFix(final int length) {
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return new String(result);
    }
    
    /**
     * Read fixed length string from byte buffers and return bytes.
     *
     * @param length length of fixed string
     *
     * @return fixed length bytes
     */
    public byte[] readStringFixByBytes(final int length) {
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }
    
    /**
     * Write variable length string to byte buffers.
     * 
     * @param value fixed length string
     */
    public void writeStringFix(final String value) {
        byteBuf.writeBytes(value.getBytes());
    }
    
    /**
     * Write variable length bytes to byte buffers.
     * 
     * @param value fixed length bytes
     */
    public void writeBytes(final byte[] value) {
        byteBuf.writeBytes(value);
    }
    
    /**
     * Bytes before zero.
     *
     * @return the number of bytes before zero
     */
    public int bytesBeforeZero() {
        return byteBuf.bytesBefore((byte) 0);
    }
    
    /**
     * Read null terminated string from byte buffers.
     * 
     * @return null terminated string
     */
    public String readStringNul() {
        byte[] result = new byte[byteBuf.bytesBefore((byte) 0)];
        byteBuf.readBytes(result);
        byteBuf.skipBytes(1);
        return new String(result);
    }
    
    /**
     * Read null terminated string from byte buffers and return bytes.
     *
     * @return null terminated bytes
     */
    public byte[] readStringNulByBytes() {
        byte[] result = new byte[byteBuf.bytesBefore((byte) 0)];
        byteBuf.readBytes(result);
        byteBuf.skipBytes(1);
        return result;
    }
    
    /**
     * Write null terminated string to byte buffers.
     * 
     * @param value null terminated string
     */
    public void writeStringNul(final String value) {
        byteBuf.writeBytes(value.getBytes());
        byteBuf.writeByte(0);
    }
    
    /**
     * Read rest of packet string from byte buffers.
     * 
     * @return rest of packet string
     */
    public String readStringEOF() {
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return new String(result);
    }
    
    /**
     * Write rest of packet string to byte buffers.
     * 
     * @param value rest of packet string
     */
    public void writeStringEOF(final String value) {
        byteBuf.writeBytes(value.getBytes());
    }
    
    /**
     * Skip reserved from byte buffers.
     * 
     * @param length length of reserved
     */
    public void skipReserved(final int length) {
        byteBuf.skipBytes(length);
    }
    
    /**
     * Write null for reserved to byte buffers.
     * 
     * @param length length of reserved
     */
    public void writeReserved(final int length) {
        for (int i = 0; i < length; i++) {
            byteBuf.writeByte(0);
        }
    }
    
    @Override
    public void close() {
        byteBuf.release();
    }
}
