package disable_battery_optimizations.devices;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.DrawableRes;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.models.OptimizationVerificationStatus;
import disable_battery_optimizations.utils.Manufacturer;

public interface DeviceBase {
    boolean isThatRom();
    Manufacturer getDeviceManufacturer();
    
    DeviceCapabilities getCapabilities(Context context);
    
    // Status Checks (Independent APIs)
    OptimizationVerificationStatus checkBatteryOptimizationStatus(Context context);
    OptimizationVerificationStatus checkAutoStartStatus(Context context);
    OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context);
    
    // Existence Checks
    boolean isActionPowerSavingAvailable(Context context);
    boolean isActionAutoStartAvailable(Context context);
    boolean isActionNotificationAvailable(Context context);
    
    // Intent Getters
    Intent getActionPowerSaving(Context context);
    Intent getActionAutoStart(Context context);
    Intent getActionNotification(Context context);
    
    // Guides
    BatteryGuide getPowerSavingGuide(Context context);
    BatteryGuide getAutoStartGuide(Context context);
    
    String getExtraDebugInformations(Context context);

    Intent getActionDozeMode(Context context);
    boolean isActionDozeModeNotNecessary(Context context);

    @DrawableRes int getHelpImagePowerSaving();
    @DrawableRes int getHelpImageAutoStart();
    @DrawableRes int getHelpImageNotification();
}
