package net.coru.kloadgen.model.json;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ArrayField extends Field {

  List<Field> values;

  int minItems;

  boolean uniqueItems;

  @Builder(toBuilder = true)
  public ArrayField(String name, List<Field> values, int minItems, boolean uniqueItems) {
    super(name, "array");
    this.values = values;
    this.minItems = minItems;
    this.uniqueItems = uniqueItems;
  }

  @Override
  public Field cloneField(String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  @Override
  public List<Field> getProperties() {
    return values;
  }

  public static class ArrayFieldBuilder {

    private final List<Field> values = new ArrayList<>();

    public ArrayFieldBuilder values(List<Field> values) {
      this.values.addAll(values);
      return this;
    }

    public ArrayFieldBuilder value(Field value) {
      this.values.add(value);
      return this;
    }
  }
}
