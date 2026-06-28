package disable_battery_optimizations.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.Manufacturer;

/**
 * Represents Pixel, Motorola, Nokia, and other devices running near-stock Android.
 */
public class StandardDevice extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        // This is a fallback device, but we can detect "Google" or "Motorola" here if needed.
        return false; 
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        // Defaulting to a generic manufacturer or null
        return null;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                .setCanVerifyDoze(true)
                .setSupportsRestrictedSettings(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        return getActionDozeMode(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return null;
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Standard Android Device (AOSP-based)";
    }
}
