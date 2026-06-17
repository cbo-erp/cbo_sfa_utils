package disable_battery_optimizations.models;

public class DeviceCapabilities {
    public final boolean supportsDoze;
    public final boolean supportsAutoStart;
    public final boolean supportsBackgroundRestriction;
    public final boolean supportsDeepOptimization;
    public final boolean supportsNotificationOptimization;
    public final boolean canVerifyDoze;
    public final boolean requiresManualConfirmation;
    public final boolean requiresForegroundService;
    public final boolean supportsExactAlarm;
    public final boolean supportsRestrictedSettings;

    private DeviceCapabilities(Builder builder) {
        this.supportsDoze = builder.supportsDoze;
        this.supportsAutoStart = builder.supportsAutoStart;
        this.supportsBackgroundRestriction = builder.supportsBackgroundRestriction;
        this.supportsDeepOptimization = builder.supportsDeepOptimization;
        this.supportsNotificationOptimization = builder.supportsNotificationOptimization;
        this.canVerifyDoze = builder.canVerifyDoze;
        this.requiresManualConfirmation = builder.requiresManualConfirmation;
        this.requiresForegroundService = builder.requiresForegroundService;
        this.supportsExactAlarm = builder.supportsExactAlarm;
        this.supportsRestrictedSettings = builder.supportsRestrictedSettings;
    }

    public static class Builder {
        private boolean supportsDoze = true;
        private boolean supportsAutoStart = false;
        private boolean supportsBackgroundRestriction = false;
        private boolean supportsDeepOptimization = false;
        private boolean supportsNotificationOptimization = false;
        private boolean canVerifyDoze = true;
        private boolean requiresManualConfirmation = false;
        private boolean requiresForegroundService = false;
        private boolean supportsExactAlarm = false;
        private boolean supportsRestrictedSettings = false;

        public Builder setSupportsDoze(boolean supportsDoze) { this.supportsDoze = supportsDoze; return this; }
        public Builder setSupportsAutoStart(boolean supportsAutoStart) { this.supportsAutoStart = supportsAutoStart; return this; }
        public Builder setSupportsBackgroundRestriction(boolean supportsBackgroundRestriction) { this.supportsBackgroundRestriction = supportsBackgroundRestriction; return this; }
        public Builder setSupportsDeepOptimization(boolean supportsDeepOptimization) { this.supportsDeepOptimization = supportsDeepOptimization; return this; }
        public Builder setSupportsNotificationOptimization(boolean supportsNotificationOptimization) { this.supportsNotificationOptimization = supportsNotificationOptimization; return this; }
        public Builder setCanVerifyDoze(boolean canVerifyDoze) { this.canVerifyDoze = canVerifyDoze; return this; }
        public Builder setRequiresManualConfirmation(boolean requiresManualConfirmation) { this.requiresManualConfirmation = requiresManualConfirmation; return this; }
        public Builder setRequiresForegroundService(boolean requiresForegroundService) { this.requiresForegroundService = requiresForegroundService; return this; }
        public Builder setSupportsExactAlarm(boolean supportsExactAlarm) { this.supportsExactAlarm = supportsExactAlarm; return this; }
        public Builder setSupportsRestrictedSettings(boolean supportsRestrictedSettings) { this.supportsRestrictedSettings = supportsRestrictedSettings; return this; }

        public DeviceCapabilities build() { return new DeviceCapabilities(this); }
    }
}
