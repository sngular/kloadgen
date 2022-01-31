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
public class DateField extends Field {

  String format;

  @Builder(toBuilder = true)
  public DateField(String name, String format) {
    super(name, "timestamp");
    this.format = format;
  }

  @Override
  public Field cloneField(String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  @Override
  public List<Field> getProperties() {
    return Collections.singletonList(this);
  }
}
