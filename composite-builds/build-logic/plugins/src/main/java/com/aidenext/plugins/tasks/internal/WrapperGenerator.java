/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aidenext.plugins.tasks.internal;

import com.google.common.io.ByteStreams;
import org.gradle.api.GradleException;
import org.gradle.api.NonNullApi;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.api.tasks.wrapper.Wrapper.PathBase;
import org.gradle.util.GradleVersion;
import org.gradle.util.internal.DistributionLocator;
import org.gradle.util.internal.GFileUtils;
import org.gradle.wrapper.WrapperExecutor;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

@NonNullApi
public class WrapperGenerator {

  public static File getPropertiesFile(File jarFileDestination) {
    return new File(jarFileDestination.getParentFile(), jarFileDestination.getName().replaceAll("\\.jar$", ".properties"));
  }

  public static File getBatchScript(File scriptFile) {
    return new File(scriptFile.getParentFile(), scriptFile.getName().replaceFirst("(\\.[^\\.]+)?$", ".bat"));
  }

  public static String getDistributionUrl(GradleVersion gradleVersion, Wrapper.DistributionType distributionType) {
    String distType = distributionType.name().toLowerCase(Locale.ENGLISH);
    return new DistributionLocator().getDistributionFor(gradleVersion, distType).toASCIIString();
  }

  public static void generate(
      PathBase archiveBase, String archivePath,
      PathBase distributionBase, String distributionPath,
      @Nullable String distributionSha256Sum,
      File wrapperPropertiesOutputFile,
      File wrapperJarOutputFile, String jarFileRelativePath,
      File unixScript, File batchScript,
      @Nullable String distributionUrl,
      boolean validateDistributionUrl,
      @Nullable Integer networkTimeout
  ) {
    writeProperties(wrapperPropertiesOutputFile, distributionUrl, distributionSha256Sum, distributionBase, distributionPath, archiveBase, archivePath, networkTimeout, validateDistributionUrl);
    writeWrapperJar(wrapperJarOutputFile);
    writeScripts(jarFileRelativePath, unixScript, batchScript);
  }

  private static void writeProperties(
      File propertiesFileDestination,
      @Nullable String distributionUrl,
      @Nullable String distributionSha256Sum,
      PathBase distributionBase,
      String distributionPath,
      PathBase archiveBase,
      String archivePath,
      @Nullable Integer networkTimeout,
      boolean validateDistributionUrl
  ) {
    Properties wrapperProperties = new Properties();
    if (distributionUrl != null) {
      wrapperProperties.setProperty(WrapperExecutor.DISTRIBUTION_URL_PROPERTY, distributionUrl);
    }
    if (distributionSha256Sum != null) {
      wrapperProperties.setProperty(WrapperExecutor.DISTRIBUTION_SHA_256_SUM, distributionSha256Sum);
    }
    wrapperProperties.setProperty(WrapperExecutor.DISTRIBUTION_BASE_PROPERTY, distributionBase.toString());
    wrapperProperties.setProperty(WrapperExecutor.DISTRIBUTION_PATH_PROPERTY, distributionPath);
    wrapperProperties.setProperty(WrapperExecutor.ZIP_STORE_BASE_PROPERTY, archiveBase.toString());
    wrapperProperties.setProperty(WrapperExecutor.ZIP_STORE_PATH_PROPERTY, archivePath);
    if (networkTimeout != null) {
      wrapperProperties.setProperty(WrapperExecutor.NETWORK_TIMEOUT_PROPERTY, String.valueOf(networkTimeout));
    }
    wrapperProperties.setProperty(WrapperExecutor.VALIDATE_DISTRIBUTION_URL, String.valueOf(validateDistributionUrl));

    GFileUtils.parentMkdirs(propertiesFileDestination);
    try (BufferedWriter writer = Files.newBufferedWriter(propertiesFileDestination.toPath())) {
      wrapperProperties.store(writer, "Gradle wrapper properties");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void writeWrapperJar(File destination) {
    URL jarFileSource = Wrapper.class.getResource("/gradle-wrapper.jar");
    if (jarFileSource == null) {
      throw new GradleException("Cannot locate wrapper JAR resource.");
    }
    try (InputStream in = jarFileSource.openStream(); OutputStream out = Files.newOutputStream(destination.toPath())) {
      ByteStreams.copy(in, out);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write wrapper JAR to " + destination, e);
    }
  }

  private static void writeScripts(String jarFileRelativePath, File unixScript, File batchScript) {
    writeUnixScript(unixScript, jarFileRelativePath);
    writeWindowsScript(batchScript, jarFileRelativePath);
  }

  private static void writeUnixScript(File unixScript, String jarRelativePath) {
    GFileUtils.parentMkdirs(unixScript);
    String script = ""
      + "#!/bin/sh\n"
      + "\n"
      + "# Gradle wrapper script\n"
      + "\n"
      + "APP_NAME=\"Gradle\"\n"
      + "APP_BASE_NAME=$(basename \"$0\")\n"
      + "\n"
      + "DEFAULT_JVM_OPTS='\"-Xmx64m\" \"-Xms64m\"'\n"
      + "\n"
      + "CLASSPATH=$APP_HOME/" + jarRelativePath + "\n"
      + "\n"
      + "if [ \"x$JAVA_HOME\" != \"x\" ]; then\n"
      + "  JAVA_HOME=\"$JAVA_HOME\"\n"
      + "fi\n"
      + "\n"
      + "if [ \"x$GRADLE_OPTS\" != \"x\" ]; then\n"
      + "  JVM_OPTS=\"$GRADLE_OPTS\"\n"
      + "fi\n"
      + "\n"
      + "exec \"$JAVA_HOME/bin/java\" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \"-Dorg.gradle.appname=$APP_BASE_NAME\" -classpath \"$CLASSPATH\" org.gradle.wrapper.GradleWrapperMain \"$@\"\n";
    try {
      Files.writeString(unixScript.toPath(), script);
      unixScript.setExecutable(true);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write unix script", e);
    }
  }

  private static void writeWindowsScript(File batchScript, String jarRelativePath) {
    GFileUtils.parentMkdirs(batchScript);
    String script = ""
      + "@rem Gradle wrapper script\n"
      + "@if \"%DEBUG%\"==\"\" @echo off\n"
      + "@rem Set local scope\n"
      + "setlocal\n"
      + "\n"
      + "@rem Set APP_NAME and APP_BASE_NAME\n"
      + "set APP_NAME=Gradle\n"
      + "set APP_BASE_NAME=%~n0\n"
      + "\n"
      + "@rem Set CLASSPATH\n"
      + "set CLASSPATH=%APP_HOME%\\" + jarRelativePath.replace("/", "\\") + "\n"
      + "\n"
      + "@rem Execute Gradle wrapper\n"
      + "\"%JAVA_HOME%/bin/java.exe\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% \"-Dorg.gradle.appname=%APP_BASE_NAME%\" -classpath \"%CLASSPATH%\" org.gradle.wrapper.GradleWrapperMain %*\n"
      + "\n"
      + ":end\n"
      + "@rem End local scope\n"
      + "endlocal\n";
    try {
      Files.writeString(batchScript.toPath(), script);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write windows script", e);
    }
  }

}