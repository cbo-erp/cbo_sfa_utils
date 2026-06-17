package disable_battery_optimizations.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class HTC extends DeviceAbstract {

    private static final String HTC_PITROAD_PACKAGE_NAME = "com.htc.pitroad";
    private static final String HTC_PITROAD_POWERSAVING = "com.htc.pitroad.landingpage.activity.LandingPageActivity";

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.HTC;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(HTC_PITROAD_PACKAGE_NAME, HTC_PITROAD_POWERSAVING));
        if (ActionsUtils.isIntentAvailable(context, intent)) {
            return intent;
        }
        return super.getActionDozeMode(context);
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
        return "HTC Model: " + Build.MODEL;
    }
}
