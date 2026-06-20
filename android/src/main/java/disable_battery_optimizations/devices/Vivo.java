package disable_battery_optimizations.devices;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cbo.sfa_utils.R;

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

public class Vivo extends DeviceAbstract {

    private static final ComponentName[] AUTOSTART = {
            new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
            new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"),
            new ComponentName("com.vivo.powermanager", "com.vivo.powermanager.activity.BgStartUpManagerActivity"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity")
    };

    private static final ComponentName[] POWER_SAVE = {
            new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"),
            new ComponentName("com.vivo.powermanager", "com.vivo.powermanager.activity.PowerSavingActivity"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
    };

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
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context) {
        // Vivo/Funtouch OS: Background optimization verification is explicitly unreliable
        // Rely entirely on user preference

        // Only check reflection as absolute last resort
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    Method getLevelMethod = ActivityManager.class.getMethod("getBackgroundRestrictionLevel");
                    int level = (int) getLevelMethod.invoke(am);

                    final int RESTRICTION_LEVEL_RESTRICTED = 50;

                    if (level >= RESTRICTION_LEVEL_RESTRICTED) {
                        LogUtils.d("Vivo", "❌ Background restriction check: FAILED (level=" + level + ")");
                        return OptimizationVerificationStatus.FAILED;
                    }
                }
            } catch (Exception e) {
                LogUtils.e("Vivo", "Reflection failed for getBackgroundRestrictionLevel: " + e.getMessage());
            }
        }

        // Vivo: Always trust user preference (Funtouch OS is unpredictable)
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d("Vivo", "✅ Background restriction check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }

        // Vivo power saving available but not confirmed yet
        if (getActionPowerSaving(context) != null) {
            LogUtils.d("Vivo", "❓ Background restriction check: UNKNOWN (Funtouch OS requires user confirmation)");
            return OptimizationVerificationStatus.UNKNOWN;
        }

        LogUtils.d("Vivo", "ℹ️ Background restriction check: NOT_SUPPORTED");
        return OptimizationVerificationStatus.NOT_SUPPORTED;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(POWER_SAVE[0]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[1]),
                ActionsUtils.createIntent().setComponent(POWER_SAVE[2]),
                ActionsUtils.openApplicationInfo(context)
        ));
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
                "Vivo Battery Management",
                "Enable high background power usage and disable restrictions for " + appName + ".",
                Arrays.asList(
                        "1. Go to Battery settings",
                        "2. Select 'Background Power Consumption Management'",
                        "3. Find '" + appName + "' and select 'High Background Power Consumption'"
                ),
                0,
                null,
                "Settings might vary across Funtouch OS versions."
        );
    }
    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        String appName = getAppName(context);

        return new BatteryGuide(
                "Vivo Auto Start",
                "Allow the app to start automatically.",
                Arrays.asList(
                        "1. Go to Settings -> More Settings -> Applications",
                        "2. Select 'Autostart'",
                        "3. Toggle the switch for '" + appName + "'"
                ),
                0,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Vivo Model: " + Build.MODEL + " ROM: " + Build.DISPLAY;
    }


    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }
}
