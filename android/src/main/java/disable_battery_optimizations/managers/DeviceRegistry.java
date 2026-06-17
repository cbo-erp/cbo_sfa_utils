package disable_battery_optimizations.managers;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import disable_battery_optimizations.devices.*;

public class DeviceRegistry {
    private static final List<DeviceBase> registeredDevices = new ArrayList<>();
    private static final DeviceBase standardDevice = new StandardDevice();

    static {
        register(new Asus());
        register(new Huawei());
        register(new Letv());
        register(new Meizu());
        register(new OnePlus());
        register(new Oppo());
        register(new Vivo());
        register(new HTC());
        register(new Samsung());
        register(new Xiaomi());
        register(new ZTE());
    }

    public static void register(DeviceBase device) {
        registeredDevices.add(0, device); // Add to front so new registrations take priority
    }

    public static DeviceBase findCurrentDevice(Context context) {
        for (DeviceBase device : registeredDevices) {
            if (device.isThatRom()) {
                return device;
            }
        }
        return standardDevice;
    }
    
    public static List<DeviceBase> getAllDevices() {
        return new ArrayList<>(registeredDevices);
    }
}
