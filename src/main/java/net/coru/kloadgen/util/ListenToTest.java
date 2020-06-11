package net.coru.kloadgen.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

@Slf4j
public class ListenToTest implements TestStateListener, Remoteable {

  private final ReportGenerator reportGenerator;

  private boolean remoteStop;

  /**
   * Listener for remote test
   * @param reportGenerator {@link ReportGenerator}
   */
  public ListenToTest( ReportGenerator reportGenerator) {
    this.remoteStop = remoteStop;
    this.reportGenerator = reportGenerator;
  }

  @Override
  // N.B. this is called by a daemon RMI thread from the remote host
  public void testEnded(String host) {
    final long now=System.currentTimeMillis();
    log.info("Finished remote host: {} ({})", host, now);
  }

  @Override
  public void testEnded() {
    endTest(false);
  }

  @Override
  public void testStarted(String host) {
    final long now=System.currentTimeMillis();
    log.info("Started remote host:  {} ({})", host, now);
  }

  @Override
  public void testStarted() {
    if (log.isInfoEnabled()) {
      final long now = System.currentTimeMillis();
      log.info("{} ({})", JMeterUtils.getResString("running_test"), now);//$NON-NLS-1$
    }
  }

  private void endTest(boolean isDistributed) {
    long now = System.currentTimeMillis();
    log.info("Tidying up ...    @ "+new Date(now)+" ("+now+")");

    if(reportGenerator != null) {
      try {
        log.info("Generating Dashboard");
        reportGenerator.generate();
        log.info("Dashboard generated");
      } catch (Exception ex) {
        log.error("Error generating the report: {}", ex.getMessage(), ex);
      }
    }
    checkForRemainingThreads();
    log.info("... end of run");
  }

  /**
   * Runs daemon thread which waits a short while;
   * if JVM does not exit, lists remaining non-daemon threads on stdout.
   */
  private void checkForRemainingThreads() {
    // This cannot be a JMeter class variable, because properties
    // are not initialised until later.
    final int pauseToCheckForRemainingThreads =
        JMeterUtils.getPropDefault("jmeter.exit.check.pause", 2000); // $NON-NLS-1$

    if (pauseToCheckForRemainingThreads > 0) {
      Thread daemon = new Thread(() -> {
        try {
          TimeUnit.MILLISECONDS.sleep(pauseToCheckForRemainingThreads); // Allow enough time for JVM to exit
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
        // This is a daemon thread, which should only reach here if there are other
        // non-daemon threads still active
        log.warn("The JVM should have exited but did not.");//NOSONAR
        log.warn("The following non-daemon threads are still running (DestroyJavaVM is OK):");//NOSONAR
        JOrphanUtils.displayThreads(false);
      });
      daemon.setDaemon(true);
      daemon.start();
    } else {
      log.debug("jmeter.exit.check.pause is <= 0, JMeter won't check for unterminated non-daemon threads");
    }
  }
}
