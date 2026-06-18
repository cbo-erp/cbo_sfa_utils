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

    public String getAppName(Context context) {
        return context.getApplicationInfo()
                .loadLabel(context.getPackageManager())
                .toString();
    }
    
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
            LogUtils.d(this.getClass().getSimpleName(), "✅ Doze mode check: VERIFIED (already ignored)");
            return OptimizationVerificationStatus.VERIFIED;
        }
        
        if (PrefUtils.hasKey(context, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d(this.getClass().getSimpleName(), "✅ Doze mode check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }
        
        LogUtils.d(this.getClass().getSimpleName(), "❌ Doze mode check: FAILED");
        return OptimizationVerificationStatus.FAILED;
    }

    @Override
    public OptimizationVerificationStatus checkAutoStartStatus(Context context) {
        if (!isActionAutoStartAvailable(context)) {
            LogUtils.d(this.getClass().getSimpleName(), "ℹ️ Auto-start check: NOT_SUPPORTED");
            return OptimizationVerificationStatus.NOT_SUPPORTED;
        }
        
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, false)) {
                LogUtils.d(this.getClass().getSimpleName(), "✅ Auto-start check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }
        
        LogUtils.d(this.getClass().getSimpleName(), "❓ Auto-start check: UNKNOWN");
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
                    final int RESTRICTION_LEVEL_RESTRICTED = 50;
                    final int RESTRICTION_LEVEL_ADAPTIVE = 20;
                    
                    if (level >= RESTRICTION_LEVEL_RESTRICTED) {
                        LogUtils.d(this.getClass().getSimpleName(), "❌ Background restriction check: FAILED (level=" + level + ")");
                        return OptimizationVerificationStatus.FAILED;
                    } else if (level <= RESTRICTION_LEVEL_ADAPTIVE) {
                        LogUtils.d(this.getClass().getSimpleName(), "✅ Background restriction check: VERIFIED (level=" + level + ")");
                        return OptimizationVerificationStatus.VERIFIED;
                    }
                }
            } catch (Exception e) {
                LogUtils.e(this.getClass().getSimpleName(), "Reflection failed for getBackgroundRestrictionLevel: " + e.getMessage());
            }
        }

        if (!isActionPowerSavingAvailable(context)) {
            LogUtils.d(this.getClass().getSimpleName(), "ℹ️ Background restriction check: NOT_SUPPORTED");
            return OptimizationVerificationStatus.NOT_SUPPORTED;
        }

        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d(this.getClass().getSimpleName(), "✅ Background restriction check: USER_CONFIRMED (from prefs)");
                return OptimizationVerificationStatus.USER_CONFIRMED;
            }
        }
        
        LogUtils.d(this.getClass().getSimpleName(), "❓ Background restriction check: UNKNOWN");
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
                LogUtils.d(this.getClass().getSimpleName(), "🚀 Creating Doze mode intent");
                return dozeIntent;
            } else {
                LogUtils.i(this.getClass().getSimpleName(), "getActionDozeMode: App already ignoring battery optimization");
            }
        } else {
            LogUtils.i(this.getClass().getSimpleName(), "getActionDozeMode: Doze mode not supported (API < 23)");
        }
        return null;
    }
}
