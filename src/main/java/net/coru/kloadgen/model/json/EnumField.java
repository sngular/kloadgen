package net.coru.kloadgen.model.json;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EnumField extends Field {

  String defaultValue;

  @Singular
  List<String> enumValues;

  @Override
  public List<Field> getProperties() {
    return Collections.singletonList(this);
  }

  @Builder(toBuilder = true)
  public EnumField(String name, String defaultValue, List<String> enumValues) {
    super(name, "enum");
    this.defaultValue = defaultValue;
    this.enumValues = enumValues;
  }

  @Override
  public Field cloneField(String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }
}
