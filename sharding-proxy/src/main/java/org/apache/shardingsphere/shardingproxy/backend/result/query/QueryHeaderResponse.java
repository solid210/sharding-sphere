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

package org.apache.shardingsphere.shardingproxy.backend.result.query;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;

import java.util.List;

/**
 * Query header response.
 *
 * @author zhangliang
 */
@Getter
public final class QueryHeaderResponse implements BackendResponse {
    
    private final List<QueryHeader> queryHeaders;
    
    private int sequenceId;
    
    public QueryHeaderResponse(final List<QueryHeader> queryHeaders, final int sequenceId) {
        this.queryHeaders = queryHeaders;
        this.sequenceId = sequenceId;
    }
}
