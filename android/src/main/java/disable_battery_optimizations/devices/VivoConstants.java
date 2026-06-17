package disable_battery_optimizations.devices;

import android.content.ComponentName;

public class VivoConstants {
    public static final ComponentName[] AUTOSTART = {
            new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
            new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"),
            new ComponentName("com.vivo.powermanager", "com.vivo.powermanager.activity.BgStartUpManagerActivity"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity")
    };

    public static final ComponentName[] POWER_SAVE = {
            new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"),
            new ComponentName("com.vivo.powermanager", "com.vivo.powermanager.activity.PowerSavingActivity"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
    };
}
