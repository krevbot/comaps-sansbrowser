package app.organicmaps.editor;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import app.organicmaps.base.BaseMwmFragmentActivity;

/**
 * SansBrowser:
 *
 * OpenStreetMap browser-based authentication is disabled.
 *
 * Retain this activity and its public API for compatibility
 * with existing application references.
 */
public class OsmLoginActivity extends BaseMwmFragmentActivity
{
  public static final String EXTRA_OAUTH2CODE = "oauth2code";

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return OsmLoginFragment.class;
  }

  /**
   * SansBrowser:
   * Ignore OAuth2 callbacks instead of launching the
   * OpenStreetMap login activity.
   */
  public static void OAuth2Callback(@NonNull Activity activity, String oauth2code)
  {
    // Intentionally disabled.
  }
}
