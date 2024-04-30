package io.blustream.sulley.mappers;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.models.Sample;

abstract class AbstractBufferMapper<T extends Sample> implements BufferMapper<T> {
    @Override
    @NonNull
    public List<T> fromBytes(byte[] bytes) throws MapperException {
        short packetLength = getSubsampleByteMapper().expectedReadLength();

        if (bytes.length % packetLength != 0
                || bytes.length == 0) {
            throw new MapperException.InvalidLength();
        }

        List<T> packetResults = new ArrayList<>();

        for (int i = 0; i < (bytes.length / packetLength); i++) {
            int packetStart = packetLength * i;
            int packetEnd = packetStart + packetLength;
            byte[] packet = Arrays.copyOfRange(bytes, packetStart, packetEnd);
            T packetResult;
            try {
                packetResult = getSubsampleByteMapper().fromBytes(packet);
            } catch (MapperException ex) {
                Log.e("Invalid dataPoint");
                continue;
            }
            packetResults.add(packetResult);
        }

        // TODO Collections.unmodifiableList()

        return packetResults;
    }
}