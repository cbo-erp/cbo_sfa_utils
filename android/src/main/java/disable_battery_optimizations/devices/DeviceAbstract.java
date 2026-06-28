package disable_battery_optimizations.devices;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.DrawableRes;

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
            LogUtils.d(this.getClass().getSimpleName(), "✅ Doze mode check: UNRESTRICTED (already ignored)");
            return OptimizationVerificationStatus.UNRESTRICTED;
        }

        if (PrefUtils.hasKey(context, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d(this.getClass().getSimpleName(), "✅ Doze mode check: UNRESTRICTED (user confirmed)");
                return OptimizationVerificationStatus.UNRESTRICTED;
            }
        }

        LogUtils.d(this.getClass().getSimpleName(), "❌ Doze mode check: RESTRICTED");
        return OptimizationVerificationStatus.RESTRICTED;
    }

    @Override
    public OptimizationVerificationStatus checkAutoStartStatus(Context context) {
        if (!isActionAutoStartAvailable(context)) {
            LogUtils.d(this.getClass().getSimpleName(), "ℹ️ Auto-start check: NOT_SUPPORTED");
            return OptimizationVerificationStatus.NOT_SUPPORTED;
        }

        // Check user preference
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, false)) {
                LogUtils.d(this.getClass().getSimpleName(), "✅ Auto-start check: UNRESTRICTED (user confirmed)");
                return OptimizationVerificationStatus.UNRESTRICTED;
            }
        }

        // Auto-start is available but not confirmed - user needs to configure
        LogUtils.d(this.getClass().getSimpleName(), "❓ Auto-start check: UNKNOWN (requires user confirmation)");
        return OptimizationVerificationStatus.UNKNOWN;
    }

    @Override
    public OptimizationVerificationStatus checkBackgroundRestrictionStatus(Context context) {
        // Android 9+ (API 28): Use official API for definitive answer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API 28
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                if (!am.isBackgroundRestricted()) {
                    LogUtils.d(this.getClass().getSimpleName(), "✅ Background restriction check: UNRESTRICTED (system confirms)");
                    return OptimizationVerificationStatus.UNRESTRICTED;
                } else {
                    LogUtils.d(this.getClass().getSimpleName(), "❌ Background restriction check: RESTRICTED (system confirms)");
                    return OptimizationVerificationStatus.RESTRICTED;
                }
            }
        }

        // Fallback: Check user preference
        if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            if ((boolean) PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false)) {
                LogUtils.d(this.getClass().getSimpleName(), "✅ Background restriction check: UNRESTRICTED (user confirmed)");
                return OptimizationVerificationStatus.UNRESTRICTED;
            }
        }

        // System API couldn't determine + user hasn't confirmed yet
        LogUtils.d(this.getClass().getSimpleName(), "❓ Background restriction check: UNKNOWN (requires user confirmation)");
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
