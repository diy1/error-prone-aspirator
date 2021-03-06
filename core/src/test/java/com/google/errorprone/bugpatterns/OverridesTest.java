/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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
 * @author cushon@google.com (Liam Miller-Cushon)
 */
@RunWith(JUnit4.class)
public class OverridesTest {
  private CompilationTestHelper compilationHelper;

  @Before
  public void setUp() {
    compilationHelper = new CompilationTestHelper(Overrides.class);
  }

  @Test
  public void testPositiveCase1() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        sources(getClass(), "OverridesPositiveCase1.java"));
  }

  @Test
  public void testPositiveCase2() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        sources(getClass(), "OverridesPositiveCase2.java"));
  }

  @Test
  public void testPositiveCase3() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        sources(getClass(), "OverridesPositiveCase3.java"));
  }
  
  @Test
  public void testPositiveCase4() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        sources(getClass(), "OverridesPositiveCase4.java"));
  }

  @Test
  public void testPositiveCase5() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        sources(getClass(), "OverridesPositiveCase5.java"));
  }

  @Test
  public void testNegativeCase1() throws Exception {
    compilationHelper.assertCompileSucceeds(
        sources(getClass(), "OverridesNegativeCase1.java"));
  }

  @Test
  public void testNegativeCase2() throws Exception {
    compilationHelper.assertCompileSucceeds(
        sources(getClass(), "OverridesNegativeCase2.java"));
  }

  @Test
  public void testNegativeCase3() throws Exception {
    compilationHelper.assertCompileSucceeds(
        sources(getClass(), "OverridesNegativeCase3.java"));
  }
}
