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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.LimitSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.LiteralLimitValueSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.PlaceholderLimitValueSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.util.NumberUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Limit extractor.
 *
 * @author duhongjun
 */
public final class LimitExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<LimitSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> limitNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.LIMIT_CLAUSE);
        if (!limitNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> rangeNode = ExtractorUtils.findFirstChildNode(limitNode.get(), RuleName.RANGE_CLAUSE);
        if (!rangeNode.isPresent()) {
            return Optional.absent();
        }
        Map<ParserRuleContext, Integer> placeholderAndNodeIndexMap = getPlaceholderAndNodeIndexMap(ancestorNode);
        LimitValueSegment firstLimitValue = createLimitValueSegment(placeholderAndNodeIndexMap, (ParserRuleContext) rangeNode.get().getChild(0));
        if (rangeNode.get().getChildCount() >= 3) {
            LimitValueSegment rowCountLimitValue = createLimitValueSegment(placeholderAndNodeIndexMap, (ParserRuleContext) rangeNode.get().getChild(2));
            return Optional.of(new LimitSegment(rowCountLimitValue, firstLimitValue));
        }
        return Optional.of(new LimitSegment(firstLimitValue));
    }
    
    private Map<ParserRuleContext, Integer> getPlaceholderAndNodeIndexMap(final ParserRuleContext ancestorNode) {
        Map<ParserRuleContext, Integer> result = new HashMap<>();
        int index = 0;
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.QUESTION)) {
            result.put(each, index++);
        }
        return result;
    }
    
    private LimitValueSegment createLimitValueSegment(final Map<ParserRuleContext, Integer> placeholderAndNodeIndexMap, final ParserRuleContext limitValueNode) {
        return Symbol.QUESTION.getLiterals().equals(limitValueNode.getText()) 
                ? new PlaceholderLimitValueSegment(placeholderAndNodeIndexMap.get(limitValueNode.getChild(0)), ((ParserRuleContext) limitValueNode.getChild(0)).getStart().getStartIndex())
                : new LiteralLimitValueSegment(NumberUtil.getExactlyNumber(limitValueNode.getText(), 10).intValue(), limitValueNode.getStart().getStartIndex());
    }
}
