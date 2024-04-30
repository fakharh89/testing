package io.blustream.sulley.routines;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import io.blustream.logger.Log;
import io.blustream.sulley.sensor.Sensor;

public class DefinitionCheckHelper {

    private static boolean checkUUID(List<UUID> sensorsCharacteristics, UUID characteristics, String charName) {
        boolean res = false;
        if (sensorsCharacteristics.contains(characteristics)) {
            res = true;
        } else {
            Log.d(charName + " characteristic wrong");
        }
        return res;
    }

    private static List<UUID> getSensorsCharacteristicsAndServices(Sensor sensor) {
        Iterator<BluetoothGattCharacteristic> characteristicIterator = sensor.getBleDevice().getNativeCharacteristics();
        Iterator<BluetoothGattService> serviceIterator = sensor.getBleDevice().getNativeServices();
        List<UUID> sensorsCharacteristicsServices = new ArrayList<>();
        while (characteristicIterator.hasNext()) {
            sensorsCharacteristicsServices.add(characteristicIterator.next().getUuid());
        }
        while (serviceIterator.hasNext()) {
            sensorsCharacteristicsServices.add(serviceIterator.next().getUuid());
        }
        return sensorsCharacteristicsServices;
    }

    public static boolean checkSensor(Object definitions, Sensor sensor) {
        List<Boolean> checkResults = new ArrayList<>();
        List<UUID> sensorsUUIDs = getSensorsCharacteristicsAndServices(sensor);
        Class<Object> obj = (Class<Object>) definitions.getClass();
        for (Method method : obj.getDeclaredMethods()) {
            UUID result = null;
            Log.d("checkSensor.method = " + method.getName());
            Annotation bleCharacteristicAnnotation = method.getAnnotation(BleCharacteristic.class);
            Annotation bleServiceAnnotation = method.getAnnotation(BleService.class);
            if (bleServiceAnnotation == null && bleCharacteristicAnnotation == null) {
                continue;
            }
            try {
                result = (UUID) method.invoke(definitions);
                checkResults.add(checkUUID(sensorsUUIDs, result, method.getName()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.e("checkSensor. catch");
            }
        }
        return !checkResults.contains(false);
    }
}
