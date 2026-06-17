package disable_battery_optimizations.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cbo.sfa_utils.R;

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
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
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
        return super.getPowerSavingGuide(context);
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Asus Auto-start Manager",
                "Allow the app to start automatically.",
                Collections.singletonList("Toggle the switch for our app in the Auto-start Manager"),
                R.drawable.asus_autostart,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Asus Model: " + Build.MODEL;
    }

    @Override
    public int getHelpImageAutoStart() {
        return R.drawable.asus_autostart;
    }

    @Override
    public int getHelpImageNotification() {
        return R.drawable.asus_notification;
    }
}
