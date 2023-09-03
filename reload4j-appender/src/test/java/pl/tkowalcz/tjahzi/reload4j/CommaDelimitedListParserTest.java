package pl.tkowalcz.tjahzi.reload4j;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommaDelimitedListParserTest {

    @Test
    public void testHappyPathScenario() {
        String input = "server-address:10.1.1.2,organisation:foo,namespace:bar";
        Label[] labels = CommaDelimitedListParser.parseString(input, Label::createLabel).toArray(new Label[0]);

        assertEquals(3, labels.length);
        assertEquals("server-address", labels[0].getName());
        assertEquals("10.1.1.2", labels[0].getValue());
        assertEquals("organisation", labels[1].getName());
        assertEquals("foo", labels[1].getValue());
        assertEquals("namespace", labels[2].getName());
        assertEquals("bar", labels[2].getValue());
    }

    @Test
    public void testEmptyStringScenario() {
        String input = "";
        List<Label> labels = CommaDelimitedListParser.parseString(input, Label::createLabel);

        assertThat(labels).isEmpty();
    }

    @Test
    public void testNullInputScenario() {
        List<Label> labels = CommaDelimitedListParser.parseString(null, Label::createLabel);

        assertThat(labels).isEmpty();
    }

    @Test
    public void testMalformedInputScenario() {
        String input = "server-address:10.1.1.2,organisation:foo,namespace:";
        Label[] labels = CommaDelimitedListParser.parseString(input, Label::createLabel).toArray(new Label[0]);

        assertEquals(2, labels.length);
        assertEquals("server-address", labels[0].getName());
        assertEquals("10.1.1.2", labels[0].getValue());
        assertEquals("organisation", labels[1].getName());
        assertEquals("foo", labels[1].getValue());
    }

    @Test
    public void testWhitespaceHandlingScenario() {
        String input = " server-address : 10.1.1.2 , organisation : foo , namespace : bar ";
        Label[] labels = CommaDelimitedListParser.parseString(input, Label::createLabel).toArray(new Label[0]);

        assertEquals(3, labels.length);
        assertEquals("server-address", labels[0].getName());
        assertEquals("10.1.1.2", labels[0].getValue());
        assertEquals("organisation", labels[1].getName());
        assertEquals("foo", labels[1].getValue());
        assertEquals("namespace", labels[2].getName());
        assertEquals("bar", labels[2].getValue());
    }

    @Test
    public void testSingleKeyValuePairScenario() {
        String input = "server-address:10.1.1.2";
        Label[] labels = CommaDelimitedListParser.parseString(input, Label::createLabel).toArray(new Label[0]);

        assertEquals(1, labels.length);
        assertEquals("server-address", labels[0].getName());
        assertEquals("10.1.1.2", labels[0].getValue());
    }

    @Test
    public void testMissingValueScenario() {
        String input = "server-address:";
        List<Label> labels = CommaDelimitedListParser.parseString(input, Label::createLabel);

        assertThat(labels).isEmpty();
    }

    @Test
    public void testMissingKeyScenario() {
        String input = ":10.1.1.2";
        List<Label> labels = CommaDelimitedListParser.parseString(input, Label::createLabel);

        assertThat(labels).isEmpty();
    }

    @Test
    public void testMixedInvalidAndValidLabels() {
        String input = "server-address:10.1.1.2,organisation:foo,invalidLabel:,namespace:bar, :value";
        Label[] labels = CommaDelimitedListParser.parseString(input, Label::createLabel).toArray(new Label[0]);
        ;

        assertEquals(3, labels.length);
        assertEquals("server-address", labels[0].getName());
        assertEquals("10.1.1.2", labels[0].getValue());
        assertEquals("organisation", labels[1].getName());
        assertEquals("foo", labels[1].getValue());
        assertEquals("namespace", labels[2].getName());
        assertEquals("bar", labels[2].getValue());
    }
}
