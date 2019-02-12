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

package org.apache.shardingsphere.core.parsing.lexer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.parsing.lexer.token.Token;
import org.apache.shardingsphere.core.parsing.lexer.token.TokenType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class LexerAssert {
    
    public static void assertNextToken(final Lexer lexer, final TokenType expectedTokenType, final String expectedLiterals) {
        lexer.nextToken();
        Token actualToken = lexer.getCurrentToken();
        assertThat(actualToken.getType(), is(expectedTokenType));
        assertThat(actualToken.getLiterals(), is(expectedLiterals));
    }
}
