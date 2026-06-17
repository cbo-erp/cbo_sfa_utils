package disable_battery_optimizations.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cbo.sfa_utils.R;

import java.util.Arrays;
import java.util.Collections;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class Samsung extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.SAMSUNG;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setCanVerifyDoze(true)
                .setRequiresManualConfirmation(false)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setAction(SamsungConstants.ACTION_BATTERY),
                ActionsUtils.createIntent().setComponent(SamsungConstants.BATTERY_SETTINGS[0]),
                ActionsUtils.createIntent().setComponent(SamsungConstants.BATTERY_SETTINGS[1]),
                ActionsUtils.createIntent().setComponent(SamsungConstants.BATTERY_SETTINGS[2]),
                ActionsUtils.createIntent().setComponent(SamsungConstants.BATTERY_SETTINGS[3])
        ));
        
        return intent != null ? intent : ActionsUtils.openApplicationInfo(context);
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
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Samsung Battery Optimization",
                "Ensure the app is not set to sleep or restricted.",
                Arrays.asList(
                        "Go to Battery settings",
                        "Select 'Background usage limits'",
                        "Add our app to 'Never sleeping apps'"
                ),
                R.drawable.samsung,
                null,
                "One UI versions may vary."
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Samsung Model: " + Build.MODEL + " OneUI/Android: " + Build.VERSION.RELEASE;
    }

    @Override
    public int getHelpImagePowerSaving() {
        return R.drawable.samsung;
    }
}
