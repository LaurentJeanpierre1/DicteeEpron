package com.laurent.jeanpierre.dictee;


import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import java.util.Set;

/**
 * A Preferences Activity
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
  SettingsFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      getFragmentManager().beginTransaction().replace(android.R.id.content, frag=new SettingsFragment()).commit();

      checkValues();

      ActionBar actionBar = getActionBar();
      if (actionBar != null)
        actionBar.setDisplayHomeAsUpEnabled(true); // Displays up button in action bar
    }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) { // receives up-button notification
      NavUtils.navigateUpFromSameTask(this); // util to navigate back to parent task
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void checkValues()
    {
      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

  @Override
  protected void onResume() {
    super.onResume();
    // Set up a listener whenever a key changes
    PreferenceManager.getDefaultSharedPreferences(getBaseContext())
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // Unregister the listener whenever a key changes
    PreferenceManager.getDefaultSharedPreferences(getBaseContext())
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    Set<String> ss = sharedPreferences.getStringSet(s,null);
    Preference p = frag.findPreference(s);
    if (p instanceof MultiSelectListPreference && ss != null) {
      //EditTextPreference editTextPref = (EditTextPreference) p;
      //p.setSummary(editTextPref.getText());
      StringBuilder sb = new StringBuilder();
      for(String v : ss) {
        if (sb.length() != 0)
          sb.append(", ");
        sb.append(v);
      }
      p.setSummary(sb.toString());
    }
  }
}