/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor.extractors;

import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;

public interface Extractor<T> {
  List<FieldValueMapping> processSchema(final T schema);

  List<String> getSchemaNameList(final String schema);

}
