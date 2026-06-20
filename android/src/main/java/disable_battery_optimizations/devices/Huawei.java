package disable_battery_optimizations.devices;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.Arrays;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.models.OptimizationVerificationStatus;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.LogUtils;
import disable_battery_optimizations.utils.Manufacturer;
import disable_battery_optimizations.utils.PrefKeys;
import disable_battery_optimizations.utils.PrefUtils;

public class Huawei extends DeviceAbstract {

    private static final String HUAWEI_ACTION_POWERSAVING = "huawei.intent.action.HSM_PROTECTED_APPS";
    private static final String HUAWEI_ACTION_AUTOSTART = "huawei.intent.action.HSM_BOOTAPP_MANAGER";
    private static final String HUAWEI_ACTION_NOTIFICATION = "huawei.intent.action.NOTIFICATIONMANAGER";

    private static final String[] HUAWEI_AUTOSTART_COMPONENTS = {
            "com.huawei.systemmanager/com.huawei.systemmanager.optimize.process.ProtectActivity",
            "com.huawei.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            "com.huawei.systemmanager/com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity",
            "com.huawei.systemmanager/com.huawei.permissionmanager.ui.MainActivity"
    };

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase("huawei") || Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.BRAND.equalsIgnoreCase("honor") || Build.MANUFACTURER.equalsIgnoreCase("honor");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.HUAWEI;
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
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context) {
        // Huawei/Honor EMUI: Background optimization management is complex across versions
        // Use conservative approach with user preference priority

        if (Build.VERSION.SDK_INT >= 31) {
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    Method getLevelMethod = ActivityManager.class.getMethod("getBackgroundRestrictionLevel");
                    int level = (int) getLevelMethod.invoke(am);

                    final int RESTRICTION_LEVEL_RESTRICTED = 50;
                    final int RESTRICTION_LEVEL_ADAPTIVE = 20;

                    if (level >= RESTRICTION_LEVEL_RESTRICTED) {
                        LogUtils.d("Huawei", "❌ Background restriction check: FAILED (level=" + level + ")");
                        return OptimizationVerificationStatus.FAILED;
                    } else if (level <= RESTRICTION_LEVEL_ADAPTIVE) {
                        LogUtils.d("Huawei", "✅ Background restriction check: VERIFIED (level=" + level + ")");
                        return OptimizationVerificationStatus.VERIFIED;
                    }
                }
            } catch (Exception e) {
                LogUtils.e("Huawei", "Reflection failed for getBackgroundRestrictionLevel: " + e.getMessage());
            }
        }

        // Huawei: Trust user preference (EMUI management is device-version dependent)
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d("Huawei", "✅ Background restriction check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }

        // Huawei power saving available but not confirmed yet
        if (getActionPowerSaving(context) != null) {
            LogUtils.d("Huawei", "❓ Background restriction check: UNKNOWN (EMUI requires user confirmation)");
            return OptimizationVerificationStatus.UNKNOWN;
        }

        LogUtils.d("Huawei", "ℹ️ Background restriction check: NOT_SUPPORTED");
        return OptimizationVerificationStatus.NOT_SUPPORTED;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setAction(HUAWEI_ACTION_POWERSAVING),
                ActionsUtils.openApplicationInfo(context)
        ));
        return intent;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setAction(HUAWEI_ACTION_AUTOSTART),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[0])),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[1])),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[2])),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[3]))
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        String appName = getAppName(context);
        return new BatteryGuide(
                "Huawei Background Settings",
                "Ensure " + appName + " remains active for offline visit tracking.",
                Arrays.asList(
                        "1. Go to Battery -> App launch",
                        "2. Find '" + appName + "' and disable 'Manage automatically'",
                        "3. Enable 'Auto-launch', 'Secondary launch', and 'Run in background'"
                ),
                0,
                null,
                "Note: On EMUI 12+, also ensure 'Performance mode' is enabled if needed."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "Huawei Auto Start",
                "Allow the app to start automatically.",
                Arrays.asList(
                        "1. Open Phone Manager -> App launch",
                        "2. Toggle '" + appName + "' to 'Manage manually'",
                        "3. Ensure all three toggles are enabled"
                ),
                0,
                null,
                null
        );
    }

    @Override
    public Intent getActionNotification(Context context) {
        Intent intent = ActionsUtils.createIntent().setAction(HUAWEI_ACTION_NOTIFICATION);
        if (ActionsUtils.isIntentAvailable(context, intent)) {
            return intent;
        }
        return null;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Huawei Model: " + Build.MODEL;
    }

}
