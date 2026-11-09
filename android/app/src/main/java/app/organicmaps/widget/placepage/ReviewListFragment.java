package app.organicmaps.widget.placepage;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.textview.MaterialTextView;

import app.organicmaps.R;
import app.organicmaps.base.BaseMwmRecyclerFragment;
import app.organicmaps.sdk.bookmarks.data.Review;

import java.util.List;
import java.util.Objects;

/**
 * SansBrowser:
 *
 * Display existing reviews without allowing external
 * review submission or clickable source links.
 */
public final class ReviewListFragment extends BaseMwmRecyclerFragment<ReviewListAdapter>
{
  public static final String EXTRA_REVIEWS = "reviews";
  public static final String EXTRA_REVIEW_EDITOR_APP_NAME = "review_editor_app_name";
  public static final String EXTRA_FEATURE_ID = "feature_id";

  @CallSuper
  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    DividerItemDecoration divider =
        new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

    getRecyclerView().addItemDecoration(divider);

    handleBottomNavBar(view);
    disableReviewSourceLinks(view);
    hideAddReviewSection(view);
  }

  @LayoutRes
  @Override
  protected int getLayoutRes()
  {
    return R.layout.fragment_review_list;
  }

  @NonNull
  @Override
  protected ReviewListAdapter createAdapter()
  {
    List<Review> reviews;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    {
      reviews = Objects.requireNonNull(
          requireArguments().getParcelableArrayList(EXTRA_REVIEWS, Review.class));
    }
    else
    {
      //noinspection deprecation
      reviews = Objects.requireNonNull(
          requireArguments().getParcelableArrayList(EXTRA_REVIEWS));
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
    {
      return new ReviewListAdapter(
          reviews,
          getResources().getConfiguration().getLocales().get(0));
    }
    else
    {
      //noinspection deprecation
      return new ReviewListAdapter(
          reviews,
          getResources().getConfiguration().locale);
    }
  }

  private void handleBottomNavBar(View view)
  {
    View reviewSource = view.findViewById(R.id.review_source);

    if (reviewSource != null)
    {
      int originalPaddingBottom = reviewSource.getPaddingBottom();

      ViewCompat.setOnApplyWindowInsetsListener(reviewSource, (v, insets) -> {
        Insets navBars =
            insets.getInsets(WindowInsetsCompat.Type.systemBars());

        v.setPadding(
            v.getPaddingLeft(),
            v.getPaddingTop(),
            v.getPaddingRight(),
            originalPaddingBottom + navBars.bottom);

        return insets;
      });
    }
  }

  private void disableReviewSourceLinks(View view)
  {
    MaterialTextView reviewSource = view.findViewById(R.id.review_source);

    if (reviewSource != null)
    {
      reviewSource.setLinksClickable(false);
      reviewSource.setMovementMethod(null);
    }
  }

  private void hideAddReviewSection(View view)
  {
    View addReviewContainer = view.findViewById(R.id.add_review_container);

    if (addReviewContainer != null)
    {
      addReviewContainer.setOnClickListener(null);
      addReviewContainer.setVisibility(View.GONE);
    }
  }
}
