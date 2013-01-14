package yl.demo.pathHelper.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {
	public static final String KEY_FIRST_TIME_USE = "isFirstTime";
	public static final String KEY_HAS_NO_DB = "hasNoDb";
	public static final String KEY_SHAKE_REFRESH = "isShake2RefreshOn";
	public static final String KEY_TURN_REMAIN = "isTurnRemainOn";
	public static final String KEY_SHOW_FOOTPRINT = "isShowFootPrintOn";
	public static final String KEY_CAR_NUMBER = "carNumber";
	public static final String KEY_PARKED_PLACE = "parkedPlace";
	public static final String KEY_PARKED_TIME = "parkedTime";
	public static final String KEY_IP_ADDRESS = "ipAddress";
	public static final String KEY_PORT = "port";
	
	public static final String PREFERENCE_NAME = "setting";
	
	private static SharedPreferences preferences;
	
	public static void initPreference(Context context) {
		preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
	}
	
	public static String getStringValue(Context context, String key) {
		if(preferences == null && context != null) 
			initPreference(context);
		return preferences.getString(key, "");
	}
	
	public static boolean getBooleanValue(Context context, String key) {
		if(preferences == null && context != null) 
			initPreference(context);
		return preferences.getBoolean(key, true);
	}
	
	public static int getIntValue(Context context, String key) {
		if(preferences == null && context != null) 
			initPreference(context);
		return preferences.getInt(key, 0);
	}
	
	public static long getLongValue(Context context, String key) {
		if(preferences == null && context != null) 
			initPreference(context);
		return preferences.getLong(key, 0);
	}
	
	public static void saveStringValue(Context context, String key,String info) {
		if(preferences == null && context != null) 
			initPreference(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, info);
		editor.commit();
	}
	
	public static void saveBooleanValue(Context context, String key,Boolean info) {
		if(preferences == null && context != null) 
			initPreference(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, info);
		editor.commit();
	}
	
	public static void saveIntValue(Context context, String key, int info) {
		if(preferences == null && context != null) 
			initPreference(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, info);
		editor.commit();
	}
	
	public static void saveLongValue(Context context, String key, long value) {
		if(preferences == null && context != null) 
			initPreference(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}
}
