package disable_battery_optimizations.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Collections;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class ZTE extends DeviceAbstract {

    private static final String ZTE_HEARTYSERVICE_PACKAGE_NAME = "com.zte.heartyservice";
    private static final String ZTE_HEARTYSERVICE_AUTOSTART_ACTIVITY = "com.zte.heartyservice.autorun.AppAutoRunManager";
    private static final String ZTE_HEARTYSERVICE_POWERSAVING_ACTIVITY = "com.zte.heartyservice.setting.ClearAppSettingsActivity";

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ZTE;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(ZTE_HEARTYSERVICE_PACKAGE_NAME, ZTE_HEARTYSERVICE_POWERSAVING_ACTIVITY));
        if (ActionsUtils.isIntentAvailable(context, intent)) {
            return intent;
        }
        return super.getActionDozeMode(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(ZTE_HEARTYSERVICE_PACKAGE_NAME, ZTE_HEARTYSERVICE_AUTOSTART_ACTIVITY));
        return intent;
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "ZTE Battery Settings",
                "Ensure the app is not killed by ZTE's HeartyService.",
                Collections.singletonList("Go to Settings -> Battery -> Power management and whitelist our app"),
                0,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "ZTE Model: " + Build.MODEL;
    }
}
