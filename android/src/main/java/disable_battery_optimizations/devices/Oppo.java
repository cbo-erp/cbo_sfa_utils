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
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("oppo") || manufacturer.contains("realme");
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
                "Oppo/Realme Background Settings",
                "Ensure Savera RM can run in the background without interruptions.",
                Arrays.asList(
                        "1. Open App Info for 'Savera RM'",
                        "2. Tap on 'Battery usage'",
                        "3. Enable 'Allow background activity'"
                ),
                0, null,
                "Note: On some versions, you may also need to disable 'Optimize battery use' in Battery settings."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Oppo/Realme Auto Launch",
                "Allow the app to start automatically.",
                Arrays.asList(
                        "1. Go to Settings -> App management",
                        "2. Tap on 'Auto-launch apps'",
                        "3. Toggle 'Savera RM' to ON"
                ),
                0, null, null
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
