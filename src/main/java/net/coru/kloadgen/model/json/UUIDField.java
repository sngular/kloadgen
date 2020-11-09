package net.coru.kloadgen.model.json;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UUIDField extends Field {

	String regex;

	String format;

	@Override
	public List<Field> getProperties() {
		return Collections.singletonList(this);
	}

	@Builder(toBuilder = true)
	public UUIDField(String name, String regex, String format) {
		super(name, "uuid");
		this.regex = regex;
		this.format = format;
	}

	@Override
	public Field cloneField(String fieldName) {
		return this.toBuilder().name(fieldName).build();
	}
}
