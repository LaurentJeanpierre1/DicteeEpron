package com.laurent.jeanpierre.dictee;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;

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
  }

}
