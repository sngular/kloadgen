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
public class ObjectField extends Field {

  List<Field> properties;

  List<String> required;

  @Override
  public List<Field> getProperties() {
    return properties;
  }

  @Builder(toBuilder = true)
  public ObjectField(String name, List<Field> properties, List<String> required) {
    super(name, "object");
    this.properties = properties;
    this.required = required;
  }

  @Override
  public Field cloneField(String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  public static class ObjectFieldBuilder {

    private final List<Field> properties = new ArrayList<>();

    private List<String> required = new ArrayList<>();

    public ObjectFieldBuilder properties(List<Field> fieldList) {
      properties.addAll(fieldList);
      return this;
    }

    public ObjectFieldBuilder property(Field field) {
      properties.add(field);
      return this;
    }

    public ObjectFieldBuilder required(String requiredField) {
      required.add(requiredField);
      return this;
    }

    public ObjectFieldBuilder required(List<String> requiredFields) {
      required = requiredFields;
      return this;
    }
  }

}
