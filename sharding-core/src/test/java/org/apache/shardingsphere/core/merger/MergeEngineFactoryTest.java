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

package org.apache.shardingsphere.core.merger;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.merger.dal.DALMergeEngine;
import org.apache.shardingsphere.core.merger.dql.DQLMergeEngine;
import org.apache.shardingsphere.core.merger.fixture.TestQueryResult;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MergeEngineFactoryTest {
    
    private List<QueryResult> queryResults;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("label");
        List<ResultSet> resultSets = Lists.newArrayList(resultSet);
        queryResults = new ArrayList<>(resultSets.size());
        queryResults.add(new TestQueryResult(resultSets.get(0)));
    }
    
    @Test
    public void assertNewInstanceWithSelectStatement() throws SQLException {
        SQLStatement selectStatement = new SelectStatement();
        assertThat(MergeEngineFactory.newInstance(DatabaseType.MySQL, null, selectStatement, null, queryResults), instanceOf(DQLMergeEngine.class));
    }

    @Test
    public void assertNewInstanceWithDALStatement() throws SQLException {
        SQLStatement dalStatement = new DALStatement();
        assertThat(MergeEngineFactory.newInstance(DatabaseType.MySQL, null, dalStatement, null, queryResults), instanceOf(DALMergeEngine.class));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertNewInstanceWithOtherStatement() throws SQLException {
        SQLStatement insertStatement = new InsertStatement();
        MergeEngineFactory.newInstance(DatabaseType.MySQL, null, insertStatement, null, queryResults);
    }
}
