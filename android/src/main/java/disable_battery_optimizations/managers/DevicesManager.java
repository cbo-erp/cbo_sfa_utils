package disable_battery_optimizations.managers;

import android.content.Context;
import disable_battery_optimizations.devices.DeviceBase;

/**
 * Legacy manager for backward compatibility.
 * Delegates to the new DeviceRegistry.
 */
public class DevicesManager {
    
    public static DeviceBase getDevice() {
        // This is a legacy call without context. 
        // We use DeviceRegistry but we don't have context here.
        // Usually getDevice is called after some initialization or we can try to find it without context
        // if the device implementations don't strictly need context for isThatRom()
        return DeviceRegistry.findCurrentDevice(null);
    }

    public static DeviceBase getDevice(Context context) {
        return DeviceRegistry.findCurrentDevice(context);
    }
}
