package app.organicmaps.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import app.organicmaps.R;
import app.organicmaps.base.BaseMwmToolbarFragment;
import app.organicmaps.util.WindowInsetUtils.ScrollableContentInsetsListener;

import com.google.android.material.button.MaterialButton;

/**
 * SansBrowser:
 *
 * OpenStreetMap login is disabled because authentication
 * requires an external browser.
 *
 * This fragment remains available to preserve compatibility
 * with existing navigation and activity references.
 *
 * No browser, OAuth, or external URI is launched.
 */
public class OsmLoginFragment extends BaseMwmToolbarFragment
{
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_osm_login, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view,
                            @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    getToolbarController().setTitle(R.string.login);

    // SansBrowser: hide browser-based authentication controls.
    MaterialButton loginButton = view.findViewById(R.id.login);
    MaterialButton registerButton = view.findViewById(R.id.register);

    loginButton.setVisibility(View.GONE);
    registerButton.setVisibility(View.GONE);

    // Preserve existing screen inset handling.
    ScrollView scrollView = view.findViewById(R.id.scrollView);

    ViewCompat.setOnApplyWindowInsetsListener(
        scrollView,
        new ScrollableContentInsetsListener(scrollView)
    );
  }
}
