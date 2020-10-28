package net.coru.kloadgen.testutil;

import java.io.File;
import java.net.URL;

public final class FileHelper {

  public File getFile(String fileName) {
    URL url = this.getClass().getResource(fileName);
    return new File(url.getFile());
  }
}

