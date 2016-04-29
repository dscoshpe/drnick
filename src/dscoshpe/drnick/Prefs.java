package dscoshpe.drnick;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
	// option names and default values
	private static final String OPT_LOG = "log";
	private static final boolean OPT_LOG_DEF = true;
	private static final String OPT_EXTRA = "extra";
	private static final boolean OPT_EXTRA_DEF = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
	
	// get current value of the hint option
	public static boolean getLog(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_LOG, OPT_LOG_DEF);
	}
	
	// get current value of the EXTRAess lookup option
	public static boolean getEXTRA(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_EXTRA, OPT_EXTRA_DEF);
	}

}
