package disable_battery_optimizations.devices;

import android.app.ActivityManager;
import android.content.ComponentName;
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

public class Oppo extends DeviceAbstract {

    private static final ComponentName[] AUTOSTART = {
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            new ComponentName("com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity"),
            new ComponentName("com.oplus.safecenter", "com.oplus.safecenter.permission.startup.StartupAppListActivity"),
            new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"),
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.syspro.WorkModeActivity")
    };

    private static final ComponentName[] POWER_SAVE = {
            new ComponentName("com.coloros.powermanager", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"),
            new ComponentName("com.coloros.powermanager", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"),
            new ComponentName("com.oplus.battery", "com.oplus.battery.PowerConsumptionActivity"),
            new ComponentName("com.oplus.battery", "com.oplus.battery.PowerUsageModelActivity"),
            new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")
    };

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
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context) {
        // Oppo/Realme: ColorOS battery optimization cannot be verified reliably
        // Trust user preference entirely

        // Try reflection for Android 12+ as fallback only
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    Method getLevelMethod = ActivityManager.class.getMethod("getBackgroundRestrictionLevel");
                    int level = (int) getLevelMethod.invoke(am);

                    final int RESTRICTION_LEVEL_RESTRICTED = 50;

                    if (level >= RESTRICTION_LEVEL_RESTRICTED) {
                        LogUtils.d("Oppo", "❌ Background restriction check: FAILED (level=" + level + ")");
                        return OptimizationVerificationStatus.FAILED;
                    }
                }
            } catch (Exception e) {
                LogUtils.e("Oppo", "Reflection failed for getBackgroundRestrictionLevel: " + e.getMessage());
            }
        }

        // Oppo: Always trust user preference (ColorOS is unpredictable)
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d("Oppo", "✅ Background restriction check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }

        // ColorOS requires user confirmation - can't verify automatically
        LogUtils.d("Oppo", "❓ Background restriction check: UNKNOWN (ColorOS requires user confirmation)");
        return OptimizationVerificationStatus.UNKNOWN;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[1]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[2]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[3]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[4])
        ));
        return intent != null ? intent : ActionsUtils.openApplicationInfo(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(AUTOSTART[0]),
                ActionsUtils.createIntent().setComponent(AUTOSTART[1]),
                ActionsUtils.createIntent().setComponent(AUTOSTART[2]),
                ActionsUtils.createIntent().setComponent(AUTOSTART[3]),
                ActionsUtils.createIntent().setComponent(AUTOSTART[4]),
                ActionsUtils.createIntent().setComponent(AUTOSTART[5])
        ));
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "Oppo/Realme Background Settings",
                "Ensure " + appName + " can run in the background without interruptions.",
                Arrays.asList(
                        "1. Open App Info for '" + appName + "'",
                        "2. Tap on 'Battery usage'",
                        "3. Enable 'Allow background activity'"
                ),
                0,
                null,
                "Note: On some versions, you may also need to disable 'Optimize battery use' in Battery settings."
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "Oppo/Realme Auto Launch",
                "Allow the app to start automatically.",
                Arrays.asList(
                        "1. Go to Settings -> App management",
                        "2. Tap on 'Auto-launch apps'",
                        "3. Toggle '" + appName + "' to ON"
                ),
                0,
                null,
                null
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
