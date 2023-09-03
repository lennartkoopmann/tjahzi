package pl.tkowalcz.tjahzi.reload4j.infra;

import org.apache.log4j.PropertyConfigurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Testcontainers
public class IntegrationTest {

    public static Network network = Network.newNetwork();

    @Container
    public GenericContainer loki = new GenericContainer("grafana/loki:2.3.0")
            .withNetwork(network)
            .withNetworkAliases("loki")
            .withCommand("-config.file=/etc/loki-config.yaml")
            .withClasspathResourceMapping("loki-config.yaml",
                    "/etc/loki-config.yaml",
                    BindMode.READ_ONLY
            )
            .waitingFor(
                    Wait.forHttp("/ready")
                            .forPort(3100)
            )
            .withExposedPorts(3100);

    @BeforeEach
    public void setUp() {
        System.setProperty("loki.host", loki.getHost());
        System.setProperty("loki.port", loki.getFirstMappedPort().toString());
    }

    public static void loadConfig(String fileName) {
        try {
            URL resource = IntegrationTest.class
                    .getClassLoader()
                    .getResource(fileName);

            if (resource == null) {
                Assertions.fail("Resource " + fileName + " not found");
            }

            URI uri = resource
                    .toURI();

            loadConfig(uri);
        } catch (URISyntaxException | MalformedURLException e) {
            Assertions.fail(e);
        }
    }

    public static void loadConfig(URI uri) throws MalformedURLException {
        PropertyConfigurator.configure(uri.toURL());
    }
}
