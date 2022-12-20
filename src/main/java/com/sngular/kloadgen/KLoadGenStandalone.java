/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.util.ListenToTest;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public final class KLoadGenStandalone {

  public static final String JMETER_REPORT_OUTPUT_DIR_PROPERTY = "jmeter.reportgenerator.outputdir";

  private static final Logger LOG = Logger.getLogger("KLoadGenStandalone");

  private KLoadGenStandalone() {}

  public static void main(final String... args) {
    final var options = createCLIOptions();

    final var parser = new DefaultParser();
    try {

      final var line = parser.parse(options, args);
      final var jMeterPropsFile = Paths.get(line.getOptionValue("h"));
      if (!Files.exists(jMeterPropsFile) || !Files.isReadable(jMeterPropsFile) || !Files.isDirectory(jMeterPropsFile)) {
        throw new KLoadGenException("JMeter properties File not Valid");
      }
      JMeterUtils.setJMeterHome(jMeterPropsFile.toAbsolutePath().toString());
      JMeterUtils.loadJMeterProperties(jMeterPropsFile.toAbsolutePath() + "/bin/jmeter.properties");
      if (line.hasOption("o")) {
        final var optionalPropsFile = Paths.get(line.getOptionValue("o"));
        if (!Files.exists(optionalPropsFile) || !Files.isReadable(optionalPropsFile) || Files.isDirectory(optionalPropsFile)) {
          throw new KLoadGenException("Optionals properties File not Valid");
        }
        JMeterUtils.loadJMeterProperties(optionalPropsFile.toAbsolutePath().toString());
      }

      final var testPlanFile = Paths.get(line.getOptionValue("t"));
      if (!Files.exists(testPlanFile) || !Files.isReadable(testPlanFile) || Files.isDirectory(testPlanFile)) {
        throw new KLoadGenException("Test plan File not Valid");
      }

      if (line.hasOption("r")) {
        final var resultsFile = Paths.get(line.getOptionValue("r"));
        if (!Files.isDirectory(resultsFile)) {
          throw new KLoadGenException("Path is required to be a folder");
        }

        JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, resultsFile.toAbsolutePath().toString());
      }

      final var jmeter = new StandardJMeterEngine();

      JMeterUtils.initLocale();

      final var testPlanTree = SaveService.loadTree(testPlanFile.toFile());

      ReportGenerator reportGenerator = null;

      if (line.hasOption("l")) {
        final var resultsFile = Paths.get(line.getOptionValue("l"));
        if (Files.isDirectory(resultsFile)) {
          throw new KLoadGenException("Folders are not allow in this Option");
        }

        reportGenerator = createCollector(testPlanTree, resultsFile);
        testPlanTree.add(testPlanTree.getArray()[0], new ListenToTest(reportGenerator));
      }
      jmeter.configure(testPlanTree);
      jmeter.run();

    } catch (final ParseException ex) {
      LOG.log(Level.SEVERE, "Parsing failed.  Reason: ", ex);
      final var formatter = new HelpFormatter();
      formatter.printHelp("kloadgen", options);
    } catch (KLoadGenException | ConfigurationException ex) {
      LOG.log(Level.SEVERE, "Wrong parameters.  Reason: ", ex);
      final var formatter = new HelpFormatter();
      formatter.printHelp("kloadgen", options);
    } catch (final IOException ex) {
      LOG.log(Level.SEVERE, "Error accessing files.  Reason: ", ex);
    }

  }

  private static Options createCLIOptions() {
    final var options = new Options();
    options.addOption(Option.builder("h").longOpt("jmeterHome").hasArg().desc("JMeter Properties file").required().build());
    options.addOption(Option.builder("o").longOpt("optionalPros").hasArg().desc("Optional properties file").build());
    options.addOption(Option.builder("t").longOpt("testPlan").hasArg().desc("Test plan file").required().build());
    options.addOption(Option.builder("r").longOpt("reportOutput").hasArg().desc("Report Output Folder").build());
    options.addOption(Option.builder("l").longOpt("logFileName").hasArg().desc("Jtl File where logs will be dump").build());
    return options;
  }

  private static ReportGenerator createCollector(final HashTree testPlanTree, final Path resultsFile) throws ConfigurationException {
    Summariser summariser = null;
    final var summariserName = JMeterUtils.getPropDefault("summariser.name", "KLoagGenSummariser");
    if (summariserName.length() > 0) {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info(String.format("Creating summariser <%s>", summariserName));
      }
      summariser = new Summariser(summariserName);
    }
    final var resultCollector = new ResultCollector(summariser);
    resultCollector.setFilename(resultsFile.toAbsolutePath().toString());
    testPlanTree.add(testPlanTree.getArray()[0], resultCollector);
    return new ReportGenerator(resultsFile.toAbsolutePath().toString(), resultCollector);
  }
}
