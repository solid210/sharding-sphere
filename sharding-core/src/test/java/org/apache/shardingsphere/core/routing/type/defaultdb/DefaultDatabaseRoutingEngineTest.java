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

package org.apache.shardingsphere.core.routing.type.defaultdb;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.routing.type.RoutingResult;
import org.apache.shardingsphere.core.routing.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DefaultDatabaseRoutingEngineTest {
    
    private DefaultDatabaseRoutingEngine defaultDatabaseRoutingEngine;
    
    @Before
    public void setEngineContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("ds_0");
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        Collection<String> logicTables = Arrays.asList("t_order", "t_order_item");
        defaultDatabaseRoutingEngine = new DefaultDatabaseRoutingEngine(shardingRule, logicTables);
    }
    
    @Test
    public void assertRoute() {
        RoutingResult routingResult = defaultDatabaseRoutingEngine.route();
        List<TableUnit> tableUnitList = new ArrayList<>(routingResult.getTableUnits().getTableUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getTableUnits().getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getRoutingTables().size(), is(2));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getActualTableName(), is("t_order"));
        assertThat(tableUnitList.get(0).getRoutingTables().get(0).getLogicTableName(), is("t_order"));
        assertThat(tableUnitList.get(0).getRoutingTables().get(1).getActualTableName(), is("t_order_item"));
        assertThat(tableUnitList.get(0).getRoutingTables().get(1).getLogicTableName(), is("t_order_item"));
    }
}
