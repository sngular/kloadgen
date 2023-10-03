/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.testutil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public final class FileHelper {

  public File getFile(final String fileName) {
    final URL url = this.getClass().getResource(fileName);
    return new File(url.getFile());
  }

  public String getContent(final String fileName) throws IOException {
    return IOUtils.toString(
        getClass().getResourceAsStream(fileName),
        StandardCharsets.UTF_8
    );
  }
}

