/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

/**
 * A single replaced section of a source file. When multiple replacements are to be made in a file,
 * these should be applied in reverse order of startPosition.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class Replacement {
  // positions are character offset from beginning of the source file
  public int startPosition;
  public int endPosition;
  public String replaceWith;

  public Replacement(int startPosition, int endPosition, String replaceWith) {
    this.startPosition = startPosition;
    this.endPosition = endPosition;
    this.replaceWith = replaceWith;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Replacement that = (Replacement) o;

    if (endPosition != that.endPosition) {
      return false;
    }
    if (startPosition != that.startPosition) {
      return false;
    }
    if (replaceWith != null ? !replaceWith.equals(that.replaceWith) : that.replaceWith != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = startPosition;
    result = 31 * result + endPosition;
    result = 31 * result + (replaceWith != null ? replaceWith.hashCode() : 0);
    return result;
  }
}
