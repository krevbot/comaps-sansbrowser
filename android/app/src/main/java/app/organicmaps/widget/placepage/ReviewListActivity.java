package app.organicmaps.widget.placepage;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.organicmaps.R;
import app.organicmaps.base.BaseToolbarActivity;
import app.organicmaps.sdk.bookmarks.data.FeatureId;
import app.organicmaps.sdk.bookmarks.data.Review;

import java.util.ArrayList;

/**
 * SansBrowser:
 *
 * Preserve the ability to view existing place reviews.
 * Do not pass external review-editor information to the
 * review list.
 *
 * The existing start() signature is retained for
 * compatibility with other parts of the application.
 */
public final class ReviewListActivity extends BaseToolbarActivity
{
  private static final String EXTRA_TITLE = "title";

  @Override
  protected int getToolbarTitle()
  {
    return R.string.place_reviews_title;
  }

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return ReviewListFragment.class;
  }

  public static void start(@NonNull Context context,
                           @NonNull String title,
                           @NonNull ArrayList<Review> reviews,
                           @Nullable String reviewEditorAppName,
                           @NonNull FeatureId featureId)
  {
    Intent intent = new Intent(context, ReviewListActivity.class)
        .putParcelableArrayListExtra(
            ReviewListFragment.EXTRA_REVIEWS, reviews)
        .putExtra(
            ReviewListFragment.EXTRA_REVIEW_EDITOR_APP_NAME, (String) null)
        .putExtra(
            ReviewListFragment.EXTRA_FEATURE_ID, featureId)
        .putExtra(
            EXTRA_TITLE, context.getString(R.string.place_reviews, title));

    context.startActivity(intent);
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    String toolbarTitle = getIntent().getStringExtra(EXTRA_TITLE);
    this.getToolbar().setTitle(toolbarTitle);
  }
}
