package disable_battery_optimizations.managers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import disable_battery_optimizations.devices.DeviceBase;
import disable_battery_optimizations.models.OptimizationVerificationStatus;
import disable_battery_optimizations.utils.LogUtils;
import disable_battery_optimizations.utils.PrefKeys;
import disable_battery_optimizations.utils.PrefUtils;

/**
 * Central manager for battery optimization checks and intent resolution.
 * 
 * Handles:
 * - Device detection and capability querying
 * - Status verification for 3 optimization types (Doze, Auto-start, Background Restriction)
 * - Intent generation for opening OEM-specific settings
 * 
 * THREAD-SAFE: Uses synchronized singleton pattern
 */
public class BatteryOptimizationManager {
    private static BatteryOptimizationManager instance;
    private final Context context;
    private final DeviceBase currentDevice;

    private BatteryOptimizationManager(Context context) {
        this.context = context.getApplicationContext();
        this.currentDevice = DeviceRegistry.findCurrentDevice(this.context);
        LogUtils.d("BatteryOptimizationManager", "Initialized with device: " + 
                (currentDevice != null ? currentDevice.getClass().getSimpleName() : "null"));
    }

    public static synchronized BatteryOptimizationManager getInstance(Context context) {
        if (instance == null) {
            instance = new BatteryOptimizationManager(context);
        }
        return instance;
    }

    // --- API 1: Check Battery Optimization (Android Standard Doze) ---
    /**
     * Checks if battery optimization (Doze mode) is disabled.
     * 
     * Returns true if:
     * - Device is verified as ignoring battery optimizations (PowerManager.isIgnoringBatteryOptimizations)
     * - Device requires manual confirmation and user has confirmed
     * - Device is pre-Android 6 (no Doze mode)
     * 
     * @return true if optimization is disabled, false otherwise
     */
    public boolean isBatteryOptimizationDisabled() {
        if (currentDevice != null) {
            OptimizationVerificationStatus status = currentDevice.checkBatteryOptimizationStatus(context);
            boolean result = (status == OptimizationVerificationStatus.VERIFIED || 
                            status == OptimizationVerificationStatus.USER_CONFIRMED);
            LogUtils.d("BatteryOptimizationManager", "isBatteryOptimizationDisabled: status=" + status + ", result=" + result);
            return result;
        }
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            LogUtils.d("BatteryOptimizationManager", "isBatteryOptimizationDisabled: API < 23, no Doze mode");
            return true;
        }
        
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean result = pm != null && pm.isIgnoringBatteryOptimizations(context.getPackageName());
        LogUtils.d("BatteryOptimizationManager", "isBatteryOptimizationDisabled (PowerManager check): " + result);
        return result;
    }

    // --- API 2: Check Auto Start (OEM Specific) ---
    /**
     * Checks if auto-start is enabled for this app.
     * 
     * @return VERIFIED if confirmed enabled, USER_CONFIRMED if user said so, NOT_SUPPORTED if device doesn't have it, UNKNOWN otherwise
     */
    public OptimizationVerificationStatus checkAutoStartStatus() {
        if (currentDevice != null) {
            OptimizationVerificationStatus status = currentDevice.checkAutoStartStatus(context);
            LogUtils.d("BatteryOptimizationManager", "checkAutoStartStatus: " + status);
            return status;
        }
        LogUtils.d("BatteryOptimizationManager", "checkAutoStartStatus: No device, returning NOT_SUPPORTED");
        return OptimizationVerificationStatus.NOT_SUPPORTED;
    }

    // --- API 3: Check Manufacturing Restrictions (Background Management) ---
    /**
     * Checks if background restrictions are disabled.
     * 
     * @return VERIFIED if confirmed unrestricted, USER_CONFIRMED if user said so, NOT_SUPPORTED if device doesn't have it, UNKNOWN otherwise
     */
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus() {
        if (currentDevice != null) {
            OptimizationVerificationStatus status = currentDevice.checkBackgroundRestrictionStatus(context);
            LogUtils.d("BatteryOptimizationManager", "checkBackgroundRestrictionStatus: " + status);
            return status;
        }
        LogUtils.d("BatteryOptimizationManager", "checkBackgroundRestrictionStatus: No device, returning NOT_SUPPORTED");
        return OptimizationVerificationStatus.NOT_SUPPORTED;
    }

    // --- API 4: Get Doze Mode Settings Intent ---
    /**
     * Gets the intent to open Doze/Battery optimization settings.
     * 
     * For OEM devices: Returns device-specific intent
     * For stock Android: Returns ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
     * 
     * @return Intent to open settings, or null if not needed
     */
    public Intent getDozeIntent() {
        if (currentDevice != null) {
            Intent intent = currentDevice.getActionDozeMode(context);
            LogUtils.d("BatteryOptimizationManager", "getDozeIntent: " + 
                    (intent != null ? intent.getAction() + "/" + intent.getComponent() : "null"));
            return intent;
        }
        return null;
    }

    // --- API 5: Get Auto Start Settings Intent ---
    /**
     * Gets the intent to open Auto-start settings.
     * 
     * @return Intent to open auto-start settings, or null if not supported
     */
    public Intent getAutoStartIntent() {
        if (currentDevice != null) {
            Intent intent = currentDevice.getActionAutoStart(context);
            LogUtils.d("BatteryOptimizationManager", "getAutoStartIntent: " + 
                    (intent != null ? intent.getAction() + "/" + intent.getComponent() : "null"));
            return intent;
        }
        LogUtils.d("BatteryOptimizationManager", "getAutoStartIntent: No device support");
        return null;
    }

    // --- API 6: Get Background Restriction Settings Intent ---
    /**
     * Gets the intent to open Background/Power saving settings.
     * 
     * @return Intent to open settings, or null if not supported
     */
    public Intent getBackgroundRestrictionIntent() {
        if (currentDevice != null) {
            Intent intent = currentDevice.getActionPowerSaving(context);
            LogUtils.d("BatteryOptimizationManager", "getBackgroundRestrictionIntent: " + 
                    (intent != null ? intent.getAction() + "/" + intent.getComponent() : "null"));
            return intent;
        }
        LogUtils.d("BatteryOptimizationManager", "getBackgroundRestrictionIntent: No device support");
        return null;
    }

    // --- Getters ---
    public DeviceBase getCurrentDevice() {
        return currentDevice;
    }
}
