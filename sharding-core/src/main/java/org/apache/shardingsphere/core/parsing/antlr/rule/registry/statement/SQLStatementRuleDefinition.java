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

package org.apache.shardingsphere.core.parsing.antlr.rule.registry.statement;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parsing.antlr.extractor.SQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.optimizer.SQLStatementOptimizer;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.entity.statement.SQLStatementRuleDefinitionEntity;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.entity.statement.SQLStatementRuleEntity;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.extractor.ExtractorRuleDefinition;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL statement rule definition.
 *
 * @author zhangliang
 */
@Getter
public final class SQLStatementRuleDefinition {
    
    private final Map<String, SQLStatementRule> rules = new LinkedHashMap<>();
    
    /**
     * Initialize SQL statement rule definition.
     * 
     * @param dialectRuleDefinitionEntity SQL dialect statement rule definition entity
     * @param extractorRuleDefinition extractor rule definition
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public void init(final SQLStatementRuleDefinitionEntity dialectRuleDefinitionEntity, final ExtractorRuleDefinition extractorRuleDefinition) {
        for (SQLStatementRuleEntity each : dialectRuleDefinitionEntity.getRules()) {
            SQLStatementRule sqlStatementRule = new SQLStatementRule(each.getContext(), 
                    (Class<? extends SQLStatement>) Class.forName(each.getSqlStatementClass()),
                    (SQLStatementOptimizer) newClassInstance(each.getOptimizerClass()));
            sqlStatementRule.getExtractors().addAll(createExtractors(each.getExtractorRuleRefs(), extractorRuleDefinition));
            rules.put(getContextClassName(each.getContext()), sqlStatementRule);
        }
    }
    
    private Collection<SQLSegmentExtractor> createExtractors(final String sqlExtractorRuleRefs, final ExtractorRuleDefinition extractorRuleDefinition) {
        Collection<SQLSegmentExtractor> result = new LinkedList<>();
        if (sqlExtractorRuleRefs != null) {
            for (String each : Splitter.on(',').trimResults().splitToList(sqlExtractorRuleRefs)) {
                result.add(extractorRuleDefinition.getRules().get(each));
            }
        }
        return result;
    }
    
    private String getContextClassName(final String context) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, context + "Context");
    }
    
    @SneakyThrows
    private Object newClassInstance(final String className) {
        return Strings.isNullOrEmpty(className) ? null : Class.forName(className).newInstance();
    }
}
