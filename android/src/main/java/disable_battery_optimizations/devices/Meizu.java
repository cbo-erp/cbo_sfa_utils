package disable_battery_optimizations.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;

import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.models.DeviceCapabilities;
import disable_battery_optimizations.utils.ActionsUtils;
import disable_battery_optimizations.utils.Manufacturer;

public class Meizu extends DeviceAbstract {

    private static final String MEIZU_DEFAULT_ACTION_APPSPEC = "com.meizu.safe.security.SHOW_APPSEC";
    private static final String MEIZU_POWERSAVING_ACTION = "com.meizu.power.PowerAppKilledNotification";
    private static final String MEIZU_DEFAULT_EXTRA_PACKAGE = "packageName";
    private static final String MEIZU_DEFAULT_PACKAGE = "com.meizu.safe";
    
    private static final String[] MEIZU_COMPONENTS = {
            "com.meizu.safe.cleaner.RubbishCleanMainActivity",
            "com.meizu.safe.powerui.AppPowerManagerActivity",
            "com.meizu.safe.powerui.PowerAppPermissionActivity",
            "com.meizu.safe.permission.NotificationActivity"
    };

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.MEIZU;
    }

    @Override
    public DeviceCapabilities getCapabilities(Context context) {
        return new DeviceCapabilities.Builder()
                .setSupportsDoze(true)
                .setSupportsAutoStart(true)
                .setSupportsNotificationOptimization(true)
                .build();
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent().setAction(MEIZU_POWERSAVING_ACTION);
        if (ActionsUtils.isIntentAvailable(context, intent)) {
            return intent;
        }

        MEIZU_SECURITY_CENTER_VERSION mSecVersion = getMeizuSecVersion(context);
        intent = ActionsUtils.createIntent();
        if (mSecVersion == MEIZU_SECURITY_CENTER_VERSION.SEC_2_2) {
            intent.setClassName(MEIZU_DEFAULT_PACKAGE, MEIZU_COMPONENTS[0]);
        } else if (mSecVersion == MEIZU_SECURITY_CENTER_VERSION.SEC_3_4) {
            intent.setClassName(MEIZU_DEFAULT_PACKAGE, MEIZU_COMPONENTS[1]);
        } else if (mSecVersion == MEIZU_SECURITY_CENTER_VERSION.SEC_3_7) {
            intent.setClassName(MEIZU_DEFAULT_PACKAGE, MEIZU_COMPONENTS[2]);
        } else {
            return getDefaultSettingAction(context);
        }
        return intent;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        return getDefaultSettingAction(context);
    }

    private Intent getDefaultSettingAction(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setAction(MEIZU_DEFAULT_ACTION_APPSPEC);
        intent.putExtra(MEIZU_DEFAULT_EXTRA_PACKAGE, context.getPackageName());
        return intent;
    }

    @Override
    public Intent getActionNotification(Context context) {
        MEIZU_SECURITY_CENTER_VERSION mSecVersion = getMeizuSecVersion(context);
        Intent intent = ActionsUtils.createIntent();
        if (mSecVersion == MEIZU_SECURITY_CENTER_VERSION.SEC_3_7 || mSecVersion == MEIZU_SECURITY_CENTER_VERSION.SEC_4_1) {
            intent.setComponent(new ComponentName(MEIZU_DEFAULT_PACKAGE, MEIZU_COMPONENTS[3]));
            return intent;
        } else {
            return getDefaultSettingAction(context);
        }
    }

    @Override
    public BatteryGuide getPowerSavingGuide(Context context) {
        return new BatteryGuide(
                "Meizu Battery Settings",
                "Ensure the app is allowed to run in the background.",
                Collections.singletonList("Go to Security -> Battery -> App Power Management and allow our app"),
                0,
                null,
                null
        );
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return "Meizu Sec Version: " + getMeizuSecVersion(context);
    }

    @Override
    public int getHelpImagePowerSaving() {
        return 0;
    }

    private enum MEIZU_SECURITY_CENTER_VERSION {
        SEC_2_2, SEC_3_4, SEC_3_6, SEC_3_7, SEC_4_1
    }

    private MEIZU_SECURITY_CENTER_VERSION getMeizuSecVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(MEIZU_DEFAULT_PACKAGE, 0);
            String versionStr = info.versionName;
            if (versionStr.startsWith("2")) return MEIZU_SECURITY_CENTER_VERSION.SEC_2_2;
            if (versionStr.startsWith("3")) {
                int d = Integer.parseInt(versionStr.substring(2, 3));
                if (d <= 4) return MEIZU_SECURITY_CENTER_VERSION.SEC_3_4;
                if (d < 7) return MEIZU_SECURITY_CENTER_VERSION.SEC_3_6;
                return MEIZU_SECURITY_CENTER_VERSION.SEC_3_7;
            }
            return MEIZU_SECURITY_CENTER_VERSION.SEC_4_1;
        } catch (Exception e) {
            return MEIZU_SECURITY_CENTER_VERSION.SEC_4_1;
        }
    }
}
