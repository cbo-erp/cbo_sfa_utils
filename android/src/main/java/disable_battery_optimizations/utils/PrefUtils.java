package disable_battery_optimizations.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class PrefUtils {
    private static SharedPreferences getPreference(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);

        Context ctx = contextWeakReference.get();
        if (ctx != null) {
            String dbName = ctx.getPackageName() + "-sfa-utils";
            return ctx.getSharedPreferences(dbName, Context.MODE_PRIVATE);
        }
        return null;
    }

    /**
     * Called to save supplied value in shared preferences against given key.
     *
     * @param context Context of caller activity
     * @param key     Key of value to save against
     * @param value   Value to save
     */
    public static void saveToPrefs(Context context, String key, Object value) {
        SharedPreferences prefs = getPreference(context);
        if (prefs != null) {
            final SharedPreferences.Editor editor = prefs.edit();
            if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof String) {
                editor.putString(key, value.toString());
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Double) {
                editor.putLong(key, Double.doubleToRawLongBits((double) value));
            }
            editor.apply();
        }
    }

    /**
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned of no value found or error occurred.
     *
     * @param context      Context of caller activity
     * @param key          Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static Object getFromPrefs(Context context, String key, Object defaultValue) {
        SharedPreferences sharedPrefs = getPreference(context);
        if (sharedPrefs != null) {
            try {
                if (defaultValue instanceof String) {
                    return sharedPrefs.getString(key, defaultValue.toString());
                } else if (defaultValue instanceof Integer) {
                    return sharedPrefs.getInt(key, (Integer) defaultValue);
                } else if (defaultValue instanceof Boolean) {
                    return sharedPrefs.getBoolean(key, (Boolean) defaultValue);
                } else if (defaultValue instanceof Long) {
                    return sharedPrefs.getLong(key, (Long) defaultValue);
                } else if (defaultValue instanceof Float) {
                    return sharedPrefs.getFloat(key, (Float) defaultValue);
                } else if (defaultValue instanceof Double) {
                    return Double.longBitsToDouble(sharedPrefs.getLong(key, Double.doubleToLongBits((double) defaultValue)));
                }
            } catch (Exception e) {
                Log.e("Exception", Objects.requireNonNull(e.getMessage()));
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * @param context Context of caller activity
     * @param key     Key to delete from SharedPreferences
     */
    public static void removeFromPrefs(Context context, String key) {
        SharedPreferences sharedPrefs = getPreference(context);
        if (sharedPrefs != null) {
            final SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.remove(key);
            editor.apply();
        }
    }

    @SuppressWarnings("unused")
    public static boolean hasKey(Context context, String key) {
        SharedPreferences sharedPrefs = getPreference(context);
        if (sharedPrefs != null) {
            return sharedPrefs.contains(key);
        }
        return false;
    }

}
