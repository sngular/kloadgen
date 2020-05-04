package net.coru.kloadgen.util;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StatelessRandomToolTest {

  private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2019, 12, 06, 12, 00, 00);

  

  private static Stream<Arguments> parametersForGenerateRandomValueForField() {
    return Stream.of(
        Arguments.of("name", "string", 1, Collections.singletonList("testString"), "testString"),
        Arguments.of("name", "int", 1, Collections.singletonList("1"), 1), Arguments.of("name", "long", 1, Collections.singletonList("1"), 1L),
        Arguments.of("name", "short", 1, Collections.singletonList("1"), (short) 1),
        Arguments.of("name", "double", 1, Collections.singletonList("1.0"), 1.0),
        Arguments.of("name", "timestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), FIXED_DATE),
        Arguments.of("name", "longTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"),
            FIXED_DATE.toInstant(ZoneOffset.UTC).toEpochMilli()),
        Arguments.of("name", "stringTimestamp", 1, Collections.singletonList("2019-12-06T12:00:00"), "2019-12-06T12:00"),
        Arguments.of("name", "uuid", 1, Collections.singletonList("0177f035-e51c-4a46-8b82-5b157371c2a5"),
            UUID.fromString("0177f035-e51c-4a46-8b82-5b157371c2a5")),
        Arguments.of("name", "boolean", 1, Collections.singletonList("true"), Boolean.TRUE));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateRandomValueForField")
  void testGenerateRandomValueForField(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList, Object expected) {
    assertThat(new StatelessRandomTool().generateRandom(fieldName, fieldType, valueLength, fieldValuesList)).isEqualTo(expected);
  }

  private static Stream<Arguments> parametersForGenerateSequenceValueForField() {
    return Stream.of(
        Arguments.of("name", "seq", 1, Collections.singletonList("0"), "0"), Arguments.of("name", "seq", 1, Collections.singletonList("1"), "1"),
        Arguments.of("name", "seq", 1, Collections.singletonList("2"), "2"));
  }

  @ParameterizedTest
  @MethodSource("parametersForGenerateSequenceValueForField")
  void testGenerateSequenceValueForField(String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList,
      Object expectedTyped) {
    assertThat(new StatelessRandomTool().generateRandom(fieldName, fieldType, valueLength, fieldValuesList)).isEqualTo(expectedTyped);
  }

}
