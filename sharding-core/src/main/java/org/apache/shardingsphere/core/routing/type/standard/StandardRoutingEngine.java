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

package org.apache.shardingsphere.core.routing.type.standard;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.hint.HintManagerHolder;
import org.apache.shardingsphere.core.optimizer.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.condition.ShardingConditions;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken.InsertColumnValue;
import org.apache.shardingsphere.core.routing.strategy.ShardingStrategy;
import org.apache.shardingsphere.core.routing.strategy.hint.HintShardingStrategy;
import org.apache.shardingsphere.core.routing.type.RoutingEngine;
import org.apache.shardingsphere.core.routing.type.RoutingResult;
import org.apache.shardingsphere.core.routing.type.RoutingTable;
import org.apache.shardingsphere.core.routing.type.TableUnit;
import org.apache.shardingsphere.core.routing.value.ListRouteValue;
import org.apache.shardingsphere.core.routing.value.RouteValue;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Standard routing engine.
 * 
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class StandardRoutingEngine implements RoutingEngine {
    
    private final SQLStatement sqlStatement;
    
    private final ShardingRule shardingRule;
    
    private final String logicTableName;
    
    private final ShardingConditions shardingConditions;
   
    @Override
    public RoutingResult route() {
        return generateRoutingResult(getDataNodes(shardingRule.getTableRule(logicTableName)));
    }
    
    private RoutingResult generateRoutingResult(final Collection<DataNode> routedDataNodes) {
        RoutingResult result = new RoutingResult();
        for (DataNode each : routedDataNodes) {
            TableUnit tableUnit = new TableUnit(each.getDataSourceName());
            tableUnit.getRoutingTables().add(new RoutingTable(logicTableName, each.getTableName()));
            result.getTableUnits().getTableUnits().add(tableUnit);
        }
        return result;
    }
    
    private Collection<DataNode> getDataNodes(final TableRule tableRule) {
        if (shardingRule.isRoutingByHint(tableRule)) {
            return routeByHint(tableRule);
        }
        if (isRoutingByShardingConditions(tableRule)) {
            return routeByShardingConditions(tableRule);
        }
        return routeByMixedConditions(tableRule);
    }
    
    private Collection<DataNode> routeByHint(final TableRule tableRule) {
        return route(tableRule, getDatabaseShardingValuesFromHint(), getTableShardingValuesFromHint());
    }
    
    private boolean isRoutingByShardingConditions(final TableRule tableRule) {
        return !(shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy || shardingRule.getTableShardingStrategy(tableRule) instanceof HintShardingStrategy);
    }
    
    private Collection<DataNode> routeByShardingConditions(final TableRule tableRule) {
        return shardingConditions.getShardingConditions().isEmpty() ? route(tableRule, Collections.<RouteValue>emptyList(), Collections.<RouteValue>emptyList())
                : routeByShardingConditionsWithCondition(tableRule);
    }
    
    private Collection<DataNode> routeByShardingConditionsWithCondition(final TableRule tableRule) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getShardingConditions()) {
            Collection<DataNode> dataNodes = route(tableRule, getShardingValuesFromShardingConditions(shardingRule.getDatabaseShardingStrategy(tableRule).getShardingColumns(), each),
                    getShardingValuesFromShardingConditions(shardingRule.getTableShardingStrategy(tableRule).getShardingColumns(), each));
            reviseInsertStatement(each, dataNodes);
            result.addAll(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditions(final TableRule tableRule) {
        return shardingConditions.getShardingConditions().isEmpty() ? routeByMixedConditionsWithHint(tableRule) : routeByMixedConditionsWithCondition(tableRule);
    }
    
    private Collection<DataNode> routeByMixedConditionsWithCondition(final TableRule tableRule) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getShardingConditions()) {
            Collection<DataNode> dataNodes = route(tableRule, getDatabaseShardingValues(tableRule, each), getTableShardingValues(tableRule, each));
            reviseInsertStatement(each, dataNodes);
            result.addAll(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditionsWithHint(final TableRule tableRule) {
        if (shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy) {
            return route(tableRule, getDatabaseShardingValuesFromHint(), Collections.<RouteValue>emptyList());
        }
        return route(tableRule, Collections.<RouteValue>emptyList(), getTableShardingValuesFromHint());
    }
    
    private List<RouteValue> getDatabaseShardingValues(final TableRule tableRule, final ShardingCondition shardingCondition) {
        ShardingStrategy dataBaseShardingStrategy = shardingRule.getDatabaseShardingStrategy(tableRule);
        return isGettingShardingValuesFromHint(dataBaseShardingStrategy)
                ? getDatabaseShardingValuesFromHint() : getShardingValuesFromShardingConditions(dataBaseShardingStrategy.getShardingColumns(), shardingCondition);
    }
    
    private List<RouteValue> getTableShardingValues(final TableRule tableRule, final ShardingCondition shardingCondition) {
        ShardingStrategy tableShardingStrategy = shardingRule.getTableShardingStrategy(tableRule);
        return isGettingShardingValuesFromHint(tableShardingStrategy)
                ? getTableShardingValuesFromHint() : getShardingValuesFromShardingConditions(tableShardingStrategy.getShardingColumns(), shardingCondition);
    }
    
    private boolean isGettingShardingValuesFromHint(final ShardingStrategy shardingStrategy) {
        return shardingStrategy instanceof HintShardingStrategy;
    }
    
    private List<RouteValue> getDatabaseShardingValuesFromHint() {
        return getRouteValues(HintManagerHolder.getDatabaseShardingValues(logicTableName));
    }
    
    private List<RouteValue> getTableShardingValuesFromHint() {
        return getRouteValues(HintManagerHolder.getTableShardingValues(logicTableName));
    }
    
    private List<RouteValue> getRouteValues(final Collection<Comparable<?>> shardingValue) {
        return shardingValue.isEmpty() ? Collections.<RouteValue>emptyList() : Collections.<RouteValue>singletonList(new ListRouteValue<>(new Column("", logicTableName), shardingValue));
    }
    
    private List<RouteValue> getShardingValuesFromShardingConditions(final Collection<String> shardingColumns, final ShardingCondition shardingCondition) {
        List<RouteValue> result = new ArrayList<>(shardingColumns.size());
        for (RouteValue each : shardingCondition.getShardingValues()) {
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(logicTableName);
            if ((logicTableName.equals(each.getColumn().getTableName()) || bindingTableRule.isPresent() && bindingTableRule.get().hasLogicTable(logicTableName)) 
                    && shardingColumns.contains(each.getColumn().getName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<DataNode> route(final TableRule tableRule, final List<RouteValue> databaseShardingValues, final List<RouteValue> tableShardingValues) {
        Collection<String> routedDataSources = routeDataSources(tableRule, databaseShardingValues);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTables(tableRule, each, tableShardingValues));
        }
        return result;
    }
    
    private Collection<String> routeDataSources(final TableRule tableRule, final List<RouteValue> databaseShardingValues) {
        Collection<String> availableTargetDatabases = tableRule.getActualDatasourceNames();
        if (databaseShardingValues.isEmpty()) {
            return availableTargetDatabases;
        }
        Collection<String> result = new LinkedHashSet<>(shardingRule.getDatabaseShardingStrategy(tableRule).doSharding(availableTargetDatabases, databaseShardingValues));
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        return result;
    }
    
    private Collection<DataNode> routeTables(final TableRule tableRule, final String routedDataSource, final List<RouteValue> tableShardingValues) {
        Collection<String> availableTargetTables = tableRule.getActualTableNames(routedDataSource);
        Collection<String> routedTables = new LinkedHashSet<>(tableShardingValues.isEmpty() ? availableTargetTables
                : shardingRule.getTableShardingStrategy(tableRule).doSharding(availableTargetTables, tableShardingValues));
        Preconditions.checkState(!routedTables.isEmpty(), "no table route info");
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedTables) {
            result.add(new DataNode(routedDataSource, each));
        }
        return result;
    }
    
    private void reviseInsertStatement(final ShardingCondition shardingCondition, final Collection<DataNode> dataNodes) {
        if (sqlStatement instanceof InsertStatement) {
            for (InsertColumnValue each : ((InsertStatement) sqlStatement).getInsertValuesToken().getColumnValues()) {
                if (isQualifiedInsertColumnValue(each, shardingCondition)) {
                    each.getDataNodes().addAll(dataNodes);
                }
            }
        }
    }
    
    private boolean isQualifiedInsertColumnValue(final InsertColumnValue insertColumnValue, final ShardingCondition shardingCondition) {
        for (RouteValue each : shardingCondition.getShardingValues()) {
            Optional<Object> columnValue = insertColumnValue.getColumnValue(each.getColumn().getName());
            if (!columnValue.isPresent() || !columnValue.get().equals(((ListRouteValue) each).getValues().iterator().next())) {
                return false;
            }
        }
        return true;
    }
}
