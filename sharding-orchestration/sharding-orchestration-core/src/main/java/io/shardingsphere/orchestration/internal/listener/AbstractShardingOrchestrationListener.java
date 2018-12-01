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

package io.shardingsphere.orchestration.internal.listener;

import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEventListener;
import lombok.RequiredArgsConstructor;

/**
 * Abstract sharding orchestration listener.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractShardingOrchestrationListener implements ShardingOrchestrationListener {
    
    private final RegistryCenter regCenter;
    
    private final String watchKey;
    
    @Override
    public final void watch() {
        regCenter.watch(watchKey, getDataChangedEventListener());
    }
    
    protected abstract DataChangedEventListener getDataChangedEventListener();
}
