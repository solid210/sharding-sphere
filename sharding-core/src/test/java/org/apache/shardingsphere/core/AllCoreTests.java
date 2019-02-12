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

package org.apache.shardingsphere.core;

import org.apache.shardingsphere.core.config.AllConfigTests;
import org.apache.shardingsphere.core.constant.AllConstantsTests;
import org.apache.shardingsphere.core.encrypt.AllEncryptorTests;
import org.apache.shardingsphere.core.executor.AllExecutorTests;
import org.apache.shardingsphere.core.hint.AllHintTests;
import org.apache.shardingsphere.core.keygen.AllKeygenTests;
import org.apache.shardingsphere.core.masterslave.AllMasterSlaveTests;
import org.apache.shardingsphere.core.merger.AllMergerTests;
import org.apache.shardingsphere.core.metadata.AllMetaDataTests;
import org.apache.shardingsphere.core.optimizer.AllOptimizerTests;
import org.apache.shardingsphere.core.parsing.AllParsingTests;
import org.apache.shardingsphere.core.rewrite.AllRewriteTests;
import org.apache.shardingsphere.core.routing.AllRoutingTests;
import org.apache.shardingsphere.core.spi.AllSPITests;
import org.apache.shardingsphere.core.util.AllUtilTests;
import org.apache.shardingsphere.core.yaml.AllYamlTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllConstantsTests.class, 
        AllConfigTests.class, 
        AllUtilTests.class, 
        AllMetaDataTests.class, 
        AllParsingTests.class, 
        AllOptimizerTests.class, 
        AllRewriteTests.class, 
        AllRoutingTests.class, 
        AllExecutorTests.class, 
        AllMergerTests.class, 
        AllHintTests.class, 
        AllYamlTests.class,
        AllSPITests.class, 
        AllMasterSlaveTests.class, 
        AllKeygenTests.class, 
        AllEncryptorTests.class
    })
public final class AllCoreTests {
}
