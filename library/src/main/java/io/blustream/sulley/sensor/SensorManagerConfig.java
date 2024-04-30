package io.blustream.sulley.sensor;

import io.blustream.logger.models.LoggerConfig;

public interface SensorManagerConfig {

    String[] getCompatibleSensorIdentifiers();

    void setLoggerConfig(LoggerConfig loggerConfig);

    LoggerConfig getLoggerConfig();
}
