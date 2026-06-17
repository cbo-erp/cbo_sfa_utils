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

public class Xiaomi extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
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
                .setCanVerifyDoze(false) // MIUI ignores standard doze for its own settings
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
                "Set to 'No restrictions' to ensure reliable background service.",
                Collections.singletonList("Find the app, and select 'No restrictions'"),
                R.drawable.xiaomi,
                null,
                "HyperOS/MIUI may kill apps if battery saver is enabled."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Xiaomi Auto Start",
                "Allow the app to start automatically.",
                Collections.singletonList("Toggle the switch for our app in the Auto-start list"),
                R.drawable.xiaomi,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Xiaomi/HyperOS Model: " + Build.MODEL;
    }

    @Override
    public int getHelpImagePowerSaving() {
        return R.drawable.xiaomi;
    }

    @Override
    public int getHelpImageAutoStart() {
        return R.drawable.xiaomi;
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }
}
