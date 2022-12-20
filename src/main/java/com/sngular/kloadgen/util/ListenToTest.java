/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

@Slf4j
public class ListenToTest implements TestStateListener, Remoteable {

  private final ReportGenerator reportGenerator;

  public ListenToTest(final ReportGenerator reportGenerator) {
    this.reportGenerator = reportGenerator;
  }

  @Override
  public final void testStarted() {
    if (log.isInfoEnabled()) {
      final long now = System.currentTimeMillis();
      log.info("{} ({})", JMeterUtils.getResString("running_test"), now);
    }
  }

  @Override
  public final void testStarted(final String host) {
    final long now = System.currentTimeMillis();
    log.info("Started remote host:  {} ({})", host, now);
  }

  @Override
  public final void testEnded() {
    endTest();
  }

  @Override
  public final void testEnded(final String host) {
    final long now = System.currentTimeMillis();
    log.info("Finished remote host: {} ({})", host, now);
  }

  private void endTest() {
    final long now = System.currentTimeMillis();
    log.info("Tidying up ...    @ " + new Date(now) + " (" + now + ")");

    if (reportGenerator != null) {
      try {
        log.info("Generating Dashboard");
        reportGenerator.generate();
        log.info("Dashboard generated");
      } catch (final GenerationException ex) {
        log.error("Error generating the report: {}", ex.getMessage(), ex);
      }
    }
    checkForRemainingThreads();
    log.info("... end of run");
  }

  private void checkForRemainingThreads() {
    final int pauseToCheckForRemainingThreads =
        JMeterUtils.getPropDefault("jmeter.exit.check.pause", 2000);

    if (pauseToCheckForRemainingThreads > 0) {
      final Thread daemon = new Thread(() -> {
        try {
          TimeUnit.MILLISECONDS.sleep(pauseToCheckForRemainingThreads);
        } catch (final InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
        log.warn("The JVM should have exited but did not.");
        log.warn("The following non-daemon threads are still running (DestroyJavaVM is OK):");
        JOrphanUtils.displayThreads(false);
      });
      daemon.setDaemon(true);
      daemon.start();
    } else {
      log.debug("jmeter.exit.check.pause is <= 0, JMeter won't check for unterminated non-daemon threads");
    }
  }
}
