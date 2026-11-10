package app.organicmaps.settings;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import app.organicmaps.BuildConfig;
import app.organicmaps.R;
import app.organicmaps.help.HelpActivity;

/**
 * SansBrowser:
 *
 * OpenStreetMap account settings are disabled.
 *
 * The OpenStreetMap profile preference has already
 * been removed from prefs_main.xml, so this fragment
 * must not attempt to access it.
 *
 * All other settings remain available.
 */
public class SettingsPrefsFragment extends BaseXmlSettingsFragment
{
  @Override
  protected int getXmlResources()
  {
    return R.xml.prefs_main;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    updateAboutSummary();
  }

  @Override
  public boolean onPreferenceTreeClick(@NonNull Preference preference)
  {
    final String key = preference.getKey();

    // SansBrowser: prevent OpenStreetMap profile access.
    if (getString(R.string.pref_osm_profile).equals(key))
      return true;

    if (getString(R.string.pref_about).equals(key))
    {
      startActivity(new Intent(requireActivity(), HelpActivity.class));
      return true;
    }

    return super.onPreferenceTreeClick(preference);
  }

  /**
   * Preserve the About screen's version information.
   */
  private void updateAboutSummary()
  {
    final Preference pref =
        findPreference(getString(R.string.pref_about));

    if (pref != null)
    {
      pref.setSummary(
          getString(R.string.pref_about_summary, BuildConfig.VERSION_NAME));
    }
  }
}
