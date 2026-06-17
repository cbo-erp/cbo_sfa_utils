package disable_battery_optimizations.managers;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import java.util.List;
import disable_battery_optimizations.utils.ActionsUtils;

public class IntentResolver {
    
    public static class ResolutionResult {
        public final Intent intent;
        public final String resolutionPath;
        public final boolean success;

        public ResolutionResult(Intent intent, String resolutionPath, boolean success) {
            this.intent = intent;
            this.resolutionPath = resolutionPath;
            this.success = success;
        }
    }

    public ResolutionResult resolve(@NonNull Context context, @NonNull List<Intent> prioritizedIntents) {
        for (int i = 0; i < prioritizedIntents.size(); i++) {
            Intent intent = prioritizedIntents.get(i);
            if (ActionsUtils.isIntentAvailable(context, intent)) {
                String path = "Priority_" + i;
                if (intent.getComponent() != null) {
                    path += " (Component: " + intent.getComponent().flattenToShortString() + ")";
                } else if (intent.getAction() != null) {
                    path += " (Action: " + intent.getAction() + ")";
                }
                return new ResolutionResult(intent, path, true);
            }
        }
        
        // Final fallbacks
        Intent appInfo = ActionsUtils.openApplicationInfo(context);
        return new ResolutionResult(appInfo, "Fallback: Application Details", true);
    }
}
