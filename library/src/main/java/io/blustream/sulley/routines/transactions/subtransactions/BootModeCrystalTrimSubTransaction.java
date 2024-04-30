package io.blustream.sulley.routines.transactions.subtransactions;

import androidx.annotation.NonNull;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleTransaction;

import io.blustream.logger.Log;
import io.blustream.sulley.mappers.CrystalTrimMapper;
import io.blustream.sulley.mappers.exceptions.MapperException;
import io.blustream.sulley.routines.OTAUBootModeBleDefinitions;

public class BootModeCrystalTrimSubTransaction extends BleTransaction {
    private int crystalTrim;
    private boolean mLog = false;
    private OTAUBootModeBleDefinitions mDefinitions;

    public int getCrystalTrim() {
        return crystalTrim;
    }

    public BootModeCrystalTrimSubTransaction(@NonNull OTAUBootModeBleDefinitions definitions) {
        mDefinitions = definitions;
    }

    protected void start(BleDevice device) {
        Log.i(getDevice().getName_override() + " Determining OTAU version");
        requestCrystalTrim();
    }

    private void requestCrystalTrim() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
          if (readWriteEvent.wasSuccess()) {
              Log.d("Request CrystalTrim success");
              readCrystalTrim();
          } else {
              Log.e("Failed to request BootModeCrystalTrim!");
              fail();
          }
        };

        getDevice().write(mDefinitions.getBootModeCrystalTrimService(), mDefinitions.getBootModeCrystalTrimKeyBlockCharacteristic(), new byte[] {(byte)2}, listener);
    }

    private void readCrystalTrim() {
        BleDevice.ReadWriteListener listener = readWriteEvent -> {
            if (readWriteEvent.wasSuccess()) {
                try {
                    crystalTrim = mapCrystalTrim(readWriteEvent.data());
                    Log.i(getDevice().getName_override() + " Boot Mode Crystal Trim is " + crystalTrim);
                } catch (MapperException e) {
                    Log.e("Failed to map Boot Mode Crystal Trim", e);
                } finally {
                    succeed();
                }
            } else {
                Log.e(getDevice().getName_override() + " Failed to get OTAU Version!");
                fail();
            }
        };

        getDevice().read(mDefinitions.getBootModeCrystalTrimService(), mDefinitions.getBootModeCrystalTrimDataTransferCharacteristic(),
                listener);
    }


    private Integer mapCrystalTrim(byte[] data) throws MapperException {
        CrystalTrimMapper mapper = new CrystalTrimMapper();
        return mapper.fromBytes(data);
    }
}
