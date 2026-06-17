package disable_battery_optimizations.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class Oppo extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains("oppo") ||
                Build.FINGERPRINT.toLowerCase().contains("realme");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.OPPO;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setCanVerifyDoze(false)
                .setRequiresManualConfirmation(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(OppoConstants.POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(OppoConstants.POWER_SAVE[1]),
                ActionsUtils.createIntent().setComponent(OppoConstants.POWER_SAVE[2]),
                ActionsUtils.createIntent().setComponent(OppoConstants.POWER_SAVE[3]),
                ActionsUtils.createIntent().setComponent(OppoConstants.POWER_SAVE[4])
        ));
        return intent != null ? intent : ActionsUtils.openApplicationInfo(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(OppoConstants.AUTOSTART[0]),
                ActionsUtils.createIntent().setComponent(OppoConstants.AUTOSTART[1]),
                ActionsUtils.createIntent().setComponent(OppoConstants.AUTOSTART[2]),
                ActionsUtils.createIntent().setComponent(OppoConstants.AUTOSTART[3]),
                ActionsUtils.createIntent().setComponent(OppoConstants.AUTOSTART[4]),
                ActionsUtils.createIntent().setComponent(OppoConstants.AUTOSTART[5])
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Oppo/Realme Battery Settings",
                "Allow the app to run in the background without restrictions.",
                Arrays.asList(
                        "Go to Battery -> More battery settings",
                        "Select 'Optimize battery use'",
                        "Find our app and select 'Don't optimize'"
                ),
                0,
                null,
                "ColorOS/Realme UI may have additional 'App management' settings."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Oppo/Realme Auto Start",
                "Enable the app to start automatically.",
                Arrays.asList(
                        "Go to Settings -> App management",
                        "Select 'Auto-launch apps'",
                        "Enable the toggle for our app"
                ),
                0,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Oppo/Realme Model: " + Build.MODEL + " OS: " + Build.VERSION.RELEASE;
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }
}
