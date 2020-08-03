/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.util.ListenToTest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
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

public class KLoadGenStandalone {

  public static final String JMETER_REPORT_OUTPUT_DIR_PROPERTY = "jmeter.reportgenerator.outputdir";

  private static final Logger log = Logger.getLogger("KLoadGenStandalone");

  public static void main(String... args) {
    Options options = createCLIOptions();

    CommandLineParser parser = new DefaultParser();
    try {

      CommandLine line = parser.parse(options, args);
      Path jMeterPropsFile = Paths.get(line.getOptionValue("h"));
      if (!Files.exists(jMeterPropsFile) || !Files.isReadable(jMeterPropsFile) || !Files.isDirectory(jMeterPropsFile)) {
        throw new KLoadGenException("JMeter properties File not Valid");
      }
      JMeterUtils.setJMeterHome(jMeterPropsFile.toAbsolutePath().toString());
      JMeterUtils.loadJMeterProperties(jMeterPropsFile.toAbsolutePath().toString() + "/bin/jmeter.properties");
      if (line.hasOption("o")) {
        Path optionalPropsFile = Paths.get(line.getOptionValue("o"));
        if (!Files.exists(optionalPropsFile) || !Files.isReadable(optionalPropsFile) || Files.isDirectory(optionalPropsFile)) {
          throw new KLoadGenException("Optionals properties File not Valid");
        }
        JMeterUtils.loadJMeterProperties(optionalPropsFile.toAbsolutePath().toString());
      }

      Path testPlanFile = Paths.get(line.getOptionValue("t"));
      if (!Files.exists(testPlanFile) || !Files.isReadable(testPlanFile) || Files.isDirectory(testPlanFile)) {
        throw new KLoadGenException("Test plan File not Valid");
      }

      if (line.hasOption("r")) {
        Path resultsFile = Paths.get(line.getOptionValue("r"));
        if (!Files.isDirectory(resultsFile)) {
          throw new KLoadGenException("Path is required to be a folder");
        }

        JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, resultsFile.toAbsolutePath().toString());
      }

      StandardJMeterEngine jmeter = new StandardJMeterEngine();

      JMeterUtils.initLocale();

      HashTree testPlanTree = SaveService.loadTree(testPlanFile.toFile());

      ReportGenerator reportGenerator = null;

      if (line.hasOption("l")) {
        Path resultsFile = Paths.get(line.getOptionValue("l"));
        if (Files.isDirectory(resultsFile)) {
          throw new KLoadGenException("Folders are not allow in this Option");
        }

        reportGenerator = createCollector(testPlanTree, resultsFile);
        testPlanTree.add(testPlanTree.getArray()[0], new ListenToTest(reportGenerator));
      }
      jmeter.configure(testPlanTree);
      jmeter.run();

    } catch (ParseException ex) {
      log.log(Level.SEVERE, "Parsing failed.  Reason: ", ex);
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("kloadgen", options);
    } catch (KLoadGenException | ConfigurationException ex) {
      log.log(Level.SEVERE, "Wrong parameters.  Reason: ", ex);
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("kloadgen", options);
    } catch (IOException ex) {
      log.log(Level.SEVERE, "Error accessing files.  Reason: ", ex);
    }

  }

  private static ReportGenerator createCollector(HashTree testPlanTree, Path resultsFile) throws ConfigurationException {
    Summariser summariser = null;
    String summariserName = JMeterUtils.getPropDefault("summariser.name", "KLoagGenSummariser");//$NON-NLS-1$
    if (summariserName.length() > 0) {
      log.info(String.format("Creating summariser <%s>", summariserName));
      summariser = new Summariser(summariserName);
    }
    ResultCollector resultCollector;
    resultCollector = new ResultCollector(summariser);
    resultCollector.setFilename(resultsFile.toAbsolutePath().toString());
    testPlanTree.add(testPlanTree.getArray()[0], resultCollector);
    return new ReportGenerator(resultsFile.toAbsolutePath().toString(), resultCollector);
  }

  private static Options createCLIOptions() {
    Options options = new Options();
    options.addOption(Option.builder("h").longOpt("jmeterHome").hasArg().desc("JMeter Properties file").required().build());
    options.addOption(Option.builder("o").longOpt("optionalPros").hasArg().desc("Optional properties file").build());
    options.addOption(Option.builder("t").longOpt("testPlan").hasArg().desc("Test plan file").required().build());
    options.addOption(Option.builder("r").longOpt("reportOutput").hasArg().desc("Report Output Folder").build());
    options.addOption(Option.builder("l").longOpt("logFileName").hasArg().desc("Jtl File where logs will be dump").build());
    return options;
  }
}
