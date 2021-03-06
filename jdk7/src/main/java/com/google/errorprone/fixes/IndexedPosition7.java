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

package com.google.errorprone.fixes;

import com.sun.tools.javac.tree.JCTree;

import java.util.Map;

/**
 * Describes a position that only has a start and end index.
 */
public class IndexedPosition7 extends AbstractIndexedPosition {
  public IndexedPosition7(int startPos, int endPos) {
    super(startPos, endPos);
  }

  @Override
  public int getEndPosition(Map<JCTree, Integer> endPosTable) {
    return endPos;
  }
}
