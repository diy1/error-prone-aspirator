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

package com.google.errorprone;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.errorprone.BugPattern.Instance;
import com.google.errorprone.BugPattern.Suppressibility;
import com.google.errorprone.BugPattern.MaturityLevel;
import com.google.errorprone.BugPattern.SeverityLevel;

import org.kohsuke.MetaInfServices;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.size;
import static com.google.common.io.Files.readLines;
import static com.google.errorprone.BugPattern.Instance.BY_NAME;
import static com.google.errorprone.BugPattern.Instance.BY_SEVERITY;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 *
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes({"com.google.errorprone.BugPattern"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DocGen extends AbstractProcessor {

  private PrintWriter pw;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    try {
      FileObject manifest = processingEnv.getFiler()
          .createResource(StandardLocation.SOURCE_OUTPUT, "", "bugPatterns.txt");
      pw = new PrintWriter(new OutputStreamWriter(manifest.openOutputStream()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(BugPattern.class)) {
      pw.print(element.toString() + "\t");    //0
      BugPattern annotation = element.getAnnotation(BugPattern.class);
      pw.print(annotation.name() + "\t");     //1
      pw.print(Joiner.on(", ").join(annotation.altNames()) + "\t"); //2
      pw.print(annotation.category() + "\t"); //3
      pw.print(annotation.severity() + "\t"); //4
      pw.print(annotation.maturity() + "\t"); //5
      pw.print(annotation.suppressibility() + "\t"); //6
      // This is ugly, but you always get a MirroredTypeException when you try to access this
      // element. See https://community.oracle.com/thread/1184190.
      try {
        annotation.customSuppressionAnnotation();
        throw new IllegalStateException("Expected a MirroredTypeException");
      } catch (MirroredTypeException e) {
        pw.print(e.getTypeMirror().toString() + "\t"); //7
      }
      pw.print(annotation.summary() + "\t");  //8
      pw.println(annotation.explanation().replace("\n", "\\n")); //9
    }

    if (roundEnv.processingOver()) {
      // this was the last round, do cleanup
      cleanup();
    }
    return true;
  }

  /**
   * Perform cleanup after last round of annotation processing.
   */
  private void cleanup() {
    pw.close();
  }

  /**
   * Construct an appropriate wiki page template for this {@code BugPattern}.  Include altNames if
   * there are any, and explain the correct way to suppress.
   */
  private static MessageFormat constructWikiPageTemplate(Instance pattern) {
    List<String> lines = new ArrayList<String>();
    lines.addAll(Arrays.asList(
        "#summary {8}",
        "#labels BugPattern",
        "=Bug pattern: !{1}="));
    if (pattern.altNames.length() > 0) {
      lines.add("  * Alternate names: {2}");
    }
    lines.addAll(Arrays.asList(
        "  * Category: {3}",
        "  * Severity: {4}",
        "  * Maturity: {5}",
        "==The problem==",
        "{9}",
        "==Suppression=="));

    switch (pattern.suppressibility) {
      case SUPPRESS_WARNINGS:
        lines.add("Suppress false positives by adding an @!SuppressWarnings(\"!{1}\") annotation "
            + "to the enclosing element.");
        break;
      case CUSTOM_ANNOTATION:
        lines.add("Suppress false positives by adding the custom suppression annotation @{7} to "
            + "the enclosing element.");
        break;
      case UNSUPPRESSIBLE:
        lines.add("This check may not be suppressed.");
        break;
    }
    lines.add("");
    return new MessageFormat(Joiner.on("\n").join(lines), Locale.ENGLISH);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.err.println("Usage: java DocGen " +
          "<path to bugPatterns.txt> <path to wiki repository> <path to examples>");
      System.exit(1);
    }
    final File bugPatterns = new File(args[0]);
    if (!bugPatterns.exists()) {
      System.err.println("Cannot find bugPatterns file: " + args[0]);
      System.exit(1);
    }
    final File wikiDir = new File(args[1]);
    wikiDir.mkdir();
    final File exampleDirBase = new File(args[2]);
    if (!exampleDirBase.exists()) {
      System.err.println("Cannot find example directory: " + args[2]);
      System.exit(1);
    }
    String indexPage = readLines(bugPatterns, UTF_8, new LineProcessor<String>() {

      // store a list of bugpatterns to generate BugPatterns wiki page
      private Multimap<MaturityLevel, BugPattern.Instance> index = ArrayListMultimap.create();

      @Override
      public String getResult() {
        StringBuilder result = new StringBuilder("#summary Bugs caught by error-prone\n");
        // enum.values() returns all the values of the enum in declared order
        for (MaturityLevel level : MaturityLevel.values()) {
          Set<BugPattern.Instance> bugPatterns = Sets.newTreeSet(
              Ordering.from(BY_SEVERITY).compound(BY_NAME));
          bugPatterns.addAll(index.get(level));
          if (!bugPatterns.isEmpty()) {
            result.append("==")
                .append(level.description.replace("_", " "))
                .append("==\n");
            for (BugPattern.Instance bugPattern : bugPatterns) {
              result.append(String.format("  * [%s]: {{{%s}}} _(%s)_\n",
                  bugPattern.name.replace(' ', '_'), bugPattern.summary,
                  bugPattern.severity.name().toLowerCase().replace("_", " ")));
            }
          }
        }
        return result.toString();
      }

      @Override
      public boolean processLine(String line) throws IOException {
        String[] parts = line.split("\t");
        BugPattern.Instance pattern = new BugPattern.Instance();
        pattern.name = parts[1];
        pattern.altNames = parts[2];
        pattern.maturity = MaturityLevel.valueOf(parts[5]);
        pattern.summary = parts[8];
        pattern.severity = SeverityLevel.valueOf(parts[4]);
        pattern.suppressibility = Suppressibility.valueOf(parts[6]);
        pattern.customSuppressionAnnotation = parts[7];
        index.put(pattern.maturity, pattern);
        // replace spaces in filename with underscores
        Writer writer = new FileWriter(new File(wikiDir, pattern.name.replace(' ', '_') + ".wiki"));
        // replace "\n" with a carriage return for explanation
        parts[9] = parts[9].replace("\\n", "\n");

        MessageFormat wikiPageTemplate = constructWikiPageTemplate(pattern);
        writer.write(wikiPageTemplate.format(parts));

        Iterable<String> classNameParts = Splitter.on('.').split(parts[0]);
        String path = Joiner.on('/').join(limit(classNameParts, size(classNameParts) - 1));
        File exampleDir = new File(exampleDirBase, path);
        if (!exampleDir.exists()) {
          System.err.println("Warning: cannot find path " + exampleDir);
        } else {
          // Example filename must match example pattern.
          File[] examples = exampleDir.listFiles(new ExampleFilter(
              parts[0].substring(parts[0].lastIndexOf('.') + 1)));
          Arrays.sort(examples);
          if (examples.length > 0) {
            writer.write("==Examples==\n");

            for (File example: examples) {
              writer.write("===!" + example.getName() + "===\n");
              writer.write("{{{\n" + Files.toString(example, Charsets.UTF_8) + "\n}}}\n");
            }
          }
        }
        writer.close();
        return true;
      }
    });

    Writer indexWriter = new FileWriter(new File(wikiDir, "BugPatterns.wiki"));
    indexWriter.write(indexPage);
    indexWriter.close();
  }

  private static class ExampleFilter implements FilenameFilter {
    private Pattern matchPattern;

    public ExampleFilter(String checkerName) {
      this.matchPattern = Pattern.compile(checkerName + "(Positive|Negative)Case.*");
    }

    @Override
    public boolean accept(File dir, String name) {
      return matchPattern.matcher(name).matches();
    }
  }
}
