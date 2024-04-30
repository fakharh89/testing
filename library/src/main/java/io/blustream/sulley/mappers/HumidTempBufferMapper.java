package io.blustream.sulley.mappers;

import io.blustream.sulley.models.HumidTempSample;

public class HumidTempBufferMapper extends AbstractBufferMapper<HumidTempSample> {
    private HumidTempMapper mHumidTempMapper = new HumidTempMapper();

    @Override
    public HumidTempMapper getSubsampleByteMapper() {
        return mHumidTempMapper;
    }
}
