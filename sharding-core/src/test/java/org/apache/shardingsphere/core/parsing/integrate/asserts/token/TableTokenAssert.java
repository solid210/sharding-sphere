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

package org.apache.shardingsphere.core.parsing.integrate.asserts.token;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTableToken;
import org.apache.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Table token assert.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
final class TableTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertTableTokens(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        List<TableToken> tableTokens = getTableTokens(actual);
        assertThat(assertMessage.getFullAssertMessage("Table tokens size error: "), tableTokens.size(), is(expected.getTableTokens().size()));
        int count = 0;
        for (ExpectedTableToken each : expected.getTableTokens()) {
            assertTableToken(tableTokens.get(count), each);
            count++;
        }
    }
    
    private void assertTableToken(final TableToken actual, final ExpectedTableToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Table tokens start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        if (0 != expected.getSkippedSchemaNameLength()) {
            assertThat(assertMessage.getFullAssertMessage("Table tokens skipped schema name length assertion error: "), actual.getSkippedSchemaNameLength(), is(expected.getSkippedSchemaNameLength()));
        }
        assertThat(assertMessage.getFullAssertMessage("Table tokens table name assertion error: "), actual.getTableName(), is(expected.getTableName()));
        assertThat(assertMessage.getFullAssertMessage("Table tokens left delimiter assertion error: "), 
                actual.getLeftDelimiter(), is(null == expected.getLeftDelimiter() ? "" : expected.getLeftDelimiter()));
        assertThat(assertMessage.getFullAssertMessage("Table tokens right delimiter assertion error: "), 
                actual.getRightDelimiter(), is(null == expected.getRightDelimiter() ? "" : expected.getRightDelimiter()));
    }
    
    private List<TableToken> getTableTokens(final Collection<SQLToken> actual) {
        List<TableToken> result = new ArrayList<>(actual.size());
        for (SQLToken each : actual) {
            if (each instanceof TableToken) {
                result.add((TableToken) each);
            }
        }
        return result;
    }
}
