package app.organicmaps.settings;

import androidx.annotation.Keep;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import app.organicmaps.R;
import app.organicmaps.dialog.CustomMapServerDialog;
import app.organicmaps.sdk.Framework;
import app.organicmaps.sdk.util.SharedPropertiesUtils;
import app.organicmaps.sdk.util.log.LogsManager;
import app.organicmaps.util.Utils;

/**
 * SansBrowser:
 *
 * The Open External Links settings entry point is disabled.
 *
 * Preserve logging, storage diagnostics, and custom
 * map download server configuration.
 */
@Keep
public class AdvancedSettingsFragment extends BaseXmlSettingsFragment
{
  @Override
  protected int getXmlResources()
  {
    return R.xml.prefs_advanced;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    initLoggingEnabledPrefsCallbacks();
    initEmulationBadStorage();
    initCustomMapDownloadUrlPrefsCallbacks();

    // SansBrowser:
    // Do not enable the Open External Links preference.
    // It remains hidden by default in prefs_advanced.xml.
  }

  private void initLoggingEnabledPrefsCallbacks()
  {
    final Preference pref = getPreference(getString(R.string.pref_enable_logging));

    ((TwoStatePreference) pref).setChecked(
        LogsManager.INSTANCE.isFileLoggingEnabled());

    pref.setOnPreferenceChangeListener((preference, newValue) -> {
      if (!LogsManager.INSTANCE.setFileLoggingEnabled((Boolean) newValue))
      {
        Utils.showSnackbar(requireView(), "ERROR: Can't create a logs folder!");
        return false;
      }

      return true;
    });
  }

  private void initEmulationBadStorage()
  {
    final Preference pref =
        findPreference(getString(R.string.pref_emulate_bad_external_storage));

    if (pref == null)
      return;

    if (!SharedPropertiesUtils.shouldShowEmulateBadStorageSetting())
      pref.setVisible(false);
  }

  private void initCustomMapDownloadUrlPrefsCallbacks()
  {
    Preference customUrlPref =
        getPreference(getString(R.string.pref_custom_map_download_url));

    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(requireContext());

    String current =
        prefs.getString(getString(R.string.pref_custom_map_download_url), "");

    String normalizedUrl = Framework.normalizeServerUrl(current);

    customUrlPref.setSummary(
        normalizedUrl.isEmpty()
            ? getString(R.string.download_resources_custom_url_summary_none)
            : normalizedUrl);

    Framework.applyCustomMapDownloadUrl(requireContext(), normalizedUrl);

    customUrlPref.setOnPreferenceClickListener(preference -> {
      CustomMapServerDialog.show(
          requireContext(),
          url -> preference.setSummary(
              url.isEmpty()
                  ? getString(R.string.download_resources_custom_url_summary_none)
                  : url));

      return true;
    });
  }
}
