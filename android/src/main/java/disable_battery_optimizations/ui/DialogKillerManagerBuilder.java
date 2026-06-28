package disable_battery_optimizations.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.cbo.sfa_utils.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import disable_battery_optimizations.devices.DeviceBase;
import disable_battery_optimizations.managers.KillerManager;
import disable_battery_optimizations.models.BatteryGuide;
import disable_battery_optimizations.utils.KillerManagerUtils;
import disable_battery_optimizations.utils.LogUtils;

/**
 * Builds battery optimization dialogs with text-based guides.
 * 
 * Two usage modes:
 * 1. Standalone mode (default): Dialog launches the settings intent when "Open Settings" is tapped
 * 2. Callback mode (skipInternalLaunch=true): Dialog only calls callback, caller handles intent launch
 *    → Use this when integrating with ActivityResultLauncher in BatteryOptimizationHelper
 */
public class DialogKillerManagerBuilder {
    private Context mContext;
    private KillerManager.Actions mAction;
    private boolean enableDontShowAgain = false;
    private boolean skipInternalLaunch = false;
    private String titleMessage;
    private String positiveBtnStr;
    private String negativeBtnStr;
    private View.OnClickListener onPositive;
    private View.OnClickListener onNegative;

    public DialogKillerManagerBuilder() {}

    public DialogKillerManagerBuilder(Context context) {
        mContext = context;
    }

    public DialogKillerManagerBuilder setContext(Context context) {
        mContext = context;
        return this;
    }

    public DialogKillerManagerBuilder setAction(KillerManager.Actions action) {
        mAction = action;
        return this;
    }

    public DialogKillerManagerBuilder setDontShowAgain(boolean enable) {
        this.enableDontShowAgain = enable;
        return this;
    }

    /**
     * Set to true when using with ActivityResultLauncher.
     * Dialog will NOT launch the intent, only call the callback.
     * Caller is responsible for launching the intent.
     * 
     * @param skip If true, dialog won't launch intent internally
     * @return this builder for chaining
     */
    public DialogKillerManagerBuilder setSkipInternalLaunch(boolean skip) {
        this.skipInternalLaunch = skip;
        LogUtils.d("DialogKillerManagerBuilder", "skipInternalLaunch set to: " + skip);
        return this;
    }

    public DialogKillerManagerBuilder setTitleMessage(@NonNull String titleMessage) {
        this.titleMessage = titleMessage;
        return this;
    }

    public DialogKillerManagerBuilder setPositiveMessage(@NonNull String positiveMessage) {
        this.positiveBtnStr = positiveMessage;
        return this;
    }

    public DialogKillerManagerBuilder setNegativeMessage(@NonNull String negativeMessage) {
        this.negativeBtnStr = negativeMessage;
        return this;
    }

    public DialogKillerManagerBuilder setOnPositiveCallback(@NonNull View.OnClickListener onPositive) {
        this.onPositive = onPositive;
        return this;
    }

    public DialogKillerManagerBuilder setOnNegativeCallback(@NonNull View.OnClickListener onNegative) {
        this.onNegative = onNegative;
        return this;
    }

    public void show() {
        if (mContext == null || mAction == null) {
            LogUtils.e("DialogKillerManagerBuilder", "Context or Action is null, cannot show dialog");
            return;
        }

        KillerManager.init(mContext);
        DeviceBase device = KillerManager.getDevice();

        if (!KillerManager.isActionAvailable(mContext, mAction) || device == null) {
            LogUtils.d("DialogKillerManagerBuilder", "Action not available for: " + mAction + ", skipping dialog");
            if (onPositive != null) onPositive.onClick(null);
            return;
        }

        if (enableDontShowAgain && KillerManagerUtils.isDontShowAgain(mContext, mAction)) {
            LogUtils.d("DialogKillerManagerBuilder", "Don't show again is set, launching action directly");
            if (!skipInternalLaunch) {
                KillerManager.doAction(mContext, mAction);
            }
            if (onPositive != null) onPositive.onClick(null);
            return;
        }

        Context themedContext = new ContextThemeWrapper(mContext, com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(themedContext);
        
        if (positiveBtnStr == null) positiveBtnStr = "Open Settings";
        if (negativeBtnStr == null) negativeBtnStr = "Cancel";

        View customView = LayoutInflater.from(mContext).inflate(R.layout.md_dialog_custom_view, null);
        initView(customView, device);

        builder.setPositiveButton(positiveBtnStr, (dialog, which) -> {
            LogUtils.d("DialogKillerManagerBuilder", "Positive button clicked, skipInternalLaunch=" + skipInternalLaunch);
            
            // Only launch intent if NOT using ActivityResultLauncher mode
            if (!skipInternalLaunch) {
                LogUtils.d("DialogKillerManagerBuilder", "🚀 Launching action: " + mAction);
                KillerManager.doAction(mContext, mAction);
            } else {
                LogUtils.d("DialogKillerManagerBuilder", "Skipping internal launch, caller will handle intent");
            }
            
            if (onPositive != null) onPositive.onClick(customView);
        });

        builder.setNegativeButton(negativeBtnStr, (dialog, which) -> {
            LogUtils.d("DialogKillerManagerBuilder", "Negative button clicked");
            if (onNegative != null) onNegative.onClick(customView);
        });

        builder.setView(customView);
        builder.setTitle(TextUtils.isEmpty(titleMessage) ? "Optimization Guide" : titleMessage);
        builder.setCancelable(false);
        builder.show();
    }

    private void initView(View view, DeviceBase device) {
        TextView descriptionTv = view.findViewById(R.id.md_description);
        TextView stepsTv = view.findViewById(R.id.md_steps);
        TextView warningTv = view.findViewById(R.id.md_warning);
        CheckBox dontShowAgainCb = view.findViewById(R.id.md_promptCheckbox);

        BatteryGuide guide = (mAction == KillerManager.Actions.ACTION_AUTOSTART) 
                ? device.getAutoStartGuide(mContext) 
                : device.getPowerSavingGuide(mContext);

        if (guide != null) {
            descriptionTv.setText(guide.description);
            if (guide.steps != null && !guide.steps.isEmpty()) {
                stepsTv.setText(TextUtils.join("\n", guide.steps));
            } else {
                stepsTv.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(guide.warning)) {
                warningTv.setVisibility(View.VISIBLE);
                warningTv.setText(guide.warning);
            } else {
                warningTv.setVisibility(View.GONE);
            }
        }

        if (enableDontShowAgain) {
            dontShowAgainCb.setVisibility(View.VISIBLE);
            dontShowAgainCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                LogUtils.d("DialogKillerManagerBuilder", "Don't show again: " + isChecked);
                KillerManagerUtils.setDontShowAgain(mContext, mAction, isChecked);
            });
        } else {
            dontShowAgainCb.setVisibility(View.GONE);
        }
    }
}
