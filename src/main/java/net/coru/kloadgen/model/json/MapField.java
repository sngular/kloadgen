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
public class MapField extends Field{

	Field mapType;

	@Override
	public List<Field> getProperties() {
		return Collections.singletonList(this);
	}

	@Builder(toBuilder = true)
	public MapField(String name, Field mapType) {
		super(name, "map");
		this.mapType = mapType;
	}

	@Override
	public Field cloneField(String fieldName) {
		return this.toBuilder().name(fieldName).build();
	}
}
