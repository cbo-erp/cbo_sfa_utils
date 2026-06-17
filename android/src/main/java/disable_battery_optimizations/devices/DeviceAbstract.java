package disable_battery_optimizations.devices;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.DrawableRes;

import java.lang.reflect.Method;
import java.util.Collections;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.models.OptimizationVerificationStatus;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.LogUtils;
import disable_battery_optimizations.utils.PrefKeys;
import disable_battery_optimizations.utils.PrefUtils;

public abstract class DeviceAbstract implements DeviceBase {

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                .setCanVerifyDoze(true)
                .setRequiresManualConfirmation(false)
                .build();
    }

    @Override
    public OptimizationVerificationStatus checkBatteryOptimizationStatus(Context context) {
        if (isActionDozeModeNotNecessary(context)) {
            return OptimizationVerificationStatus.VERIFIED;
        }
        if (PrefUtils.hasKey(context, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }
        return OptimizationVerificationStatus.FAILED;
    }

    @Override
    public OptimizationVerificationStatus checkAutoStartStatus(Context context) {
        if (!isActionAutoStartAvailable(context)) {
            return OptimizationVerificationStatus.NOT_SUPPORTED;
        }
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, false)) {
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }
        return OptimizationVerificationStatus.UNKNOWN;
    }

    @Override
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context) {
        // Android 12+ (API 31) check using reflection to avoid compile issues in some environments
        if (Build.VERSION.SDK_INT >= 31) { // Build.VERSION_CODES.S
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    Method getLevelMethod = ActivityManager.class.getMethod("getBackgroundRestrictionLevel");
                    int level = (int) getLevelMethod.invoke(am);
                    
                    // RESTRICTION_LEVEL_RESTRICTED (50) means the app is restricted from running in background
                    // RESTRICTION_LEVEL_ADAPTIVE (20) or better means it's generally allowed
                    if (level >= 50) {
                        return OptimizationVerificationStatus.FAILED;
                    } else if (level <= 20) {
                        return OptimizationVerificationStatus.VERIFIED;
                    }
                }
            } catch (Exception e) {
                LogUtils.e("DeviceAbstract", "Reflection failed for getBackgroundRestrictionLevel: " + e.getMessage());
            }
        }

        if (!isActionPowerSavingAvailable(context)) {
            return OptimizationVerificationStatus.NOT_SUPPORTED;
        }

        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }
        return OptimizationVerificationStatus.UNKNOWN;
    }

    @Override
    public boolean isActionPowerSavingAvailable(Context context) {
        return getActionPowerSaving(context) != null;
    }

    @Override
    public boolean isActionAutoStartAvailable(Context context) {
        return getActionAutoStart(context) != null;
    }

    @Override
    public boolean isActionNotificationAvailable(Context context) {
        return false;
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Background Performance",
                "Enable 'Unrestricted' background usage to ensure long-running tasks complete successfully.",
                Collections.singletonList("Select 'Unrestricted' or 'No restrictions' in the battery settings"),
                getHelpImagePowerSaving(),
                null,
                null
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return null;
    }

    @Override
    @DrawableRes
    public int getHelpImageAutoStart() {
        return 0;
    }

    @Override
    @DrawableRes
    public int getHelpImageNotification() {
        return 0;
    }

    @DrawableRes
    @Override
    public int getHelpImagePowerSaving() {
        return 0;
    }

    @Override
    public boolean isActionDozeModeNotNecessary(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                return pm.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        }
        return false;
    }

    @Override
    public Intent getActionDozeMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isActionDozeModeNotNecessary(context)) {
                Intent dozeIntent = ActionsUtils.createIntent();
                dozeIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                return dozeIntent;
            } else {
                LogUtils.i(this.getClass().getName(), "getActionDozeMode: App already ignoring battery optimization");
            }
        }
        return null;
    }
}
