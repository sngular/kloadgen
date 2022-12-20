/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model;

public enum ConstraintTypeEnum {
  MINIMUM_VALUE,
  MAXIMUM_VALUE,
  EXCLUDED_MINIMUM_VALUE,
  EXCLUDED_MAXIMUM_VALUE,
  MULTIPLE_OF,
  REGEX,
  FORMAT,
  PRECISION,
  SCALE
}
