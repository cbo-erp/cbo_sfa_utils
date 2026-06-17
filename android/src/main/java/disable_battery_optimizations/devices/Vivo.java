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

public class Vivo extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.VIVO;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setCanVerifyDoze(false) // Vivo verification is unreliable
                .setRequiresManualConfirmation(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(VivoConstants.POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(VivoConstants.POWER_SAVE[1]),
                ActionsUtils.createIntent().setComponent(VivoConstants.POWER_SAVE[2]),
                ActionsUtils.openApplicationInfo(context)
        ));
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(VivoConstants.AUTOSTART[0]),
                ActionsUtils.createIntent().setComponent(VivoConstants.AUTOSTART[1]),
                ActionsUtils.createIntent().setComponent(VivoConstants.AUTOSTART[2]),
                ActionsUtils.createIntent().setComponent(VivoConstants.AUTOSTART[3]),
                ActionsUtils.createIntent().setComponent(VivoConstants.AUTOSTART[4]),
                ActionsUtils.createIntent().setComponent(VivoConstants.AUTOSTART[5])
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Vivo Battery Management",
                "Enable high background power usage and disable restrictions.",
                Arrays.asList(
                        "Go to Battery settings",
                        "Select 'Background Power Consumption Management'",
                        "Find our app and select 'High Background Power Consumption'"
                ),
                R.drawable.vivo_power_save,
                null,
                "Settings might vary across Funtouch OS versions."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Vivo Auto Start",
                "Allow the app to start automatically.",
                Arrays.asList(
                        "Go to Settings -> More Settings -> Applications",
                        "Select 'Autostart'",
                        "Toggle the switch for our app"
                ),
                R.drawable.vivo_auto_start,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Vivo Model: " + Build.MODEL + " ROM: " + Build.DISPLAY;
    }

    @Override
    public int getHelpImagePowerSaving() {
        return R.drawable.vivo_power_save;
    }

    @Override
    public int getHelpImageAutoStart() {
        return R.drawable.vivo_auto_start;
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }
}
