package pl.tkowalcz.tjahzi.reload4j;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import pl.tkowalcz.tjahzi.LabelSerializer;
import pl.tkowalcz.tjahzi.LabelSerializers;
import pl.tkowalcz.tjahzi.LoggingSystem;
import pl.tkowalcz.tjahzi.TjahziLogger;
import pl.tkowalcz.tjahzi.stats.MonitoringModule;
import pl.tkowalcz.tjahzi.stats.MutableMonitoringModuleWrapper;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LokiAppender extends LokiAppenderConfigurator {

    private LoggingSystem loggingSystem;
    private TjahziLogger logger;

    private String logLevelLabel;
    private String loggerNameLabel;
    private String threadNameLabel;
    private List<String> mdcLogLabels;

    private MutableMonitoringModuleWrapper monitoringModuleWrapper;

    /**
     * This is an entry point to set monitoring (statistics) hooks for this appender. This
     * API is in beta and is subject to change (and probably will).
     */
    public void setMonitoringModule(MonitoringModule monitoringModule) {
        monitoringModuleWrapper.setMonitoringModule(monitoringModule);
    }

    // @VisibleForTesting
    public LoggingSystem getLoggingSystem() {
        return loggingSystem;
    }

    @Override
    public void activateOptions() {
        LokiAppenderFactory lokiAppenderFactory = new LokiAppenderFactory(this);
        loggingSystem = lokiAppenderFactory.createAppender();
        logLevelLabel = lokiAppenderFactory.getLogLevelLabel();
        loggerNameLabel = lokiAppenderFactory.getLoggerNameLabel();
        threadNameLabel = lokiAppenderFactory.getThreadNameLabel();
        mdcLogLabels = lokiAppenderFactory.getMdcLogLabels();
        monitoringModuleWrapper = lokiAppenderFactory.getMonitoringModuleWrapper();

        logger = loggingSystem.createLogger();
        loggingSystem.start();
    }

    @Override
    protected void append(LoggingEvent event) {
        String logLevel = event.getLevel().toString();
        String loggerName = event.getLoggerName();
        String threadName = event.getThreadName();

        String message = layout.format(event);

        LabelSerializer labelSerializer = LabelSerializers.threadLocal();
        appendLogLabel(labelSerializer, logLevel);
        appendLoggerLabel(labelSerializer, loggerName);
        appendThreadLabel(labelSerializer, threadName);
        appendMdcLogLabels(labelSerializer, event);

        logger.log(
                event.getTimeStamp(),
                0L,
                labelSerializer,
                ByteBuffer.wrap(message.getBytes())
        );
    }

    private void appendLogLabel(LabelSerializer labelSerializer, String logLevel) {
        if (logLevelLabel != null) {
            labelSerializer.appendLabel(logLevelLabel, logLevel);
        }
    }

    private void appendLoggerLabel(LabelSerializer labelSerializer, String loggerName) {
        if (loggerNameLabel != null) {
            labelSerializer.appendLabel(loggerNameLabel, loggerName);
        }
    }

    private void appendThreadLabel(LabelSerializer labelSerializer, String threadName) {
        if (threadNameLabel != null) {
            labelSerializer.appendLabel(threadNameLabel, threadName);
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach") // Allocator goes brrrr
    private void appendMdcLogLabels(LabelSerializer serializer,
                                    LoggingEvent mdcPropertyMap) {
        for (int i = 0; i < mdcLogLabels.size(); i++) {
            String mdcLogLabel = mdcLogLabels.get(i);

            Object mdcValue = mdcPropertyMap.getMDC(mdcLogLabel);
            if (mdcValue != null) {
                serializer.appendLabel(mdcLogLabel, mdcValue.toString());
            }
        }
    }

    public void close() {
        loggingSystem.close(
                (int) TimeUnit.SECONDS.toMillis(getShutdownTimeoutSeconds()),
                thread -> LogLog.error("Loki appender was unable to stop thread on shutdown: " + thread)
        );
    }

    public boolean requiresLayout() {
        return true;
    }
}
