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
        return Build.BRAND.equalsIgnoreCase("samsung") || Build.MANUFACTURER.equalsIgnoreCase("samsung");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.SAMSUNG;
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Samsung Background Settings",
                "Ensure the app can run reliably for visit tracking.",
                Arrays.asList(
                        "1. Open App Info for 'Savera RM'",
                        "2. Tap on 'Battery'",
                        "3. Select 'Unrestricted'"
                ),
                0, null,
                "Note: On some models, go to 'Battery' > 'Background usage limits' > 'Never sleeping apps' and add this app."
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
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setAction("com.samsung.android.sm.ACTION_BATTERY"),
                ActionsUtils.openApplicationInfo(context)
        ));
    }

    @Override
    public Intent getActionAutoStart(Context context) { return null; }
    @Override
    public Intent getActionNotification(Context context) { return null; }
    @Override
    public String getExtraDebugInformations(Context context) { return "Samsung Model: " + Build.MODEL; }
}
