/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns;

import static com.google.errorprone.CompilationTestHelper.sources;

import com.google.errorprone.CompilationTestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author vidarh@google.com (Will Holen)
 */
@RunWith(JUnit4.class)
public class MisusedFormattingLoggerTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setUp() {
    compilationHelper = new CompilationTestHelper(MisusedFormattingLogger.class);
  }

  @Test
  public void testPositiveCases() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        sources(getClass(), "MisusedFormattingLoggerPositiveCases.java"));
  }

  @Test
  public void testNegativeCases() throws Exception {
    compilationHelper.assertCompileSucceeds(
        sources(getClass(), "MisusedFormattingLoggerNegativeCases.java"));
  }
}
