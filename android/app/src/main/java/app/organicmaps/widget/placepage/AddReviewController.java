package app.organicmaps.widget.placepage;

import static android.view.View.GONE;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.FragmentManager;

import app.organicmaps.sdk.bookmarks.data.FeatureId;

/**
 * Browser-free build:
 *
 * Disables "Add review" entry points because they depend on opening
 * external review URLs/apps.
 */
public final class AddReviewController
{
  @NonNull
  private final View mView;

  public AddReviewController(@NonNull View view, @NonNull FragmentManager fragmentManager)
  {
    mView = view;
    mView.setVisibility(GONE);
  }

  public void updateView(@Nullable String reviewEditorAppName, @NonNull FeatureId featureId)
  {
    // Intentionally disabled in browser-free build.
    mView.setVisibility(GONE);
  }
