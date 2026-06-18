package disable_battery_optimizations.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;
import java.util.Collections;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class Xiaomi extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase("xiaomi") || Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
                Build.BRAND.equalsIgnoreCase("redmi") || Build.MANUFACTURER.equalsIgnoreCase("redmi") ||
                Build.BRAND.equalsIgnoreCase("poco") || Build.MANUFACTURER.equalsIgnoreCase("poco");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.XIAOMI;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setSupportsDeepOptimization(true)
                .setCanVerifyDoze(false)
                .setRequiresManualConfirmation(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(XiaomiConstants.POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(XiaomiConstants.POWER_SAVE[1]),
                ActionsUtils.createIntent().setComponent(XiaomiConstants.POWER_SAVE[2])
        ));

        if (intent == null) {
            intent = ActionsUtils.createIntent().setAction(XiaomiConstants.ACTION_POWER_HIDE_LIST);
            intent.putExtra("package_name", context.getPackageName());
            intent.putExtra("package_label", context.getApplicationInfo().loadLabel(context.getPackageManager()).toString());
            if (!ActionsUtils.isIntentAvailable(context, intent)) {
                intent = null;
            }
        }

        return intent != null ? intent : ActionsUtils.openApplicationInfo(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(XiaomiConstants.AUTO_START[0]),
                ActionsUtils.createIntent().setComponent(XiaomiConstants.AUTO_START[1]),
                ActionsUtils.createIntent().setAction(XiaomiConstants.ACTION_OP_AUTO_START).addCategory(Intent.CATEGORY_DEFAULT)
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Xiaomi Battery Settings",
                "Ensure reliable background tracking on HyperOS/MIUI.",
                Arrays.asList(
                        "1. Open App Info for 'Savera RM'",
                        "2. Tap on 'Battery saver'",
                        "3. Select 'No restrictions'"
                ),
                0, null,
                "Note: Standard Android optimization settings are often overridden by Xiaomi's PowerKeeper."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Xiaomi Auto Start",
                "Allow the app to start automatically.",
                Collections.singletonList("1. Toggle 'Autostart' to ON in the App Info screen"),
                0, null, null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Xiaomi/HyperOS Model: " + Build.MODEL;
    }

    @Override public int getHelpImagePowerSaving() { return 0; }
    @Override public int getHelpImageAutoStart() { return 0; }
    @Override public Intent getActionNotification(Context context) { return null; }
}
