/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.exception;

public class KLoadGenException extends RuntimeException {

    public KLoadGenException(String message) {
        super(message);
    }

    public KLoadGenException(Exception exc) {
        super(exc);
    }

    public KLoadGenException(String message, Exception exc) {
        super(message, exc);
    }
}