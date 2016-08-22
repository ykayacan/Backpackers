package com.yoloo.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

/**
 * Wrapper for Android Preferences which provides a fluent interface.
 *
 * @author Evgeny Shishkin
 */
public class PrefHelper {

    private static PrefHelper singleton = null;

    private final SharedPreferences mPreferences;

    private PrefHelper(Builder builder) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(builder.mContext);
    }

    /**
     * The global default {@link PrefHelper} instance.
     */
    public static PrefHelper with(Context context) {
        if (singleton == null) {
            synchronized (PrefHelper.class) {
                if (singleton == null) {
                    singleton = new Builder(context).build();
                }
            }
        }
        return singleton;
    }

    /**
     * Retrieve all values from the mPreferences.
     * <p>Note that you <em>must not</em> modify the collection returned
     * by this method, or alter any of its contents.  The consistency of your
     * stored data is not guaranteed if you do.
     *
     * @return Returns a map containing a list of pairs key/value representing the mPreferences.
     * @throws NullPointerException
     */
    public Map<String, ?> getAll() {
        return mPreferences.getAll();
    }

    /**
     * Retrieve a String value from the mPreferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not a String.
     * @throws ClassCastException
     */
    public String getString(String key, String defValue) {
        return mPreferences.getString(key, defValue);
    }

    /**
     * Retrieve a set of String values from the mPreferences.
     * <p>Note that you <em>must not</em> modify the set instance returned
     * by this call.  The consistency of the stored data is not guaranteed
     * if you do, nor is your ability to modify the instance at all.
     *
     * @param key       The name of the preference to retrieve.
     * @param defValues Values to return if this preference does not exist.
     * @return Returns the preference values if they exist, or defValues.
     * Throws ClassCastException if there is a preference with this name that is not a Set.
     * @throws ClassCastException
     */
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return mPreferences.getStringSet(key, defValues);
    }

    /**
     * Retrieve an int value from the mPreferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not an int.
     * @throws ClassCastException
     */
    public int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }

    /**
     * Retrieve a long value from the mPreferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not a long.
     * @throws ClassCastException
     */
    public long getLong(String key, long defValue) {
        return mPreferences.getLong(key, defValue);
    }

    /**
     * Retrieve a float value from the mPreferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not a float.
     * @throws ClassCastException
     */
    public float getFloat(String key, float defValue) {
        return mPreferences.getFloat(key, defValue);
    }

    /**
     * Retrieve a boolean value from the mPreferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not a boolean.
     * @throws ClassCastException
     */
    public boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

    /**
     * Checks whether the mPreferences contains a preference.
     *
     * @param key The name of the preference to check.
     * @return Returns true if the preference exists in the mPreferences,
     * otherwise false.
     */
    public boolean contains(String key) {
        return mPreferences.contains(key);
    }

    /**
     * Create a new Editor for these mPreferences, through which you can make
     * modifications to the data in the mPreferences and atomically commit those
     * changes back to the SharedPreferences object.
     * <p>Note that you <em>must</em> call {@link SharedPreferences.Editor#apply} to have any
     * changes you perform in the Editor actually show up in the SharedPreferences.
     *
     * @return Returns a new instance of the {@link SharedPreferences.Editor} interface, allowing
     * you to modify the values in this SharedPreferences object.
     */
    public SharedPreferences.Editor edit() {
        return mPreferences.edit();
    }

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     *
     * @param listener The callback that will run.
     * @see #unregisterOnSharedPreferenceChangeListener
     */
    void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregisters a previous callback.
     *
     * @param listener The callback that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Fluent API for creating {@link PrefHelper} instances.
     */
    private static class Builder {
        private final Context mContext;

        Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            mContext = context.getApplicationContext();
        }

        /**
         * Create the {@link PrefHelper} instance.
         */
        public PrefHelper build() {
            return new PrefHelper(this);
        }
    }

}
