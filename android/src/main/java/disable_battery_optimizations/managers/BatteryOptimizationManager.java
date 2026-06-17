package disable_battery_optimizations.managers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import disable_battery_optimizations.devices.DeviceBase;
import disable_battery_optimizations.models.OptimizationVerificationStatus;
import disable_battery_optimizations.utils.PrefKeys;
import disable_battery_optimizations.utils.PrefUtils;

public class BatteryOptimizationManager {
    private static BatteryOptimizationManager instance;
    private final Context context;
    private final DeviceBase currentDevice;

    private BatteryOptimizationManager(Context context) {
        this.context = context.getApplicationContext();
        this.currentDevice = DeviceRegistry.findCurrentDevice(this.context);
    }

    public static synchronized BatteryOptimizationManager getInstance(Context context) {
        if (instance == null) {
            instance = new BatteryOptimizationManager(context);
        }
        return instance;
    }

    // --- API 1: Check Battery Optimization (Android Standard Doze) ---
    public boolean isBatteryOptimizationDisabled() {
        if (currentDevice != null) {
            OptimizationVerificationStatus status = currentDevice.checkBatteryOptimizationStatus(context);
            return status == OptimizationVerificationStatus.VERIFIED || status == OptimizationVerificationStatus.USER_CONFIRMED;
        }
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    // --- API 2: Check Auto Start (OEM Specific) ---
    public OptimizationVerificationStatus checkAutoStartStatus() {
        if (currentDevice != null) {
            return currentDevice.checkAutoStartStatus(context);
        }
        return OptimizationVerificationStatus.NOT_SUPPORTED;
    }

    // --- API 3: Check Manufacturing Restrictions (Background Management) ---
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus() {
        if (currentDevice != null) {
            return currentDevice.checkBackgroundRestrictionStatus(context);
        }
        return OptimizationVerificationStatus.NOT_SUPPORTED;
    }

    // --- API 4: Disable Battery Optimization (Open Intent) ---
    public Intent getBatteryOptimizationIntent() {
        if (currentDevice != null) {
            return currentDevice.getActionDozeMode(context);
        }
        return null;
    }

    // --- API 5: Toggle/Open Auto Start Settings ---
    public Intent getAutoStartIntent() {
        if (currentDevice != null) {
            return currentDevice.getActionAutoStart(context);
        }
        return null;
    }

    // --- API 6: Disable Manufacturing Restrictions (Open Settings) ---
    public Intent getBackgroundRestrictionIntent() {
        if (currentDevice != null) {
            return currentDevice.getActionPowerSaving(context);
        }
        return null;
    }

    public DeviceBase getCurrentDevice() {
        return currentDevice;
    }
}
