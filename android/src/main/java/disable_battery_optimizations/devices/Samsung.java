package disable_battery_optimizations.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class Samsung extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase("samsung")
                || Build.MANUFACTURER.equalsIgnoreCase("samsung");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.SAMSUNG;
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        String appName = getAppName(context);

        if (Build.VERSION.SDK_INT >= 31) { // Android 12+ (OneUI 4+)
            return new BatteryGuide(
                    "Samsung Background Settings",
                    "Allow unrestricted background usage for accurate tracking.",
                    Arrays.asList(
                            "1. Open App Info for " + appName,
                            "2. Tap Battery",
                            "3. Select Unrestricted"
                    ),
                    0,
                    null,
                    null
            );
        }

        return new BatteryGuide(
                "Samsung App Battery Settings",
                "Allow unrestricted background usage for this app.",
                Arrays.asList(
                        "1. You're now viewing the App Info page",
                        "2. Scroll down and tap Battery",
                        "3. Select Unrestricted (or Unlimited if available)",
                        "4. Confirm and return to the app"
                ),
                0,
                null,
                "If Battery option is not visible, your device may not support this setting. Return to continue."
        );
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setCanVerifyDoze(true)
                .setSupportsBackgroundRestriction(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        if (Build.VERSION.SDK_INT >= 31) { // Android 12+ (OneUI 4+)
            return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                    ActionsUtils.createIntent().setAction("com.samsung.android.sm.ACTION_BATTERY"),
                    ActionsUtils.createIntent().setAction("com.samsung.android.sm.ACTION_DEVICE_MAINTENANCE"),
                    ActionsUtils.openApplicationInfo(context)
            ));
        } else { // Pre-Android 12 (J7, older devices)
            return ActionsUtils.openApplicationInfo(context);
        }
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
        return "Samsung Model: " + Build.MODEL + " SDK: " + Build.VERSION.SDK_INT;
    }
}