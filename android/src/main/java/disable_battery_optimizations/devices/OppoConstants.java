package disable_battery_optimizations.devices;

import android.content.ComponentName;

public class OppoConstants {
    public static final ComponentName[] AUTOSTART = {
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            new ComponentName("com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity"),
            new ComponentName("com.oplus.safecenter", "com.oplus.safecenter.permission.startup.StartupAppListActivity"),
            new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"),
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.syspro.WorkModeActivity")
    };

    public static final ComponentName[] POWER_SAVE = {
            new ComponentName("com.coloros.powermanager", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"),
            new ComponentName("com.coloros.powermanager", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"),
            new ComponentName("com.oplus.battery", "com.oplus.battery.PowerConsumptionActivity"),
            new ComponentName("com.oplus.battery", "com.oplus.battery.PowerUsageModelActivity"),
            new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")
    };
}
