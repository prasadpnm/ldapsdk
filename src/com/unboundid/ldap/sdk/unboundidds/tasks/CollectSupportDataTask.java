/*
 * Copyright 2020 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2020 Ping Identity Corporation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds.tasks;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.args.ArgumentException;
import com.unboundid.util.args.DurationArgument;

import static com.unboundid.ldap.sdk.unboundidds.tasks.TaskMessages.*;



/**
 * This class defines a Directory Server task that can be used to invoke the
 * collect-support-data tool to capture a variety of information that may help
 * monitor the state of the server or diagnose potential problems.
 * <BR>
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class, and other classes within the
 *   {@code com.unboundid.ldap.sdk.unboundidds} package structure, are only
 *   supported for use against Ping Identity, UnboundID, and
 *   Nokia/Alcatel-Lucent 8661 server products.  These classes provide support
 *   for proprietary functionality or for external specifications that are not
 *   considered stable or mature enough to be guaranteed to work in an
 *   interoperable way with other types of LDAP servers.
 * </BLOCKQUOTE>
 * <BR>
 * The properties that are available for use with this type of task include:
 * <UL>
 *   <LI>The path (on the server filesystem) to which the support data archive
 *       should be written.  If this is not provided, then the server will
 *       determine an appropriate output file to use.  If this is provided and
 *       refers to a file that exists, that file will be overwritten.  If this
 *       is provided and refers to a directory that exists, then a file will
 *       be created in that directory with a server-generated name.  If this
 *       is provided and refers to a file that does not exist, then its parent
 *       directory must exist, and a new file will be created with the specified
 *       path.</LI>
 *   <LI>The path (on the server filesystem) to a file containing the passphrase
 *       to use to encrypt the contents of the support data archive.  If this is
 *       not provided, then the support data archive will not be encrypted.</LI>
 *   <LI>A flag that indicates whether to include data that may be expensive to
 *       capture in the support data archive.  This information will not be
 *       included by default.</LI>
 *   <LI>A flag that indicates whether to include a replication state dump
 *       (which may be several megabytes in size) in the support data
 *       archive.  This information will not be included by default.</LI>
 *   <LI>A flag that indicates whether to include binary files in the support
 *       data archive.  Binary files will not be included by default.</LI>
 *   <LI>A flag that indicates whether to include source code (if available) to
 *       any third-party extensions installed in the server.  Extension source
 *       code will not be included by default.</LI>
 *   <LI>The data security level to use when redacting data to include in the
 *       support data archive.  If this is not specified, the server will
 *       select an appropriate security level.</LI>
 *   <LI>A flag that indicates whether to capture items in sequential mode
 *       (which will use less memory, but at the expense of taking longer to
 *       complete) rather than in parallel.  Support data will be captured in
 *       parallel by default.</LI>
 *   <LI>The number and duration between intervals for use when collecting
 *       output of tools (like vmstat, iostat, mpstat, etc.) that use
 *       sampling over time.  If this is not provided, the server will use a
 *       default count and interval.</LI>
 *   <LI>The number of times to invoke the jstack utility to obtain a stack
 *       trace of threads running in the JVM.  If this is not provided, the
 *       server will use a default count.</LI>
 *   <LI>The duration (the length of time before the time the task is invoked)
 *       for log messages to be included in the support data archive.  If this
 *       is not provided, the server will automatically select the amount of
 *       log content to include.</LI>
 *   <LI>An optional comment to include in the support data archive.</LI>
 * </UL>
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class CollectSupportDataTask
       extends Task
{
  /**
   * The fully-qualified name of the Java class that is used for the collect
   * support data task.
   */
  static final String COLLECT_SUPPORT_DATA_TASK_CLASS =
       "com.unboundid.directory.server.tasks.CollectSupportDataTask";



  /**
   * The prefix that will appear at the beginning of all attribute names used by
   * the collect support data task.
   */
  private static final String ATTR_PREFIX = "ds-task-collect-support-data-";



  /**
   * The name of the attribute used to specify a comment to include in the
   * support data archive.
   */
  public static final String ATTR_COMMENT = ATTR_PREFIX + "comment";



  /**
   * The name of the attribute used to specify the path to a file containing the
   * passphrase to use to encrypt the contents of the support data archive.
   */
  public static final String ATTR_ENCRYPTION_PASSPHRASE_FILE =
       ATTR_PREFIX + "encryption-passphrase-file";



  /**
   * The name of the attribute used to indicate whether the support data archive
   * may include binary files that may otherwise have been omitted.
   */
  public static final String ATTR_INCLUDE_BINARY_FILES =
       ATTR_PREFIX + "include-binary-files";



  /**
   * The name of the attribute used to indicate whether the support data archive
   * should include information that may be expensive to capture.
   */
  public static final String ATTR_INCLUDE_EXPENSIVE_DATA =
       ATTR_PREFIX + "include-expensive-data";



  /**
   * The name of the attribute used to indicate whether the support data archive
   * may include the source code (if available) for any third-party extensions
   * installed in the server.
   */
  public static final String ATTR_INCLUDE_EXTENSION_SOURCE =
       ATTR_PREFIX + "include-extension-source";



  /**
   * The name of the attribute used to indicate whether the support data archive
   * should include a replication state dump (which may be several megabytes in
   * size).
   */
  public static final String ATTR_INCLUDE_REPLICATION_STATE_DUMP =
       ATTR_PREFIX + "include-replication-state-dump";



  /**
   * The name of the attribute used to specify the number of times to invoke the
   * jstack utility to capture server thread stack traces.
   */
  public static final String ATTR_JSTACK_COUNT = ATTR_PREFIX + "jstack-count";



  /**
   * The name of the attribute used to specify the length of time that should be
   * covered by the log data included in the support data archive.
   */
  public static final String ATTR_LOG_DURATION = ATTR_PREFIX + "log-duration";



  /**
   * The name of the attribute used to specify the path to which the support
   * data archive should be written.
   */
  public static final String ATTR_OUTPUT_PATH = ATTR_PREFIX + "output-path";



  /**
   * The name of the attribute used to specify the number of intervals to
   * capture for tools that capture multiple samples.
   */
  public static final String ATTR_REPORT_COUNT = ATTR_PREFIX + "report-count";



  /**
   * The name of the attribute used to specify the length of time, in seconds,
   * between samples collected from tools that capture multiple samples.
   */
  public static final String ATTR_REPORT_INTERVAL_SECONDS =
       ATTR_PREFIX + "report-interval-seconds";



  /**
   *The name of the attribute used to specify the minimum age of previous
   * support data archives that should be retained.
   */
  public static final String ATTR_RETAIN_PREVIOUS_ARCHIVE_AGE =
       ATTR_PREFIX + "retain-previous-support-data-archive-age";



  /**
   *The name of the attribute used to specify the minimum number of previous
   * support data archives that should be retained.
   */
  public static final String ATTR_RETAIN_PREVIOUS_ARCHIVE_COUNT =
       ATTR_PREFIX + "retain-previous-support-data-archive-count";



  /**
   * The name of the attribute used to specify the security level to use for
   * information added to the support data archive.
   */
  public static final String ATTR_SECURITY_LEVEL =
       ATTR_PREFIX + "security-level";



  /**
   * The name of the attribute used to indicate whether to collect items
   * sequentially rather than in parallel.
   */
  public static final String ATTR_USE_SEQUENTIAL_MODE =
       ATTR_PREFIX + "use-sequential-mode";



  /**
   * The name of the object class used in collect support data task entries.
   */
  public static final String OC_COLLECT_SUPPORT_DATA_TASK =
       "ds-task-collect-support-data";



  /**
   * The task property that will be used for the comment.
   */
  static final TaskProperty PROPERTY_COMMENT =
     new TaskProperty(ATTR_COMMENT, INFO_CSD_DISPLAY_NAME_COMMENT.get(),
          INFO_CSD_DESCRIPTION_COMMENT.get(), String.class, false, false,
          false);



  /**
   * The task property that will be used for the encryption passphrase file.
   */
  static final TaskProperty PROPERTY_ENCRYPTION_PASSPHRASE_FILE =
     new TaskProperty(ATTR_ENCRYPTION_PASSPHRASE_FILE,
          INFO_CSD_DISPLAY_NAME_ENC_PW_FILE.get(),
          INFO_CSD_DESCRIPTION_ENC_PW_FILE.get(), String.class, false, false,
          false);



  /**
   * The task property that will be used for the include binary files flag.
   */
  static final TaskProperty PROPERTY_INCLUDE_BINARY_FILES =
     new TaskProperty(ATTR_INCLUDE_BINARY_FILES,
          INFO_CSD_DISPLAY_NAME_INCLUDE_BINARY_FILES.get(),
          INFO_CSD_DESCRIPTION_INCLUDE_BINARY_FILES.get(), Boolean.class, false,
          false, false);



  /**
   * The task property that will be used for the include expensive data flag.
   */
  static final TaskProperty PROPERTY_INCLUDE_EXPENSIVE_DATA =
     new TaskProperty(ATTR_INCLUDE_EXPENSIVE_DATA,
          INFO_CSD_DISPLAY_NAME_INCLUDE_EXPENSIVE_DATA.get(),
          INFO_CSD_DESCRIPTION_INCLUDE_EXPENSIVE_DATA.get(), Boolean.class,
          false, false, false);



  /**
   * The task property that will be used for the include extension source flag.
   */
  static final TaskProperty PROPERTY_INCLUDE_EXTENSION_SOURCE =
     new TaskProperty(ATTR_INCLUDE_EXTENSION_SOURCE,
          INFO_CSD_DISPLAY_NAME_INCLUDE_EXTENSION_SOURCE.get(),
          INFO_CSD_DESCRIPTION_INCLUDE_EXTENSION_SOURCE.get(), Boolean.class,
          false, false, false);



  /**
   * The task property that will be used for the include replication state dump
   * flag.
   */
  static final TaskProperty PROPERTY_INCLUDE_REPLICATION_STATE_DUMP =
     new TaskProperty(ATTR_INCLUDE_REPLICATION_STATE_DUMP,
          INFO_CSD_DISPLAY_NAME_INCLUDE_REPLICATION_STATE_DUMP.get(),
          INFO_CSD_DESCRIPTION_INCLUDE_REPLICATION_STATE_DUMP.get(),
          Boolean.class, false, false, false);



  /**
   * The task property that will be used for the jstack count.
   */
  static final TaskProperty PROPERTY_JSTACK_COUNT =
     new TaskProperty(ATTR_JSTACK_COUNT,
          INFO_CSD_DISPLAY_NAME_JSTACK_COUNT.get(),
          INFO_CSD_DESCRIPTION_JSTACK_COUNT.get(),
          Long.class, false, false, false);



  /**
   * The task property that will be used for the log duration.
   */
  static final TaskProperty PROPERTY_LOG_DURATION =
     new TaskProperty(ATTR_LOG_DURATION,
          INFO_CSD_DISPLAY_NAME_LOG_DURATION.get(),
          INFO_CSD_DESCRIPTION_LOG_DURATION.get(),
          String.class, false, false, false);



  /**
   * The task property that will be used for the output path.
   */
  static final TaskProperty PROPERTY_OUTPUT_PATH =
     new TaskProperty(ATTR_OUTPUT_PATH,
          INFO_CSD_DISPLAY_NAME_OUTPUT_PATH.get(),
          INFO_CSD_DESCRIPTION_OUTPUT_PATH.get(),
          String.class, false, false, false);



  /**
   * The task property that will be used for the report count.
   */
  static final TaskProperty PROPERTY_REPORT_COUNT =
     new TaskProperty(ATTR_REPORT_COUNT,
          INFO_CSD_DISPLAY_NAME_REPORT_COUNT.get(),
          INFO_CSD_DESCRIPTION_REPORT_COUNT.get(),
          Long.class, false, false, false);



  /**
   * The task property that will be used for the report interval.
   */
  static final TaskProperty PROPERTY_REPORT_INTERVAL_SECONDS =
     new TaskProperty(ATTR_REPORT_INTERVAL_SECONDS,
          INFO_CSD_DISPLAY_NAME_REPORT_INTERVAL.get(),
          INFO_CSD_DESCRIPTION_REPORT_INTERVAL.get(),
          Long.class, false, false, false);



  /**
   * The task property that will be used for the retain previous support data
   * archive age.
   */
  static final TaskProperty PROPERTY_RETAIN_PREVIOUS_ARCHIVE_AGE =
     new TaskProperty(ATTR_RETAIN_PREVIOUS_ARCHIVE_AGE,
          INFO_CSD_DISPLAY_NAME_RETAIN_PREVIOUS_ARCHIVE_AGE.get(),
          INFO_CSD_DESCRIPTION_RETAIN_PREVIOUS_ARCHIVE_AGE.get(),
          String.class, false, false, false);



  /**
   * The task property that will be used for the retain previous support data
   * archive count.
   */
  static final TaskProperty PROPERTY_RETAIN_PREVIOUS_ARCHIVE_COUNT =
     new TaskProperty(ATTR_RETAIN_PREVIOUS_ARCHIVE_COUNT,
          INFO_CSD_DISPLAY_NAME_RETAIN_PREVIOUS_ARCHIVE_COUNT.get(),
          INFO_CSD_DESCRIPTION_RETAIN_PREVIOUS_ARCHIVE_COUNT.get(),
          Long.class, false, false, false);



  /**
   * The task property that will be used for the security level.
   */
  static final TaskProperty PROPERTY_SECURITY_LEVEL =
     new TaskProperty(ATTR_SECURITY_LEVEL,
          INFO_CSD_DISPLAY_NAME_SECURITY_LEVEL.get(),
          INFO_CSD_DESCRIPTION_SECURITY_LEVEL.get(
               CollectSupportDataSecurityLevel.NONE.getName(),
               CollectSupportDataSecurityLevel.OBSCURE_SECRETS.getName(),
               CollectSupportDataSecurityLevel.MAXIMUM.getName()),
          String.class, false, false, false,
          new Object[]
          {
            CollectSupportDataSecurityLevel.NONE.getName(),
            CollectSupportDataSecurityLevel.OBSCURE_SECRETS.getName(),
            CollectSupportDataSecurityLevel.MAXIMUM.getName()
          });



  /**
   * The task property that will be used for the use sequential mode flag.
   */
  static final TaskProperty PROPERTY_USE_SEQUENTIAL_MODE =
     new TaskProperty(ATTR_USE_SEQUENTIAL_MODE,
          INFO_CSD_DISPLAY_NAME_USE_SEQUENTIAL_MODE.get(),
          INFO_CSD_DESCRIPTION_USE_SEQUENTIAL_MODE.get(),
          Boolean.class, false, false, false);



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -2568906018686907596L;



  // Indicates whether to include binary files in the support data archive.
  private final Boolean includeBinaryFiles;

  // Indicates whether to include expensive data in the support data archive.
  private final Boolean includeExpensiveData;

  // Indicates whether to include third-party extension source code in the
  // support data archive.
  private final Boolean includeExtensionSource;

  // Indicates whether to include a replication state dump in the support data
  // archive.
  private final Boolean includeReplicationStateDump;

  // Indicates whether to capture information sequentially rather than in
  // parallel.
  private final Boolean useSequentialMode;

  // The security level to use for data included in the support data archive.
  private final CollectSupportDataSecurityLevel securityLevel;

  // The number of jstacks to include in the support data archive.
  private final Integer jstackCount;

  // The report count to use for sampled metrics.
  private final Integer reportCount;

  // The report interval, in seconds, to use for sampled metrics.
  private final Integer reportIntervalSeconds;

  // The minimum number of existing support data archives that should be
  // retained.
  private final Integer retainPreviousSupportDataArchiveCount;

  // A comment to include in the support data archive.
  private final String comment;

  // The path to the encryption passphrase file.
  private final String encryptionPassphraseFile;

  // A string representation of the log duration to capture.
  private final String logDuration;

  // The path to which the support data archive should be written.
  private final String outputPath;

  // The minimum age for existing support data archives that should be retained.
  private final String retainPreviousSupportDataArchiveAge;



  /**
   * Creates a new collect support data task instance that will use default
   * settings for all properties.  This instance may be used to invoke the
   * task, but it can also be used for obtaining general information about this
   * task, including the task name, description, and supported properties.
   */
  public CollectSupportDataTask()
  {
    this(new CollectSupportDataTaskProperties());
  }



  /**
   * Creates a new collect support data task instance using the provided
   * properties.
   *
   * @param  properties  The properties to use to create the collect support
   *                     data task.  It must not be {@code null}.
   */
  public CollectSupportDataTask(
              final CollectSupportDataTaskProperties properties)
  {
    super(properties.getTaskID(), COLLECT_SUPPORT_DATA_TASK_CLASS,
         properties.getScheduledStartTime(), properties.getDependencyIDs(),
         properties.getFailedDependencyAction(), properties.getNotifyOnStart(),
         properties.getNotifyOnCompletion(), properties.getNotifyOnSuccess(),
         properties.getNotifyOnError(), properties.getAlertOnStart(),
         properties.getAlertOnSuccess(), properties.getAlertOnError());

    includeBinaryFiles = properties.getIncludeBinaryFiles();
    includeExpensiveData = properties.getIncludeExpensiveData();
    includeExtensionSource = properties.getIncludeExtensionSource();
    includeReplicationStateDump = properties.getIncludeReplicationStateDump();
    useSequentialMode = properties.getUseSequentialMode();
    securityLevel = properties.getSecurityLevel();
    jstackCount = properties.getJStackCount();
    reportCount = properties.getReportCount();
    reportIntervalSeconds = properties.getReportIntervalSeconds();
    retainPreviousSupportDataArchiveCount =
         properties.getRetainPreviousSupportDataArchiveCount();
    comment = properties.getComment();
    encryptionPassphraseFile = properties.getEncryptionPassphraseFile();
    logDuration = properties.getLogDuration();
    outputPath = properties.getOutputPath();
    retainPreviousSupportDataArchiveAge =
         properties.getRetainPreviousSupportDataArchiveAge();
  }



  /**
   * Creates a new collect support data task from the provided entry.
   *
   * @param  entry  The entry to use to create this collect support data task.
   *
   * @throws  TaskException  If the provided entry cannot be parsed as a collect
   *                         support data task entry.
   */
  public CollectSupportDataTask(final Entry entry)
         throws TaskException
  {
    super(entry);

    includeBinaryFiles =
         entry.getAttributeValueAsBoolean(ATTR_INCLUDE_BINARY_FILES);
    includeExpensiveData =
         entry.getAttributeValueAsBoolean(ATTR_INCLUDE_EXPENSIVE_DATA);
    includeExtensionSource =
         entry.getAttributeValueAsBoolean(ATTR_INCLUDE_EXTENSION_SOURCE);
    includeReplicationStateDump =
         entry.getAttributeValueAsBoolean(ATTR_INCLUDE_REPLICATION_STATE_DUMP);
    useSequentialMode =
         entry.getAttributeValueAsBoolean(ATTR_USE_SEQUENTIAL_MODE);

    jstackCount = entry.getAttributeValueAsInteger(ATTR_JSTACK_COUNT);
    reportCount = entry.getAttributeValueAsInteger(ATTR_REPORT_COUNT);
    reportIntervalSeconds =
         entry.getAttributeValueAsInteger(ATTR_REPORT_INTERVAL_SECONDS);
    retainPreviousSupportDataArchiveCount =
         entry.getAttributeValueAsInteger(ATTR_RETAIN_PREVIOUS_ARCHIVE_COUNT);

    comment = entry.getAttributeValue(ATTR_COMMENT);
    encryptionPassphraseFile =
         entry.getAttributeValue(ATTR_ENCRYPTION_PASSPHRASE_FILE);
    outputPath = entry.getAttributeValue(ATTR_OUTPUT_PATH);

    final String securityLevelStr =
         entry.getAttributeValue(ATTR_SECURITY_LEVEL);
    if (securityLevelStr == null)
    {
      securityLevel = null;
    }
    else
    {
      securityLevel = CollectSupportDataSecurityLevel.forName(securityLevelStr);
      if (securityLevel == null)
      {
        throw new TaskException(ERR_CSD_ENTRY_INVALID_SECURITY_LEVEL.get(
             entry.getDN(), securityLevelStr, ATTR_SECURITY_LEVEL,
             CollectSupportDataSecurityLevel.NONE.getName(),
             CollectSupportDataSecurityLevel.OBSCURE_SECRETS.getName(),
             CollectSupportDataSecurityLevel.MAXIMUM.getName()));
      }
    }

    logDuration = entry.getAttributeValue(ATTR_LOG_DURATION);
    if (logDuration != null)
    {
      try
      {
        DurationArgument.parseDuration(logDuration, TimeUnit.MILLISECONDS);
      }
      catch (final Exception e)
      {
        Debug.debugException(e);
        throw new TaskException(
             ERR_CSD_ENTRY_INVALID_DURATION.get(entry.getDN(), logDuration,
                  ATTR_LOG_DURATION),
             e);
      }
    }

    retainPreviousSupportDataArchiveAge =
         entry.getAttributeValue(ATTR_RETAIN_PREVIOUS_ARCHIVE_AGE);
    if (retainPreviousSupportDataArchiveAge != null)
    {
      try
      {
        DurationArgument.parseDuration(retainPreviousSupportDataArchiveAge,
             TimeUnit.MILLISECONDS);
      }
      catch (final Exception e)
      {
        Debug.debugException(e);
        throw new TaskException(
             ERR_CSD_ENTRY_INVALID_DURATION.get(entry.getDN(),
                  retainPreviousSupportDataArchiveAge,
                  ATTR_RETAIN_PREVIOUS_ARCHIVE_AGE),
             e);
      }
    }
  }



  /**
   * Creates a new collect support data task from the provided set of task
   * properties.
   *
   * @param  properties  The set of task properties and their corresponding
   *                     values to use for the task.  It must not be
   *                     {@code null}.
   *
   * @throws  TaskException  If the provided set of properties cannot be used to
   *                         create a valid collect support data task.
   */
  public CollectSupportDataTask(final Map<TaskProperty,List<Object>> properties)
         throws TaskException
  {
    super(COLLECT_SUPPORT_DATA_TASK_CLASS, properties);

    Boolean includeBinary = null;
    Boolean includeExpensive = null;
    Boolean includeReplicationState = null;
    Boolean includeSource = null;
    Boolean sequentialMode = null;
    CollectSupportDataSecurityLevel secLevel = null;
    Integer numJStacks = null;
    Integer numReports = null;
    Integer reportIntervalSecs = null;
    Integer retainCount = null;
    String commentStr = null;
    String encPWFile = null;
    String logDurationStr = null;
    String outputPathStr = null;
    String retainAge = null;

    for (final Map.Entry<TaskProperty,List<Object>> entry :
         properties.entrySet())
    {
      final TaskProperty p = entry.getKey();
      final String attrName = StaticUtils.toLowerCase(p.getAttributeName());
      final List<Object> values = entry.getValue();

      if (attrName.equals(ATTR_INCLUDE_BINARY_FILES))
      {
        includeBinary = parseBoolean(p, values, includeBinary);
      }
      else if (attrName.equals(ATTR_INCLUDE_EXPENSIVE_DATA))
      {
        includeExpensive = parseBoolean(p, values, includeExpensive);
      }
      else if (attrName.equals(ATTR_INCLUDE_REPLICATION_STATE_DUMP))
      {
        includeReplicationState =
             parseBoolean(p, values, includeReplicationState);
      }
      else if (attrName.equals(ATTR_INCLUDE_EXTENSION_SOURCE))
      {
        includeSource = parseBoolean(p, values, includeSource);
      }
      else if (attrName.equals(ATTR_USE_SEQUENTIAL_MODE))
      {
        sequentialMode = parseBoolean(p, values, sequentialMode);
      }
      else if (attrName.equals(ATTR_SECURITY_LEVEL))
      {
        final String secLevelStr = parseString(p, values,
             getSecurityLevelName(secLevel));
        secLevel = CollectSupportDataSecurityLevel.forName(secLevelStr);
      }
      else if (attrName.equals(ATTR_JSTACK_COUNT))
      {
        numJStacks = parseLong(p, values,
             getIntegerAsLong(numJStacks)).intValue();
      }
      else if (attrName.equals(ATTR_REPORT_COUNT))
      {
        numReports = parseLong(p, values,
             getIntegerAsLong(numReports)).intValue();
      }
      else if (attrName.equals(ATTR_REPORT_INTERVAL_SECONDS))
      {
        reportIntervalSecs = parseLong(p, values,
             getIntegerAsLong(reportIntervalSecs)).intValue();
      }
      else if (attrName.equals(ATTR_COMMENT))
      {
        commentStr = parseString(p, values, commentStr);
      }
      else if (attrName.equals(ATTR_ENCRYPTION_PASSPHRASE_FILE))
      {
        encPWFile = parseString(p, values, encPWFile);
      }
      else if (attrName.equals(ATTR_LOG_DURATION))
      {
        logDurationStr = parseString(p, values, logDurationStr);
        try
        {
          DurationArgument.parseDuration(logDurationStr, TimeUnit.MILLISECONDS);
        }
        catch (final Exception e)
        {
          Debug.debugException(e);
          throw new TaskException(
               ERR_CSD_PROPERTY_INVALID_DURATION.get(logDurationStr,
                    ATTR_LOG_DURATION),
               e);
        }
      }
      else if (attrName.equals(ATTR_OUTPUT_PATH))
      {
        outputPathStr = parseString(p, values, outputPathStr);
      }
      else if (attrName.equals(ATTR_RETAIN_PREVIOUS_ARCHIVE_COUNT))
      {
        retainCount = parseLong(p, values,
             getIntegerAsLong(retainCount)).intValue();
      }
      else if (attrName.equals(ATTR_RETAIN_PREVIOUS_ARCHIVE_AGE))
      {
        retainAge = parseString(p, values, retainAge);
        try
        {
          DurationArgument.parseDuration(retainAge, TimeUnit.MILLISECONDS);
        }
        catch (final Exception e)
        {
          Debug.debugException(e);
          throw new TaskException(
               ERR_CSD_PROPERTY_INVALID_DURATION.get(retainAge,
                    ATTR_RETAIN_PREVIOUS_ARCHIVE_AGE),
               e);
        }
      }
    }

    includeBinaryFiles = includeBinary;
    includeExpensiveData = includeExpensive;
    includeReplicationStateDump = includeReplicationState;
    includeExtensionSource = includeSource;
    useSequentialMode = sequentialMode;
    securityLevel = secLevel;
    jstackCount = numJStacks;
    reportCount = numReports;
    reportIntervalSeconds = reportIntervalSecs;
    retainPreviousSupportDataArchiveCount = retainCount;
    comment = commentStr;
    encryptionPassphraseFile = encPWFile;
    logDuration = logDurationStr;
    outputPath = outputPathStr;
    retainPreviousSupportDataArchiveAge = retainAge;
  }



  /**
   * Retrieves the name of the provided security level.
   *
   * @param  securityLevel  The security level for which to retrieve the name.
   *                        It may be {@code null}.
   *
   * @return  The name of the provided security level, or {@code null} if the
   *          provided security level is {@code null}.
   */
  static String getSecurityLevelName(
                     final CollectSupportDataSecurityLevel securityLevel)
  {
    if (securityLevel == null)
    {
      return null;
    }
    else
    {
      return securityLevel.getName();
    }
  }



  /**
   * Retrieves the value of the provided {@code Integer} object as a
   * {@code Long}.
   *
   * @param  i  The {@code Integer} value to convert to a {@code Long}.  It may
   *            be {@code null}.
   *
   * @return  The value of the provided {@code Integer} object as a
   *          {@code Long}, or {@code null} if the provided value was
   *          {@code null}.
   */
  static Long getIntegerAsLong(final Integer i)
  {
    if (i == null)
    {
      return null;
    }
    else
    {
      return i.longValue();
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getTaskName()
  {
    return INFO_CSD_TASK_NAME.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getTaskDescription()
  {
    return INFO_CSD_TASK_DESCRIPTION.get();
  }



  /**
   * Retrieves the path on the server filesystem to which the support data
   * archive should be written.
   *
   * @return  The path on the server filesystem to which the support data
   *          archive should be written, or {@code null} if no value has been
   *          specified for the property.
   */
  public String getOutputPath()
  {
    return outputPath;
  }



  /**
   * Retrieves the path on the server filesystem to a file that contains the
   * passphrase to use to encrypt the support data archive.
   *
   * @return  The path on the server filesystem to a file that contains the
   *          passphrase to use to encrypt the support data archive, or
   *          {@code null} if no value has been specified for the property, and
   *          the support data archive should not be encrypted.
   */
  public String getEncryptionPassphraseFile()
  {
    return encryptionPassphraseFile;
  }



  /**
   * Retrieves the value of a flag that indicates whether the support data
   * archive may include data that is potentially expensive to collect and
   * could affect the performance or responsiveness of the server.
   *
   * @return  The value of a flag that indicates whether the support data
   *          archive may include data that is potentially expensive to collect,
   *          or {@code null} if the property should not be specified when the
   *          task is created (in which case the server will use a default
   *          behavior of excluding expensive data).
   */
  public Boolean getIncludeExpensiveData()
  {
    return includeExpensiveData;
  }



  /**
   * Retrieves the value of a flag that indicates whether the support data
   * archive may include a replication state dump, which may be several
   * megabytes in size.
   *
   * @return  The value of a flag that indicates whether the support data
   *          archive may include a replication state dump, or {@code null} if
   *          the property should not be specified when the task is created (in
   *          which case the server will use a default behavior of excluding the
   *          state dump).
   */
  public Boolean getIncludeReplicationStateDump()
  {
    return includeReplicationStateDump;
  }



  /**
   * Retrieves the value of a flag that indicates whether the support data
   * archive may include binary files.
   *
   * @return  The value of a flag that indicates whether the support data
   *          archive may include binary files, or {@code null} if the property
   *          should not be specified when the task is created (in which case
   *          the server will use a default behavior of excluding binary files).
   */
  public Boolean getIncludeBinaryFiles()
  {
    return includeBinaryFiles;
  }



  /**
   * Retrieves the value of a flag that indicates whether the support data
   * archive should include source code (if available) for any third-party
   * extensions installed in the server.
   *
   * @return  The value of a flag that indicates whether the support data
   *          archive should include source code (if available) for any
   *          third-party extensions installed in the server, or {@code null} if
   *          the property should not be specified when the task is created (in
   *          which case the server will use a default behavior of excluding
   *          extension source code).
   */
  public Boolean getIncludeExtensionSource()
  {
    return includeExtensionSource;
  }



  /**
   * Retrieves the value of a flag that indicates whether the server should
   * collect items for the support data archive in sequential mode rather than
   * in parallel.  Collecting data in sequential mode may reduce the amount of
   * memory consumed during the collection process, but it will take longer to
   * complete.
   *
   * @return  The value of a flag that indicates whether the server should
   *          collect items for the support data archive in sequential mode
   *          rather than in parallel, or {@code null} if the property should
   *          not be specified when the task is created (in which case the
   *          server will default to capturing data in parallel).
   */
  public Boolean getUseSequentialMode()
  {
    return useSequentialMode;
  }



  /**
   * Retrieves the security level that should be used to indicate which data
   * should be obscured, redacted, or omitted from the support data archive.
   *
   * @return  The security level that should be used when creating the support
   *          data archive, or {@code null} if the property should not be
   *          specified when the task is created (in which case the server will
   *          use a default security level).
   */
  public CollectSupportDataSecurityLevel getSecurityLevel()
  {
    return securityLevel;
  }



  /**
   * Retrieves the number of intervals that should be captured from tools that
   * use interval-based sampling (e.g., vmstat, iostat, mpstat, etc.).
   *
   * @return  The number of intervals that should be captured from tools that
   *          use interval-based sampling, or {@code null} if the property
   *          should not be specified when the task is created (in which case
   *          the server will use a default report count).
   */
  public Integer getReportCount()
  {
    return reportCount;
  }



  /**
   * Retrieves the interval duration in seconds that should be used for tools
   * that use interval-based sampling (e.g., vmstat, iostat, mpstat, etc.).
   *
   * @return  The interval duration in seconds that should be used for tools
   *          that use interval-based sampling, or {@code null} if the property
   *          should not be specified when the task is created (in which case
   *          the server will use a default report interval).
   */
  public Integer getReportIntervalSeconds()
  {
    return reportIntervalSeconds;
  }



  /**
   * Retrieves the number of times that the jstack utility should be invoked to
   * obtain stack traces from all threads in the server.
   *
   * @return  The number of times that the jstack utility should be invoked to
   *          obtain stack traces from all threads in the server, or
   *          {@code null} if the property should not be specified when the task
   *          is created (in which case the server will use a default count).
   */
  public Integer getJStackCount()
  {
    return jstackCount;
  }



  /**
   * Retrieves a string representation of the duration (up until the time that
   * the collect support data task is invoked) of log content that should be
   * included in the support data archive.
   *
   * @return  A string representation of the duration of log content that should
   *          be included in the support data archive, or {@code null} if the
   *          property should not be specified when the task is created (in
   *          which case the server will use a default behavior for selecting
   *          the amount of log content to include).
   */
  public String getLogDuration()
  {
    return logDuration;
  }



  /**
   * Retrieves a parsed value of the log duration in milliseconds.
   *
   * @return  A parsed value of the log duration in milliseconds or {@code null}
   *          if no log duration is set.
   *
   * @throws  TaskException  If the log duration value cannot be parsed as a
   *                         valid duration.
   */
  public Long getLogDurationMillis()
         throws TaskException
  {
    if (logDuration == null)
    {
      return null;
    }

    try
    {
      return DurationArgument.parseDuration(logDuration, TimeUnit.MILLISECONDS);
    }
    catch (final ArgumentException e)
    {
      Debug.debugException(e);
      throw new TaskException(e.getMessage(), e);
    }
  }



  /**
   * Retrieves an additional comment that should be included in the support data
   * archive.
   *
   * @return  An additional comment that should be included in the support data
   *          archive, or {@code null} if no comment should be included.
   */
  public String getComment()
  {
    return comment;
  }



  /**
   * Retrieves the minimum number of existing support data archives that should
   * be retained.
   *
   * @return  The minimum number of existing support data archives that should
   *          be retained, or {@code null} if there is no minimum retain count.
   */
  public Integer getRetainPreviousSupportDataArchiveCount()
  {
    return retainPreviousSupportDataArchiveCount;
  }



  /**
   * Retrieves the minimum age of existing support data archives that should be
   * retained.
   *
   * @return  The minimum age of existing support data archives that should
   *          be retained, or {@code null} if there is no minimum retain age.
   */
  public String getRetainPreviousSupportDataArchiveAge()
  {
    return retainPreviousSupportDataArchiveAge;
  }



  /**
   * Retrieves a parsed value of the retain previous support data archive age in
   * milliseconds.
   *
   * @return  A parsed value of the retain previous support data archive age in
   *          milliseconds or {@code null} if no retain age is set.
   *
   * @throws  TaskException  If the retain age value cannot be parsed as a valid
   *                         duration.
   */
  public Long getRetainPreviousSupportDataArchiveAgeMillis()
         throws TaskException
  {
    if (retainPreviousSupportDataArchiveAge == null)
    {
      return null;
    }

    try
    {
      return DurationArgument.parseDuration(
           retainPreviousSupportDataArchiveAge, TimeUnit.MILLISECONDS);
    }
    catch (final ArgumentException e)
    {
      Debug.debugException(e);
      throw new TaskException(e.getMessage(), e);
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected List<String> getAdditionalObjectClasses()
  {
    return Collections.singletonList(OC_COLLECT_SUPPORT_DATA_TASK);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected List<Attribute> getAdditionalAttributes()
  {
    final List<Attribute> attrList = new ArrayList<>(15);

    if (outputPath != null)
    {
      attrList.add(new Attribute(ATTR_OUTPUT_PATH, outputPath));
    }

    if (encryptionPassphraseFile != null)
    {
      attrList.add(new Attribute(ATTR_ENCRYPTION_PASSPHRASE_FILE,
           encryptionPassphraseFile));
    }

    if (includeExpensiveData != null)
    {
      attrList.add(new Attribute(ATTR_INCLUDE_EXPENSIVE_DATA,
           includeExpensiveData ? "TRUE" : "FALSE"));
    }

    if (includeReplicationStateDump != null)
    {
      attrList.add(new Attribute(ATTR_INCLUDE_REPLICATION_STATE_DUMP,
           includeReplicationStateDump ? "TRUE" : "FALSE"));
    }

    if (includeBinaryFiles != null)
    {
      attrList.add(new Attribute(ATTR_INCLUDE_BINARY_FILES,
           includeBinaryFiles ? "TRUE" : "FALSE"));
    }

    if (includeExtensionSource != null)
    {
      attrList.add(new Attribute(ATTR_INCLUDE_EXTENSION_SOURCE,
           includeExtensionSource ? "TRUE" : "FALSE"));
    }

    if (useSequentialMode != null)
    {
      attrList.add(new Attribute(ATTR_USE_SEQUENTIAL_MODE,
           useSequentialMode ? "TRUE" : "FALSE"));
    }

    if (securityLevel != null)
    {
      attrList.add(new Attribute(ATTR_SECURITY_LEVEL, securityLevel.getName()));
    }

    if (jstackCount != null)
    {
      attrList.add(new Attribute(ATTR_JSTACK_COUNT,
           String.valueOf(jstackCount)));
    }

    if (reportCount != null)
    {
      attrList.add(new Attribute(ATTR_REPORT_COUNT,
           String.valueOf(reportCount)));
    }

    if (reportIntervalSeconds != null)
    {
      attrList.add(new Attribute(ATTR_REPORT_INTERVAL_SECONDS,
           String.valueOf(reportIntervalSeconds)));
    }

    if (logDuration != null)
    {
      attrList.add(new Attribute(ATTR_LOG_DURATION, logDuration));
    }

    if (comment != null)
    {
      attrList.add(new Attribute(ATTR_COMMENT, comment));
    }

    if (retainPreviousSupportDataArchiveCount != null)
    {
      attrList.add(new Attribute(ATTR_RETAIN_PREVIOUS_ARCHIVE_COUNT,
           String.valueOf(retainPreviousSupportDataArchiveCount)));
    }

    if (retainPreviousSupportDataArchiveAge != null)
    {
      attrList.add(new Attribute(ATTR_RETAIN_PREVIOUS_ARCHIVE_AGE,
           retainPreviousSupportDataArchiveAge));
    }

    return attrList;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public List<TaskProperty> getTaskSpecificProperties()
  {
    return Collections.unmodifiableList(Arrays.asList(
         PROPERTY_OUTPUT_PATH,
         PROPERTY_ENCRYPTION_PASSPHRASE_FILE,
         PROPERTY_INCLUDE_EXPENSIVE_DATA,
         PROPERTY_INCLUDE_REPLICATION_STATE_DUMP,
         PROPERTY_INCLUDE_BINARY_FILES,
         PROPERTY_INCLUDE_EXTENSION_SOURCE,
         PROPERTY_USE_SEQUENTIAL_MODE,
         PROPERTY_SECURITY_LEVEL,
         PROPERTY_JSTACK_COUNT,
         PROPERTY_REPORT_COUNT,
         PROPERTY_REPORT_INTERVAL_SECONDS,
         PROPERTY_LOG_DURATION,
         PROPERTY_COMMENT,
         PROPERTY_RETAIN_PREVIOUS_ARCHIVE_COUNT,
         PROPERTY_RETAIN_PREVIOUS_ARCHIVE_AGE));
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public Map<TaskProperty,List<Object>> getTaskPropertyValues()
  {
    final Map<TaskProperty,List<Object>> props =
         new LinkedHashMap<>(StaticUtils.computeMapCapacity(20));

    if (outputPath != null)
    {
      props.put(PROPERTY_OUTPUT_PATH,
           Collections.<Object>singletonList(outputPath));
    }

    if (encryptionPassphraseFile != null)
    {
      props.put(PROPERTY_ENCRYPTION_PASSPHRASE_FILE,
           Collections.<Object>singletonList(encryptionPassphraseFile));
    }

    if (includeExpensiveData != null)
    {
      props.put(PROPERTY_INCLUDE_EXPENSIVE_DATA,
           Collections.<Object>singletonList(includeExpensiveData));
    }

    if (includeReplicationStateDump != null)
    {
      props.put(PROPERTY_INCLUDE_REPLICATION_STATE_DUMP,
           Collections.<Object>singletonList(includeReplicationStateDump));
    }

    if (includeBinaryFiles != null)
    {
      props.put(PROPERTY_INCLUDE_BINARY_FILES,
           Collections.<Object>singletonList(includeBinaryFiles));
    }

    if (includeExtensionSource != null)
    {
      props.put(PROPERTY_INCLUDE_EXTENSION_SOURCE,
           Collections.<Object>singletonList(includeExtensionSource));
    }

    if (useSequentialMode != null)
    {
      props.put(PROPERTY_USE_SEQUENTIAL_MODE,
           Collections.<Object>singletonList(useSequentialMode));
    }

    if (securityLevel != null)
    {
      props.put(PROPERTY_SECURITY_LEVEL,
           Collections.<Object>singletonList(securityLevel.getName()));
    }

    if (jstackCount != null)
    {
      props.put(PROPERTY_JSTACK_COUNT,
           Collections.<Object>singletonList(jstackCount.longValue()));
    }

    if (reportCount != null)
    {
      props.put(PROPERTY_REPORT_COUNT,
           Collections.<Object>singletonList(reportCount.longValue()));
    }

    if (reportIntervalSeconds != null)
    {
      props.put(PROPERTY_REPORT_INTERVAL_SECONDS,
           Collections.<Object>singletonList(
                reportIntervalSeconds.longValue()));
    }

    if (logDuration != null)
    {
      props.put(PROPERTY_LOG_DURATION,
           Collections.<Object>singletonList(logDuration));
    }

    if (comment != null)
    {
      props.put(PROPERTY_COMMENT,
           Collections.<Object>singletonList(comment));
    }

    if (retainPreviousSupportDataArchiveCount != null)
    {
      props.put(PROPERTY_RETAIN_PREVIOUS_ARCHIVE_COUNT,
           Collections.<Object>singletonList(
                retainPreviousSupportDataArchiveCount.longValue()));
    }

    if (retainPreviousSupportDataArchiveAge != null)
    {
      props.put(PROPERTY_RETAIN_PREVIOUS_ARCHIVE_AGE,
           Collections.<Object>singletonList(
                retainPreviousSupportDataArchiveAge));
    }

    props.putAll(super.getTaskPropertyValues());
    return Collections.unmodifiableMap(props);
  }
}
