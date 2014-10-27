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

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.CatchTreeMatcher;

import static com.google.errorprone.fixes.Fix.NO_FIX;

import com.google.errorprone.matchers.Description;
import com.sun.source.tree.LineMap;

import com.sun.source.tree.CatchTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * "TODO" and "FIXME" should not appear in the error handling
 * logic, because it is often the last line of defense.
 * 
 * For more detail, refer to the paper:
 * "Simple Testing Can Prevent Most Critical Failures: 
 *  An Analysis of Production Failures in Distributed Data-intensive Systems"
 *  Yuan et al. Proceedings of the 11th Symposium on Operating Systems Design 
 *  and Implementation (OSDI), 2014
 *
 * @author yuan@eecg.utoronto.ca (Ding Yuan)
 */
@BugPattern(name = "TodoInCatch",
    summary = "TODO or FIXME in the catch block.",
    explanation = "TODO or FIXME should not appear in the error handling blocks as"
                  + " they are often the last line of defense.",
    category = JDK, maturity = MATURE, severity = ERROR)
public class TodoInCatch extends BugChecker implements CatchTreeMatcher {
  @Override
  public Description matchCatch (CatchTree tree, VisitorState state) {
    LineMap lineMap = state.getPath().getCompilationUnit().getLineMap();
    long startLN = lineMap.getLineNumber(TreeInfo.getStartPos((JCTree) tree));
    long endLN = lineMap.getLineNumber(TreeInfo.endPos((JCTree) (tree.getBlock())));
    String filename = state.getPath().getCompilationUnit().getSourceFile().getName();

    /*
    System.out.println("DEBUG: catch block: starting line: "
              + filename + ":" + startLN + ", end line: " + endLN);
    System.out.println(state.getPath().getLeaf());
    System.out.println();
    */
      
    BufferedReader br = null;
    boolean foundWarning = false;
    try {
      br = new BufferedReader(new FileReader(filename));
      String line = null;
      int i = 0;
      boolean searchStart = false;
          
      do {
        line = br.readLine();

        i++;
        if (i == startLN) {
          searchStart = true;
        }

        if (searchStart == true) {
          // System.out.println("DEUBG: catch line: " + line);
          if (line.contains("TODO") || line.contains("FIXME")) {
            foundWarning = true;
            break;
          }
        }

        if (i >= endLN) {
          break; // reached the end of catch block
        }  
      } while (line != null);
    } catch (FileNotFoundException e) {
      // ignore
      // System.out.println("[DEBUG] file not found: " + e);
    } catch (IOException ioe) {
      // System.out.println("[DEBUG] IOException found: " + ioe);
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException ioe) {
       // Ignore this
      }
    }
    
    if (foundWarning) {
      //System.out.println("****** warning starts **************");
      //System.out.println("WARNING: Found TODO or FIXME in catch block: " 
      //   + filename + ":" + startLN);
      // System.out.println(state.getPath().getLeaf());
      //System.out.println("****** warning ends **************");
      //System.out.println();
      return describeMatch(tree, NO_FIX);
    }

    return Description.NO_MATCH;
  }
}
