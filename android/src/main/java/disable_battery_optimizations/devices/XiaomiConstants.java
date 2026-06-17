package disable_battery_optimizations.devices;

import android.content.ComponentName;

public class XiaomiConstants {
    public static final ComponentName[] AUTO_START = {
            new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartMainActivity")
    };

    public static final ComponentName[] POWER_SAVE = {
            new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
            new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"),
            new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity")
    };
    
    public static final String ACTION_POWER_HIDE_LIST = "miui.intent.action.POWER_HIDE_MODE_APP_LIST";
    public static final String ACTION_OP_AUTO_START = "miui.intent.action.OP_AUTO_START";
}
