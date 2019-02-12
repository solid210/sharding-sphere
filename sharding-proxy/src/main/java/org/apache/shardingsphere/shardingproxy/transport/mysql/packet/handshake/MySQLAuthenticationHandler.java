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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;

import java.util.Arrays;

/**
 * MySQL authentication handler.
 *
 * @author panjuan
 */
@Getter
public final class MySQLAuthenticationHandler {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private final MySQLAuthPluginData mySQLAuthPluginData = new MySQLAuthPluginData();
    
    /**
     * Login.
     *
     * @param username connection username
     * @param authResponse connection auth response
     * @return login success or failure
     */
    public boolean login(final String username, final byte[] authResponse) {
        Authentication authentication = GLOBAL_REGISTRY.getAuthentication();
        if (Strings.isNullOrEmpty(authentication.getPassword())) {
            return authentication.getUsername().equals(username);
        }
        return authentication.getUsername().equals(username) && Arrays.equals(getAuthCipherBytes(authentication.getPassword()), authResponse);
    }
    
    private byte[] getAuthCipherBytes(final String password) {
        byte[] sha1Password = DigestUtils.sha1(password);
        byte[] doubleSha1Password = DigestUtils.sha1(sha1Password);
        byte[] concatBytes = new byte[mySQLAuthPluginData.getAuthPluginData().length + doubleSha1Password.length];
        System.arraycopy(mySQLAuthPluginData.getAuthPluginData(), 0, concatBytes, 0, mySQLAuthPluginData.getAuthPluginData().length);
        System.arraycopy(doubleSha1Password, 0, concatBytes, mySQLAuthPluginData.getAuthPluginData().length, doubleSha1Password.length);
        byte[] sha1ConcatBytes = DigestUtils.sha1(concatBytes);
        return xor(sha1Password, sha1ConcatBytes);
    }
    
    private byte[] xor(final byte[] input, final byte[] secret) {
        final byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; ++i) {
            result[i] = (byte) (input[i] ^ secret[i]);
        }
        return result;
    }
}
