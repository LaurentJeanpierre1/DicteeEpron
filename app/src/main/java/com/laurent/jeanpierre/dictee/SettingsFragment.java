package com.laurent.jeanpierre.dictee;

import android.app.Application;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * the Preferences Fragement
 */
public class SettingsFragment extends PreferenceFragment {

  public SettingsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);
    MultiSelectListPreference sounds = (MultiSelectListPreference) findPreference(getString(R.string.soundsTitle));
    String[] tab = new String[WordsDatabase.all_letters_array.size()];
    sounds.setEntries(WordsDatabase.all_letters_array.toArray(tab));
    sounds.setEntryValues(tab);

    // prepare initial summary
    Set<String> ss = PreferenceManager.getDefaultSharedPreferences(getActivity())
        .getStringSet(getString(R.string.soundsTitle),null);
    if (ss != null) {
      StringBuilder sb = new StringBuilder();
      for(String v : ss) {
        if (sb.length() != 0)
          sb.append(", ");
        sb.append(v);
      }
      sounds.setSummary(sb.toString());
    }
  }

}
