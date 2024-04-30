package io.blustream.sulley.mappers;

import io.blustream.sulley.models.MotionSample;

public class MotionBufferMapper extends AbstractBufferMapper<MotionSample> {
    private MotionMapper mMotionMapper = new MotionMapper();

    public MotionMapper getSubsampleByteMapper() {
        return mMotionMapper;
    }
}
