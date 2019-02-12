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

package org.apache.shardingsphere.core.routing.strategy.complex;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.api.algorithm.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.algorithm.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.core.routing.strategy.ShardingStrategy;
import org.apache.shardingsphere.core.routing.value.ListRouteValue;
import org.apache.shardingsphere.core.routing.value.RouteValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Complex sharding strategy.
 * 
 * @author zhangliang
 */
public final class ComplexShardingStrategy implements ShardingStrategy {
    
    @Getter
    private final Collection<String> shardingColumns;
    
    private final ComplexKeysShardingAlgorithm shardingAlgorithm;
    
    public ComplexShardingStrategy(final ComplexShardingStrategyConfiguration complexShardingStrategyConfig) {
        Preconditions.checkNotNull(complexShardingStrategyConfig.getShardingColumns(), "Sharding columns cannot be null.");
        Preconditions.checkNotNull(complexShardingStrategyConfig.getShardingAlgorithm(), "Sharding algorithm cannot be null.");
        shardingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        shardingColumns.addAll(Splitter.on(",").trimResults().splitToList(complexShardingStrategyConfig.getShardingColumns()));
        shardingAlgorithm = complexShardingStrategyConfig.getShardingAlgorithm();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<RouteValue> shardingValues) {
        Map<String, Collection<Comparable<?>>> columnShardingValues = new HashMap<>(shardingValues.size(), 1);
        String logicTableName = "";
        for (RouteValue each : shardingValues) {
            columnShardingValues.put(each.getColumn().getName(), ((ListRouteValue) each).getValues());
            logicTableName = each.getColumn().getTableName();
        }
        Collection<String> shardingResult = shardingAlgorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue(logicTableName, columnShardingValues));
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
}
