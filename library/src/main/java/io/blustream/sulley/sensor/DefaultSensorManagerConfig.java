package io.blustream.sulley.sensor;

import io.blustream.logger.models.LoggerConfig;

public class DefaultSensorManagerConfig implements SensorManagerConfig {

    private final String[] mCompatibleSensorIdentifiers = {"01", "10", "02", "42"};
    private LoggerConfig loggerConfig;

    public DefaultSensorManagerConfig() {
        loggerConfig = new LoggerConfig()
                .enableConsoleLogging()
                .includeClassNames(true)
                .includeMethodNames(true)
                .includeLineNumbers(true);
    }

    @Override
    public String[] getCompatibleSensorIdentifiers() {
        return mCompatibleSensorIdentifiers;
    }

    @Override
    public void setLoggerConfig(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
    }

    @Override
    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }
}
