package disable_battery_optimizations.managers;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import disable_battery_optimizations.devices.DeviceBase;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.LogUtils;

/**
 * Legacy Manager updated to bridge with the new DeviceRegistry.
 */
public class KillerManager {

    public enum Actions {
        ACTION_AUTOSTART("ACTION_AUTOSTART"),
        ACTION_NOTIFICATIONS("ACTION_NOTIFICATIONS"),
        ACTION_POWERSAVING("ACTION_POWERSAVING");

        private final String mValue;
        Actions(String value) { this.mValue = value; }
        @NonNull
        public String toString() { return this.mValue; }
    }

    private static DeviceBase sDevice;

    public static DeviceBase getDevice() {
        return sDevice;
    }

    public static void init(Context context) {
        sDevice = DeviceRegistry.findCurrentDevice(context);
    }

    public static boolean isActionAvailable(Context context, Actions action) {
        init(context);
        if (sDevice == null) return false;
        switch (action) {
            case ACTION_AUTOSTART: return sDevice.isActionAutoStartAvailable(context);
            case ACTION_POWERSAVING: return sDevice.isActionPowerSavingAvailable(context);
            case ACTION_NOTIFICATIONS: return sDevice.isActionNotificationAvailable(context);
            default: return false;
        }
    }

    @Nullable
    private static Intent getIntentFromAction(Context context, Actions action) {
        init(context);
        if (sDevice == null) return null;
        switch (action) {
            case ACTION_AUTOSTART: return sDevice.getActionAutoStart(context);
            case ACTION_POWERSAVING: return sDevice.getActionPowerSaving(context);
            case ACTION_NOTIFICATIONS: return sDevice.getActionNotification(context);
            default: return null;
        }
    }

    public static boolean doAction(Context context, Actions action) {
        try {
            Intent intent = getIntentFromAction(context, action);
            if ( ActionsUtils.isIntentAvailable(context, intent)) {
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            LogUtils.e("KillerManager", "Action failed: " + e.getMessage());
        }
        return false;
    }

    public static void doActionAutoStart(Context context) { doAction(context, Actions.ACTION_AUTOSTART); }
    public static void doActionNotification(Context context) { doAction(context, Actions.ACTION_NOTIFICATIONS); }
    public static void doActionPowerSaving(Context context) { doAction(context, Actions.ACTION_POWERSAVING); }
}
