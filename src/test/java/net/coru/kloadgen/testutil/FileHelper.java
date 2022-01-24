/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.testutil;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public final class FileHelper {

  public File getFile(String fileName) {
    URL url = this.getClass().getResource(fileName);
    return new File(url.getFile());
  }

  public String getContent(String fileName) throws Exception {
    return IOUtils.toString(
        getClass().getResourceAsStream(fileName),
        StandardCharsets.UTF_8
    );
  }
}

