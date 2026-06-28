package disable_battery_optimizations.models;

public enum OptimizationVerificationStatus {
    UNRESTRICTED,   // App is NOT restricted (verified by system or user)
    RESTRICTED,     // App IS restricted by system
    NOT_SUPPORTED,  // Feature unavailable on this device
    UNKNOWN;        // Status couldn't be determined

    public boolean isBackgroundRestrictionDisabled() {
        return this == UNRESTRICTED || this == NOT_SUPPORTED;
    }

    public boolean isAutoStartEnabled() {
        return this == UNRESTRICTED || this == NOT_SUPPORTED;
    }

}
