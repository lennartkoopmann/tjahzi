package pl.tkowalcz.tjahzi.reload4j;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import pl.tkowalcz.tjahzi.reload4j.infra.IntegrationTest;

import static org.hamcrest.CoreMatchers.*;
import static pl.tkowalcz.tjahzi.reload4j.infra.LokiAssert.assertThat;

class LokiAppenderTest extends IntegrationTest {

    @Test
    void shouldSendData() {
        // Given
        loadConfig("basic-appender-test-configuration.properties");
        Logger logger = org.apache.log4j.LogManager.getLogger(LokiAppenderTest.class);

        String expectedLogLine = "Hello World";

        // When
        logger.info(expectedLogLine);

        // Then
        assertThat(loki)
                .returns(response -> response
                        .body("data.result.size()", equalTo(1))
                        .body("data.result[0].stream.server", equalTo("127.0.0.1"))
                        .body(
                                "data.result.values",
                                hasItems(
                                        hasItems(
                                                hasItems(
                                                        containsString("INFO LokiAppenderTest - " + expectedLogLine)
                                                )
                                        )
                                )
                        )
                );
    }
}
