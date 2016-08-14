package thjread.organise;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
    public static String readPref(Context context, String pref_id, String default_value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value;
        try {
            value = sharedPrefs.getString(pref_id, default_value);
        } catch (Exception e) {
            e.printStackTrace();
            value = default_value;
        }

        return value;
    }

    public static void writePref(Context context, String pref_id, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pref_id, value);
        editor.apply();
    }
}
