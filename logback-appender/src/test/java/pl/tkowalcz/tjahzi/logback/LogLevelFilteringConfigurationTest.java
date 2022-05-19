package pl.tkowalcz.tjahzi.logback;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import pl.tkowalcz.tjahzi.logback.infra.IntegrationTest;

import static org.hamcrest.CoreMatchers.*;
import static pl.tkowalcz.tjahzi.logback.infra.LokiAssert.assertThat;

class LogLevelFilteringConfigurationTest extends IntegrationTest {

    @Test
    void shouldNotLogBelowError() {
        // Given
        LoggerContext context = loadConfig("log-level-filtering-error-configuration.xml");
        Logger logger = context.getLogger(LogLevelFilteringConfigurationTest.class);

        String unexpectedLogLine = "Hello World";
        String expectedLogLine = "Error World";

        // When
        logger.info(unexpectedLogLine);
        logger.error(expectedLogLine);

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
                                                        containsString("p.t.t.l.LogLevelFilteringConfigurationTest - " + expectedLogLine),
                                                        not(
                                                                containsString("p.t.t.l.LogLevelFilteringConfigurationTest - " + unexpectedLogLine)
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }
}
