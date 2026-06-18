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

public class Asus extends DeviceAbstract {

    private static final String ASUS_PACKAGE_MOBILEMANAGER = "com.asus.mobilemanager";
    private static final String ASUS_ACTIVITY_FUNCTION = "com.asus.mobilemanager.entry.FunctionActivity";
    private static final String ASUS_ACTIVITY_AUTOSTART = "com.asus.mobilemanager.autostart.AutoStartActivity";

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase("asus") || Build.MANUFACTURER.equalsIgnoreCase("asus");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ASUS;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setSupportsNotificationOptimization(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        return super.getActionDozeMode(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.putExtra("showNotice", true);
        intent.setComponent(new ComponentName(ASUS_PACKAGE_MOBILEMANAGER, ASUS_ACTIVITY_AUTOSTART));
        return intent;
    }

    @Override
    public Intent getActionNotification(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.putExtra("showNotice", true);
        intent.setComponent(new ComponentName(ASUS_PACKAGE_MOBILEMANAGER, ASUS_ACTIVITY_FUNCTION));
        return intent;
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Asus Battery Settings",
                "Ensure the app can run without background restrictions.",
                Arrays.asList(
                        "1. Open 'Mobile Manager'",
                        "2. Tap on 'PowerMaster'",
                        "3. Select 'Battery-saving options'",
                        "4. Disable 'Stop apps when screen is locked'"
                ),
                0, null, null
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Asus Auto-start Manager",
                "Allow the app to start automatically.",
                Arrays.asList(
                        "1. Open 'Mobile Manager'",
                        "2. Tap on 'Auto-start Manager'",
                        "3. Find 'Savera RM' and toggle it to 'Allow'"
                ),
                0, null, null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Asus Model: " + Build.MODEL;
    }

}
