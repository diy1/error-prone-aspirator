/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

/**
 * @author scottjohnson@google.com (Scott Johnson)
 */
public class SelfEqualityPositiveCases {
  private double value;
  
  //BUG: Suggestion includes "!Double.isNaN(value)"
  private boolean othervalue = value == value;

  public boolean testEquality(double x, float y, int z) {
    boolean retVal;

    //BUG: Suggestion includes "!Double.isNaN(x)"
    retVal = x == x;
    
    //BUG: Suggestion includes "Double.isNaN(x)"
    retVal = x != x;
    
    //BUG: Suggestion includes "!Float.isNaN(y)"
    retVal = y == y;
    
    //BUG: Suggestion includes "Float.isNaN(y)"
    retVal = y != y;
    
    //BUG: Suggestion includes "true"
    retVal = z == z;
    
    //BUG: Suggestion includes "false"
    retVal = z != z;

    return retVal;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SelfEqualityPositiveCases)) {
      return false;
    }
    SelfEqualityPositiveCases other = (SelfEqualityPositiveCases) o;

    boolean ret;

    //BUG: Suggestion includes "value == other.value"
    ret = value == value;
    
    //BUG: Suggestion includes "this.value == other.value"
    ret = this.value == value;
    
    //BUG: Suggestion includes "other.value == this.value"
    ret = value == this.value;
    
    //BUG: Suggestion includes "this.value == other.value"
    ret = this.value == this.value;

    return ret;
  }

}
