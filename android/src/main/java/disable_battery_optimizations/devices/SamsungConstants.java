package disable_battery_optimizations.devices;

import android.content.ComponentName;

public class SamsungConstants {
    public static final ComponentName[] BATTERY_SETTINGS = {
            new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
            new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.battery.BatteryActivity"),
            new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"),
            new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.dashboard.SmartManagerDashBoardActivity")
    };
    
    public static final String ACTION_BATTERY = "com.samsung.android.sm.ACTION_BATTERY";
}
