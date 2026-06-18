package disable_battery_optimizations.utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.activity.ComponentActivity;
import disable_battery_optimizations.devices.DeviceBase;
import disable_battery_optimizations.managers.DevicesManager;
import disable_battery_optimizations.managers.KillerManager;
import disable_battery_optimizations.models.OptimizationVerificationStatus;
import disable_battery_optimizations.ui.DialogKillerManagerBuilder;

public class BatteryOptimizationUtil {

    public static Intent getAppSettingsIntent(Context context) {
        return ActionsUtils.openApplicationInfo(context);
    }

    public static boolean isIgnoringBatteryOptimizations(Context context) {
        DeviceBase device = DevicesManager.getDevice(context);
        if (device != null) {
            OptimizationVerificationStatus status = device.checkBatteryOptimizationStatus(context);
            return status == OptimizationVerificationStatus.VERIFIED || status == OptimizationVerificationStatus.USER_CONFIRMED;
        }

        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        String packageName = context.getApplicationContext().getPackageName();
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return true;
        }
        return powerManager.isIgnoringBatteryOptimizations(packageName);
    }

    public static Intent getIgnoreBatteryOptimizationsIntent(Context context) {
        DeviceBase device = DevicesManager.getDevice(context);
        if (device != null) {
            Intent deviceIntent = device.getActionDozeMode(context);
            if (deviceIntent != null) {
                return deviceIntent;
            }
        }

        if (Build.VERSION.SDK_INT < 23) {
            return null;
        }
        String sb = "package:" + context.getApplicationContext().getPackageName();
        @SuppressLint("BatteryLife") Intent intent = new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse(sb));
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        return intent.resolveActivity(context.getPackageManager()) == null ? getAppSettingsIntent(context) : intent;
    }

    public static void showBatteryOptimizationDialog(final ComponentActivity context, final KillerManager.Actions action, String titleMessage,  final OnOptimizationActionCallback callback) {

        if (KillerManager.isActionAvailable(context, action)) {
            if (titleMessage == null || titleMessage.isEmpty()) {
                titleMessage = String.format("Your Device %s %s has additional battery optimization", Build.MANUFACTURER, Build.MODEL);
            }

            String finalTitleMessage = titleMessage;
            context.runOnUiThread(() -> {
                new DialogKillerManagerBuilder()
                        .setContext(context)
                        .setDontShowAgain(false)
                        .setTitleMessage(finalTitleMessage)
//                        .setPositiveMessage("Ok")
                        .setOnPositiveCallback(view -> {
                            callback.onAccepted();
                        }).setOnNegativeCallback((view) -> {
                            callback.onCanceled();
                        }).setAction(action).show();
            });
        } else {
            callback.onAccepted();
        }
    }

    public interface OnOptimizationActionCallback {
        void onAccepted();
        void onCanceled();
    }
}
