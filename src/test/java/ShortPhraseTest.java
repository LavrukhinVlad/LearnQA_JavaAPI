import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShortPhraseTest {

    @ParameterizedTest
    @ValueSource(strings = {"Hello, LearnQA world", "This phrase is long enough", "Automation testing on Java"})
    public void testStringLength(String phrase) {
        assertEquals(true, phrase.length() > 15, "The phrase is not longer than 15 characters: " + phrase);
    }
}
