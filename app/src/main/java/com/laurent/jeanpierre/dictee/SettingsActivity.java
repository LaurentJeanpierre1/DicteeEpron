package com.laurent.jeanpierre.dictee;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    if (p instanceof MultiSelectListPreference) {
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