/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.model.json;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Schema {

	String id;

	String name;

	String type;

	List<String> requiredFields;

	List<Field> properties;

	List<Field> definitions;

	public static class SchemaBuilder {

		List<Field> properties = new ArrayList<>();

		List<Field> definitions = new ArrayList<>();

		List<String> requiredFields = new ArrayList<>();

		public SchemaBuilder property(Field field) {
			properties.add(field);
			return this;
		}

		public SchemaBuilder properties(List<Field> fieldList) {
			properties.addAll(fieldList);
			return this;
		}

		public SchemaBuilder description(Field field) {
			definitions.add(field);
			return this;
		}

		public SchemaBuilder descriptions(List<Field> fieldList) {
			definitions.addAll(fieldList);
			return this;
		}

		public SchemaBuilder descriptions(Collection<Field> fieldList) {
			definitions.addAll(fieldList);
			return this;
		}

		public SchemaBuilder requiredField(String field) {
			requiredFields.add(field);
			return this;
		}

		public SchemaBuilder requiredFields(List<String> fields) {
			requiredFields.addAll(fields);
			return this;
		}

		public SchemaBuilder requiredFields(String[] fields) {
			requiredFields.addAll(asList(fields));
			return this;
		}
	}

}
