package app.organicmaps.widget.placepage;

import static android.view.View.GONE;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import app.organicmaps.sdk.bookmarks.data.FeatureId;

/**
 * SansBrowser:
 *
 * Disables review submission because it depends on
 * opening external review websites or applications.
 *
 * Retains the class and public methods for compatibility.
 */
public final class AddReviewController
{
  @NonNull
  private final View mView;

  public AddReviewController(@NonNull View view,
                             @NonNull FragmentManager fragmentManager)
  {
    mView = view;
    mView.setVisibility(GONE);
  }

  public void updateView(@Nullable String reviewEditorAppName,
                         @NonNull FeatureId featureId)
  {
    // SansBrowser: review submission is disabled.
    mView.setVisibility(GONE);
  }
}
