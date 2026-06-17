package disable_battery_optimizations.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;
import java.util.Collections;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class OnePlus extends DeviceAbstract {

    private static final ComponentName[] ONEPLUS_AUTO_START = {
            new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
            new ComponentName("com.oneplus.security", "com.oneplus.security.autorun.AutorunMainActivity")
    };

    private static final ComponentName[] ONEPLUS_POWER_SAVE = {
            new ComponentName("com.oneplus.security", "com.oneplus.security.highpowerapp.view.HighPowerAppActivity"),
            new ComponentName("com.oneplus.security", "com.oneplus.security.cleanbackground.view.ManageBackgroundAppListActivity")
    };

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ONEPLUS;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setCanVerifyDoze(Build.VERSION.SDK_INT < 31)
                .setRequiresManualConfirmation(Build.VERSION.SDK_INT >= 31)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(ONEPLUS_POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(ONEPLUS_POWER_SAVE[1])
        ));
        return intent != null ? intent : ActionsUtils.openApplicationInfo(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(ONEPLUS_AUTO_START[0]),
                ActionsUtils.createIntent().setComponent(ONEPLUS_AUTO_START[1])
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "OnePlus Battery Optimization",
                "Ensure the app is set to 'Don't optimize'.",
                Collections.singletonList("Go to Battery -> Battery optimization, find the app and select 'Don't optimize'"),
                0,
                null,
                null
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "OnePlus Auto Launch",
                "Allow the app to launch automatically.",
                Collections.singletonList("Go to Settings -> Apps -> App management -> [App Name] -> Battery -> Allow auto-launch"),
                0,
                null,
                null
        );
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "OnePlus Model: " + Build.MODEL + " OxygenOS: " + Build.VERSION.RELEASE;
    }
}
