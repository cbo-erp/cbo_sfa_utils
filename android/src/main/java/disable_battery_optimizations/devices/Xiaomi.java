package disable_battery_optimizations.devices;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.models.OptimizationVerificationStatus;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.LogUtils;
import disable_battery_optimizations.utils.Manufacturer;
import disable_battery_optimizations.utils.PrefKeys;
import disable_battery_optimizations.utils.PrefUtils;

public class Xiaomi extends DeviceAbstract {

    private static final ComponentName[] AUTO_START = {
            new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartMainActivity")
    };

    private static final ComponentName[] POWER_SAVE = {
            new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
            new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"),
            new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity")
    };

    private static final String ACTION_POWER_HIDE_LIST = "miui.intent.action.POWER_HIDE_MODE_APP_LIST";
    private static final String ACTION_OP_AUTO_START = "miui.intent.action.OP_AUTO_START";



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
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context) {
        // Xiaomi/HyperOS: PowerKeeper is very aggressive, verification via reflection is unreliable
        // Use conservative approach: trust user preference above all

        if (Build.VERSION.SDK_INT >= 31) {
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    Method getLevelMethod = ActivityManager.class.getMethod("getBackgroundRestrictionLevel");
                    int level = (int) getLevelMethod.invoke(am);

                    final int RESTRICTION_LEVEL_RESTRICTED = 50;

                    // Only if clearly restricted, return FAILED
                    if (level >= RESTRICTION_LEVEL_RESTRICTED) {
                        LogUtils.d("Xiaomi", "❌ Background restriction check: FAILED (level=" + level + ")");
                        return OptimizationVerificationStatus.FAILED;
                    }
                }
            } catch (Exception e) {
                LogUtils.e("Xiaomi", "Reflection failed for getBackgroundRestrictionLevel: " + e.getMessage());
            }
        }

        // Xiaomi: Always trust user preference (most reliable)
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d("Xiaomi", "✅ Background restriction check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }

        // PowerKeeper requires user confirmation - can't verify automatically
        LogUtils.d("Xiaomi", "❓ Background restriction check: UNKNOWN (PowerKeeper requires user confirmation)");
        return OptimizationVerificationStatus.UNKNOWN;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[1]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[2])
        ));

        if (intent == null) {
            intent = ActionsUtils.createIntent().setAction(ACTION_POWER_HIDE_LIST);
            intent.putExtra("package_name", context.getPackageName());
            intent.putExtra("package_label", getAppName(context));
            if (!ActionsUtils.isIntentAvailable(context, intent)) {
                intent = null;
            }
        }

        return intent != null ? intent : ActionsUtils.openApplicationInfo(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(AUTO_START[0]),
                ActionsUtils.createIntent().setComponent(AUTO_START[1]),
                ActionsUtils.createIntent().setAction(ACTION_OP_AUTO_START).addCategory(Intent.CATEGORY_DEFAULT)
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "Xiaomi Battery Settings",
                "Ensure reliable background tracking on HyperOS/MIUI.",
                Arrays.asList(
                        "1. Open App Info for '" + appName + "'",
                        "2. Tap on 'Battery saver'",
                        "3. Select 'No restrictions'"
                ),
                0,
                null,
                "Note: Standard Android optimization settings are often overridden by Xiaomi's PowerKeeper."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "Xiaomi Auto Start",
                "Allow the app to start automatically.",
                Collections.singletonList(
                        "1. Toggle 'Autostart' for '" + appName + "' to ON in the App Info screen"
                ),
                0,
                null,
                null
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
