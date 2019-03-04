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

package org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr;

import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Function expression segment.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
@Getter
public final class FunctionExpressionSegment implements ExpressionSegment {
    
    private final String functionName;
    
    private final int functionStartIndex;
    
    private final int innerExpressionStartIndex;
    
    private final int innerExpressionStopIndex; 
    
    private final int distinctExpressionStartIndex;
    
    @Override
    public Optional<SQLExpression> convertToSQLExpression(final String sql) {
        return Optional.absent();
    }
}
