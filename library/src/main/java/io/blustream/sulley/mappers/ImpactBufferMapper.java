package io.blustream.sulley.mappers;

import io.blustream.sulley.models.ImpactSample;

public class ImpactBufferMapper extends AbstractBufferMapper<ImpactSample> {
    private ImpactMapper mImpactMapper = new ImpactMapper();

    @Override
    public ImpactMapper getSubsampleByteMapper() {
        return mImpactMapper;
    }
}
