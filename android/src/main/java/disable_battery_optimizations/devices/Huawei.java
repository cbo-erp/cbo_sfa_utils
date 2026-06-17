package disable_battery_optimizations.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cbo.sfa_utils.R;

import java.util.Arrays;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class Huawei extends DeviceAbstract {

    private static final String HUAWEI_ACTION_POWERSAVING = "huawei.intent.action.HSM_PROTECTED_APPS";
    private static final String HUAWEI_ACTION_AUTOSTART = "huawei.intent.action.HSM_BOOTAPP_MANAGER";
    private static final String HUAWEI_ACTION_NOTIFICATION = "huawei.intent.action.NOTIFICATIONMANAGER";

    private static final String[] HUAWEI_AUTOSTART_COMPONENTS = {
            "com.huawei.systemmanager/com.huawei.systemmanager.optimize.process.ProtectActivity",
            "com.huawei.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            "com.huawei.systemmanager/com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity",
            "com.huawei.systemmanager/com.huawei.permissionmanager.ui.MainActivity"
    };

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("honor");
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.HUAWEI;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setCanVerifyDoze(false)
                .setRequiresManualConfirmation(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setAction(HUAWEI_ACTION_POWERSAVING),
                ActionsUtils.openApplicationInfo(context)
        ));
        return intent;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return ActionsUtils.firstAvailableIntent(context, Arrays.asList(
                ActionsUtils.createIntent().setAction(HUAWEI_ACTION_AUTOSTART),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[0])),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[1])),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[2])),
                ActionsUtils.createIntent().setComponent(android.content.ComponentName.unflattenFromString(HUAWEI_AUTOSTART_COMPONENTS[3]))
        ));
    }

    @Override
    public Intent getActionNotification(Context context) {
        Intent intent = ActionsUtils.createIntent().setAction(HUAWEI_ACTION_NOTIFICATION);
        if (ActionsUtils.isIntentAvailable(context, intent)) {
            return intent;
        }
        return null;
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Huawei Battery Settings",
                "Ensure the app is 'Protected' or set to 'Manage manually'.",
                Arrays.asList(
                        "Go to Battery -> App launch",
                        "Find our app and disable 'Manage automatically'",
                        "Ensure 'Auto-launch', 'Secondary launch', and 'Run in background' are enabled"
                ),
                R.drawable.huawei_powersaving,
                null,
                null
        );
    }

    @Override
    public BatteryGuide getAutoStartGuide(Context context) {
        return new BatteryGuide(
                "Huawei Auto Start",
                "Allow the app to start automatically.",
                Arrays.asList(
                        "Go to Phone Manager -> Cleanup",
                        "Select 'Settings' and ensure 'Auto-cleanup' is not killing the app"
                ),
                R.drawable.huawei_autostart,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Huawei/Honor Model: " + Build.MODEL;
    }

    @Override
    public int getHelpImagePowerSaving() {
        return R.drawable.huawei_powersaving;
    }

    @Override
    public int getHelpImageAutoStart() {
        return R.drawable.huawei_autostart;
    }
}
