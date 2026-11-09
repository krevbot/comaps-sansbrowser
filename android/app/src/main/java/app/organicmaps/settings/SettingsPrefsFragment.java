package app.organicmaps.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import app.organicmaps.BuildConfig;
import app.organicmaps.R;
import app.organicmaps.help.HelpActivity;

/**
 * SansBrowser:
 *
 * Hide OpenStreetMap account settings and prevent
 * navigation to the disabled account/profile screens.
 *
 * Preserve all other application settings.
 */
public class SettingsPrefsFragment extends BaseXmlSettingsFragment
{
  @Override
  protected int getXmlResources()
  {
    return R.xml.prefs_main;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    // SansBrowser: remove OpenStreetMap profile access.
    hideOsmProfilePreference();
  }

  @Override
  public void onResume()
  {
    super.onResume();

    hideOsmProfilePreference();
    updateAboutSummary();
  }

  @Override
  public boolean onPreferenceTreeClick(@NonNull Preference preference)
  {
    final String key = preference.getKey();

    // SansBrowser: block OpenStreetMap profile navigation.
    if (key != null && key.equals(getString(R.string.pref_osm_profile)))
    {
      return true;
    }

    if (key != null && key.equals(getString(R.string.pref_about)))
    {
      startActivity(new Intent(requireActivity(), HelpActivity.class));
    }

    return super.onPreferenceTreeClick(preference);
  }

  /**
   * SansBrowser:
   *
   * Hide the OpenStreetMap profile preference.
   * Authentication requires browser access and is disabled.
   */
  private void hideOsmProfilePreference()
  {
    final Preference pref = getPreference(getString(R.string.pref_osm_profile));

    if (pref != null)
    {
      pref.setVisible(false);
      pref.setEnabled(false);
      pref.setIntent(null);
      pref.setOnPreferenceClickListener(null);
    }
  }

  /**
   * Keep the About screen and version information working.
   */
  private void updateAboutSummary()
  {
    final Preference pref = getPreference(getString(R.string.pref_about));

    if (pref != null)
    {
      pref.setSummary(
          getString(R.string.pref_about_summary, BuildConfig.VERSION_NAME));
    }
  }
}
