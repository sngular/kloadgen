package net.coru.kloadgen.model.json;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public abstract class Field {

	String name;
	String type;

	public abstract Field cloneField(String fieldName);

	public abstract List<Field> getProperties();
}
