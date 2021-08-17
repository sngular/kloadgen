package net.coru.kloadgen.randomtool.random;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomObjectTest {

  private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2019, 12, 6, 12, 0, 0);

  private static Stream<Arguments> parametersForGenerateSingleRandomValue() {
    return Stream.of(
        Arguments.of("string", 1, Collections.singletonList("testString"), "testString"),
        Arguments.of("int", 1, Collections.singletonList("1"), 1),
        Arguments.of("long", 1, Collections.singletonList("1"), 1L),
        Arguments.of("short", 1, Collections.singletonList("1"), (short) 1),
        Arguments.of("double", 1, Collections.singletonList("1.0"), 1.0),
        Arguments.of("float", 1, Collections.singletonList("1.0"), 1.0f),
        Arguments.of("timestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), FIXED_DATE),
        Arguments.of(
            "longTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), FIXED_DATE.toInstant(ZoneOffset.UTC).toEpochMilli()
        ),
        Arguments.of("stringTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), "2019-12-06T12:00"),
        Arguments.of(
            "uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
            UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")
        ),
        Arguments.of("boolean", 1, Collections.singletonList("true"), Boolean.TRUE)
    );
  }

  private static Stream<Arguments> parametersForGenerateSequenceValueForField() {
    return Stream.of(
        Arguments.of("name", "seq", Collections.singletonList("0"), new HashMap<>(), "0", 0L),
        Arguments.of("name", "seq", Collections.singletonList("1"), new HashMap<>(Maps.of("name", 15L)), "16", 16L));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSingleRandomValue")
  void generateSingleRandomValue(String fieldType, Integer valueLength, List<String> fieldValuesList, Object expected) {
    assertThat(new RandomObject().generateRandom(fieldType, valueLength, fieldValuesList, Collections.emptyMap())).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSequenceValueForField")
  void testGenerateSequenceValueForField(String fieldName, String fieldType, List<String> fieldValuesList, Map<String, Object> context,
      Object expectedTyped, Object expectedStored) {

    assertThat(new RandomObject().generateSeq(fieldName, fieldType, fieldValuesList, context)).isEqualTo(expectedTyped);
    assertThat(context).containsEntry(fieldName,expectedStored);
  }
}