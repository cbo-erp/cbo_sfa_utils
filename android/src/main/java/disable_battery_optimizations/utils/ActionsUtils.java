package disable_battery_optimizations.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ActionsUtils {

    public static Intent createIntent() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static String getExtrasDebugInformations(Intent intent) {
        StringBuilder stringBuilder = new StringBuilder();
        if (intent != null) {
            stringBuilder.append("intent actions: ").append(intent.getAction());
            stringBuilder.append(", intent component: ");
            ComponentName componentName = intent.getComponent();
            if (componentName != null) {
                stringBuilder.append("package: ").append(componentName.getPackageName());
                stringBuilder.append(", class: ").append(componentName.getClassName());
            } else {
                stringBuilder.append("null");
            }
        } else {
            stringBuilder.append("intent is null");
        }
        return stringBuilder.toString();
    }

    public static boolean isIntentAvailable(@NonNull Context ctx, @NonNull String actionIntent) {
        return isIntentAvailable(ctx, ActionsUtils.createIntent().setAction(actionIntent));
    }

    public static boolean isIntentAvailable(@NonNull Context ctx, @NonNull ComponentName componentName) {
        return isIntentAvailable(ctx, ActionsUtils.createIntent().setComponent(componentName));
    }

    public static boolean isIntentAvailable(@NonNull Context ctx, @Nullable Intent intent) {
        if (intent == null) return false;
        final PackageManager mgr = ctx.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return  !list.isEmpty();
    }

    @Nullable
    public static Intent firstAvailableIntent(@NonNull Context context, List<Intent> intents) {
        for (Intent intent : intents) {
            if (isIntentAvailable(context, intent)) {
                return intent;
            }
        }
        return null;
    }

    @Nullable
    public static ComponentName firstAvailableComponent(@NonNull Context context, List<ComponentName> components) {
        for (ComponentName component : components) {
            if (isIntentAvailable(context, component)) {
                return component;
            }
        }
        return null;
    }

    @NonNull
    public static Intent openApplicationInfo(@NonNull Context context) {
        Intent intent = createIntent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    @NonNull
    public static Intent openBatterySettings() {
        Intent intent = createIntent();
        intent.setAction(Intent.ACTION_POWER_USAGE_SUMMARY);
        return intent;
    }
}
