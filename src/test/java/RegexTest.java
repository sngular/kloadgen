import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class RegexTest {

  private Pattern pattern = Pattern.compile("(\\d+)(.*)");

  @Test
  public void testRegExp() {
    Matcher matcher = pattern.matcher("30 rep");

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(1)).isEqualToIgnoringCase("30");
    assertThat(matcher.group(2).trim()).isEqualToIgnoringCase("rep");

    matcher = pattern.matcher("30");

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.groupCount()).isEqualTo(2);
    assertThat(matcher.group(1)).isEqualToIgnoringCase("30");
    assertThat(matcher.group(2)).isEmpty();

    matcher = pattern.matcher("30`");

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.groupCount()).isEqualTo(2);
    assertThat(matcher.group(1)).isEqualToIgnoringCase("30");
    assertThat(matcher.group(2)).isEqualToIgnoringCase("`");
  }

}
