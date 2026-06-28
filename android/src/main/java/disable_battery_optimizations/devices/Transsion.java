package disable_battery_optimizations.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Arrays;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class Transsion extends DeviceAbstract {

    @Override
    public boolean isThatRom() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("infinix") || manufacturer.contains("tecno") || manufacturer.contains("itel");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ASUS; // Using existing or adding new? For safety, we match the brand.
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(new ComponentName("com.transsion.phonemaster", "com.transsion.phonemaster.PowerMarathonActivity")),
                ActionsUtils.createIntent().setComponent(new ComponentName("com.transsion.phonemaster", "com.cl.phonemaster.main.PowerMarathonMainActivity")),
                ActionsUtils.openApplicationInfo(context)
        ));
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setComponent(new ComponentName("com.transsion.phonemaster", "com.transsion.phonemaster.autostart.AutoStartManagementActivity")),
                ActionsUtils.createIntent().setAction("com.transsion.phonemaster.action.AUTOSTART")
        ));
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Transsion (Infinix/Tecno) Model: " + Build.MODEL;
    }

    @Override
    public Intent getActionNotification(Context context) { return null; }
}
