package app.organicmaps.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.organicmaps.R;
import app.organicmaps.base.BaseMwmToolbarFragment;

/**
 * SansBrowser:
 *
 * OpenStreetMap account features are disabled.
 *
 * This fragment remains available so existing references
 * to ProfileActivity and ProfileFragment do not break.
 *
 * No browser links, OAuth authentication, account network
 * requests, or external activities are launched.
 */
public class ProfileFragment extends BaseMwmToolbarFragment
{
  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_osm_profile, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view,
                            @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    getToolbarController().setTitle(R.string.profile);

    // Disable the logout control.
    View logoutButton =
        getToolbarController().getToolbar().findViewById(R.id.logout);

    if (logoutButton != null)
    {
      logoutButton.setOnClickListener(null);
      logoutButton.setVisibility(View.GONE);
    }

    // Hide all external OpenStreetMap links.
    hideView(view, R.id.about_osm);
    hideView(view, R.id.osm_history);
    hideView(view, R.id.osm_notes);

    // Hide account-related controls and loading indicators.
    hideView(view, R.id.buttons_container);
    hideView(view, R.id.user_profile_loading);
    hideView(view, R.id.user_sent_edits);
    hideView(view, R.id.user_profile_image);

    // Keep a simple native explanation instead of
    // redirecting to browser-based authentication.
    View userInfoBlock = view.findViewById(R.id.block_user_info);

    if (userInfoBlock != null)
      userInfoBlock.setVisibility(View.VISIBLE);

    TextView profileName = view.findViewById(R.id.user_profile_name);

    if (profileName != null)
    {
      profileName.setText(
          "OpenStreetMap account features are disabled in CoMaps SansBrowser.");
      profileName.setLinksClickable(false);
      profileName.setMovementMethod(null);
      profileName.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Hide a view and remove its click listener.
   */
  private static void hideView(@NonNull View root, int viewId)
  {
    View target = root.findViewById(viewId);

    if (target != null)
    {
      target.setOnClickListener(null);
      target.setVisibility(View.GONE);
    }
  }
}
