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

public class OnePlus extends DeviceAbstract {

    private static final ComponentName[] ONEPLUS_AUTO_START = {
            new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
            new ComponentName("com.oneplus.security", "com.oneplus.security.autorun.AutorunMainActivity")
    };

    private static final ComponentName[] ONEPLUS_POWER_SAVE = {
            new ComponentName("com.oneplus.security", "com.oneplus.security.highpowerapp.view.HighPowerAppActivity"),
            new ComponentName("com.oneplus.security", "com.oneplus.security.cleanbackground.view.ManageBackgroundAppListActivity")
    };

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase("oneplus") || Build.MANUFACTURER.equalsIgnoreCase("oneplus");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ONEPLUS;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setCanVerifyDoze(Build.VERSION.SDK_INT < 31)
                .setRequiresManualConfirmation(Build.VERSION.SDK_INT >= 31)
                .build();
    }

    @Override
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context) {
        // OnePlus: Handles differently on Android 12+ vs earlier versions

        if (Build.VERSION.SDK_INT >= 31) {
            // Android 12+: OnePlus battery manager unreliable, use user preference
            if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
                if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                    LogUtils.d("OnePlus", "✅ Background restriction check: USER_CONFIRMED (from prefs, Android 12+)");
                    return OptimizationVerificationStatus.USER_CONFIRMED;
                }
            }

            if (getActionPowerSaving(context) != null) {
                LogUtils.d("OnePlus", "❓ Background restriction check: UNKNOWN (Android 12+ requires manual confirmation)");
                return OptimizationVerificationStatus.UNKNOWN;
            }

            LogUtils.d("OnePlus", "ℹ️ Background restriction check: NOT_SUPPORTED");
            return OptimizationVerificationStatus.NOT_SUPPORTED;
        }

        // Pre-Android 12: Can verify via reflection
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                Method getLevelMethod = ActivityManager.class.getMethod("getBackgroundRestrictionLevel");
                int level = (int) getLevelMethod.invoke(am);

                final int RESTRICTION_LEVEL_RESTRICTED = 50;

                if (level >= RESTRICTION_LEVEL_RESTRICTED) {
                    LogUtils.d("OnePlus", "❌ Background restriction check: FAILED (level=" + level + ")");
                    return OptimizationVerificationStatus.FAILED;
                } else {
                    LogUtils.d("OnePlus", "✅ Background restriction check: VERIFIED (level=" + level + ")");
                    return OptimizationVerificationStatus.VERIFIED;
                }
            }
        } catch (Exception e) {
            LogUtils.e("OnePlus", "Reflection failed for getBackgroundRestrictionLevel: " + e.getMessage());
        }

        // Fallback to user preference
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d("OnePlus", "✅ Background restriction check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }

        // Can't verify automatically - requires user confirmation
        LogUtils.d("OnePlus", "❓ Background restriction check: UNKNOWN");
        return OptimizationVerificationStatus.UNKNOWN;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(ONEPLUS_POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(ONEPLUS_POWER_SAVE[1])
        ));
        return intent != null ? intent : ActionsUtils.openApplicationInfo(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(ONEPLUS_AUTO_START[0]),
                ActionsUtils.createIntent().setComponent(ONEPLUS_AUTO_START[1])
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "OnePlus Battery Optimization",
                "Set to 'Don't optimize' to allow continuous background tasks.",
                Arrays.asList(
                        "1. Open App Info for '" + appName + "'",
                        "2. Tap on 'Battery' or 'Battery optimization'",
                        "3. Select 'Don't optimize' or 'Unrestricted'"
                ),
                0,
                null,
                "OxygenOS may kill apps if 'Advanced Optimization' is enabled in system settings."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "OnePlus Auto Launch",
                "Allow the app to launch automatically.",
                Arrays.asList(
                        "1. Go to Settings -> Apps -> App management",
                        "2. Select '" + appName + "' -> Battery",
                        "3. Enable 'Allow auto-launch'"
                ),
                0,
                null,
                null
        );
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "OnePlus Model: " + Build.MODEL + " OxygenOS: " + Build.VERSION.RELEASE;
    }

}
