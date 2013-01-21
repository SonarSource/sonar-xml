/*
 * Sonar XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.xml;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XmlProjectFileSystem {

  private static final class DefaultInputFile implements InputFile {

    private File basedir;
    private String relativePath;

    DefaultInputFile(File basedir, String relativePath) {
      this.basedir = basedir;
      this.relativePath = relativePath;
    }

    public File getFile() {
      return new File(basedir, relativePath);
    }

    public File getFileBaseDir() {
      return basedir;
    }

    public String getRelativePath() {
      return relativePath;
    }
  }

  private static class ExclusionFilter implements IOFileFilter {

    private File sourceDir;
    private WildcardPattern[] patterns;

    ExclusionFilter(File sourceDir, WildcardPattern[] patterns) {
      this.sourceDir = sourceDir;
      this.patterns = patterns;
    }

    public boolean accept(File file) {
      String relativePath = DefaultProjectFileSystem.getRelativePath(file, sourceDir);
      if (relativePath == null) {
        return false;
      }
      for (WildcardPattern pattern : patterns) {
        if (pattern.match(relativePath)) {
          return false;
        }
      }
      return true;
    }

    public boolean accept(File file, String name) {
      return accept(file);
    }
  }

  private static class InclusionFilter implements IOFileFilter {

    private String inclusionPattern;
    private File sourceDir;

    public InclusionFilter(File sourceDir, String inclusionPattern) {
      this.sourceDir = sourceDir;
      this.inclusionPattern = inclusionPattern;
    }

    public boolean accept(File file) {
      String relativePath = DefaultProjectFileSystem.getRelativePath(file, sourceDir);
      if (relativePath == null) {
        return false;
      }

      // one of the inclusionpatterns must match.
      for (String filter : inclusionPattern.split(",")) {
        WildcardPattern matcher = WildcardPattern.create(filter);
        if (matcher.match(relativePath)) {
          return true;
        }
      }
      return false;
    }

    public boolean accept(File file, String name) {
      return accept(file);
    }
  }

  public static org.sonar.api.resources.File fromIOFile(InputFile inputfile, Project project) {
    return org.sonar.api.resources.File.fromIOFile(inputfile.getFile(), getSourceDirs(project));
  }

  public static List<File> getSourceDirs(Project project) {
    String sourceDir = (String) project.getProperty(XmlPlugin.SOURCE_DIRECTORY);
    if (sourceDir != null) {
      List<File> sourceDirs = new ArrayList<File>();
      sourceDirs.add(project.getFileSystem().resolvePath(sourceDir));
      return sourceDirs;
    } else {
      return project.getFileSystem().getSourceDirs();
    }
  }

  private final Project project;

  private List<IOFileFilter> filters = Lists.newArrayList();

  public XmlProjectFileSystem(Project project) {
    this.project = project;
  }

  private WildcardPattern[] getExclusionPatterns(boolean applyExclusionPatterns) {
    WildcardPattern[] exclusionPatterns;
    if (applyExclusionPatterns) {
      exclusionPatterns = WildcardPattern.create(project.getExclusionPatterns());
    } else {
      exclusionPatterns = new WildcardPattern[0];
    }
    return exclusionPatterns;
  }

  public List<InputFile> getFiles(Settings settings) {
    List<InputFile> result = Lists.newArrayList();
    if (getSourceDirs() == null) {
      return result;
    }

    IOFileFilter suffixFilter = getFileSuffixFilter(settings);
    WildcardPattern[] exclusionPatterns = getExclusionPatterns(true);
    IOFileFilter visibleFileFilter = HiddenFileFilter.VISIBLE;

    for (File dir : getSourceDirs()) {
      if (dir.exists()) {

        // exclusion filter
        IOFileFilter exclusionFilter = new ExclusionFilter(dir, exclusionPatterns);
        // visible filter
        List<IOFileFilter> fileFilters = Lists.newArrayList(visibleFileFilter, suffixFilter, exclusionFilter);
        // inclusion filter
        String inclusionPattern = (String) project.getProperty(XmlPlugin.INCLUDE_FILE_FILTER);
        if (inclusionPattern != null) {
          fileFilters.add(new InclusionFilter(dir, inclusionPattern));
        }
        fileFilters.addAll(this.filters);

        // create DefaultInputFile for each file.
        List<File> files = (List<File>) FileUtils.listFiles(dir, new AndFileFilter(fileFilters), HiddenFileFilter.VISIBLE);
        for (File file : files) {
          String relativePath = DefaultProjectFileSystem.getRelativePath(file, dir);
          result.add(new DefaultInputFile(dir, relativePath));
        }
      }
    }
    return result;
  }

  private String[] getFileSuffixes(Project project, Settings settings) {
    String[] extensions = settings.getStringArray(XmlPlugin.FILE_EXTENSIONS);

    if (extensions.length > 0 && StringUtils.isNotEmpty(extensions[0])) {
      return extensions;
    } else {
      return Xml.INSTANCE.getFileSuffixes();
    }
  }

  private IOFileFilter getFileSuffixFilter(Settings settings) {
    IOFileFilter suffixFilter = FileFilterUtils.trueFileFilter();

    List<String> suffixes = Arrays.asList(getFileSuffixes(project, settings));
    if (!suffixes.isEmpty()) {
      suffixFilter = new SuffixFileFilter(suffixes);
    }

    return suffixFilter;
  }

  private List<File> getSourceDirs() {
    return getSourceDirs(project);
  }
}
