package is.biosyningar;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class Prefs
{
    private static final String TAG = "Pref";
    private static Prefs mSingleton = null;
    private static SharedPreferences.Editor mEditor;
    static SharedPreferences mPreferences;

    @SuppressWarnings("all")
    Prefs(Context context)
    {
        mPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public static Prefs with(Context context)
    {
        if (mSingleton == null)
        {
            mSingleton = new Builder(context).build();
        }

        return mSingleton;
    }

    public void Save(String key, int value)
    {
        mEditor.putInt(key, value).apply();
    }

    public void Save(String key, String value)
    {
        mEditor.putString(key, value).apply();
    }

    public void Save(String key, Set<String> value)
    {
        mEditor.putStringSet(key, value).apply();
    }

    public void Remove(String value)
    {
        mEditor.remove(value).apply();
    }

    public String GetString(String key, String defValue)
    {
        return mPreferences.getString(key, defValue);
    }

    public Set<String> GetStringSet(String key, Set<String> defValue)
    {
        return mPreferences.getStringSet(key, defValue);
    }

    public int GetInt(String key, int defValue)
    {
        return mPreferences.getInt(key, defValue);
    }

    public void Clear()
    {
        mEditor.clear().apply();
    }

    private static class Builder
    {
        private final Context context;

        public Builder(Context context)
        {
            if (context == null)
            {
                throw new IllegalArgumentException("Context must not be null.");
            }

            this.context = context.getApplicationContext();
        }

        /**
         * Method that creates an instance of Prefs
         *
         * @return an instance of Prefs
         */
        public Prefs build() {
            return new Prefs(context);
        }
    }
}
