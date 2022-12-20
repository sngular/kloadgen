/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.exception;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class KLoadGenException extends RuntimeException {

  public KLoadGenException(final String message) {
    super(message);
  }

  public KLoadGenException(final Exception exc) {
    super(exc);
  }

  public KLoadGenException(final String message, final Exception exc) {
    super(message, exc);
  }
}