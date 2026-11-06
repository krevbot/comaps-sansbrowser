package app.organicmaps.widget.placepage.sections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.organicmaps.R;

/**
 * SansBrowser:
 *
 * Disable the Add Review interface, including the
 * Mangrove review submission entry point.
 *
 * The fragment remains available for compatibility
 * with existing place page references.
 *
 * Existing map ratings and reviews are unaffected.
 */
public final class PlacePageAddReviewFragment extends Fragment
{
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.place_page_add_review, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view,
                            @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    // SansBrowser: hide the review submission interface.
    view.setVisibility(View.GONE);
  }
}
