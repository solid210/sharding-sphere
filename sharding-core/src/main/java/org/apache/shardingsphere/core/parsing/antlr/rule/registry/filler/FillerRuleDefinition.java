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

package org.apache.shardingsphere.core.parsing.antlr.rule.registry.filler;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.entity.filler.FillerRuleEntity;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filler rule definition.
 *
 * @author zhangliang
 */
@Getter
public final class FillerRuleDefinition {
    
    private final Map<Class<? extends SQLSegment>, SQLStatementFiller> rules = new LinkedHashMap<>();
    
    /**
     * Initialize filler rule definition.
     * 
     * @param fillerRuleDefinitionEntity filler rule definition entity
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public void init(final FillerRuleDefinitionEntity fillerRuleDefinitionEntity) {
        for (FillerRuleEntity each : fillerRuleDefinitionEntity.getRules()) {
            rules.put((Class<? extends SQLSegment>) Class.forName(each.getSqlSegmentClass()), (SQLStatementFiller) Class.forName(each.getFillerClass()).newInstance());
        }
    }
}
