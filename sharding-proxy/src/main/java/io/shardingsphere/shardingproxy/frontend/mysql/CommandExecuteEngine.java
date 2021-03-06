/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.frontend.mysql;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.util.ListeningExecutorServiceUtil;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;

/**
 * Command execute engine.
 *
 * @author wuxu
 */
public final class CommandExecuteEngine implements AutoCloseable {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private final ListeningExecutorService executorService = ListeningExecutorServiceUtil.createAndGet(GLOBAL_REGISTRY.getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.ACCEPTOR_SIZE));
    
    /**
     * Execute.
     *
     * @param command a task to be run.
     */
    public void execute(final Runnable command) {
        executorService.submit(command);
    }
    
    @Override
    public void close() {
        ListeningExecutorServiceUtil.close(executorService);
    }
}
