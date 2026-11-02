
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
 * Browser-free build.
 *
 * Website, social-media, and other external link
 * controls are intentionally disabled.
 *
 * This fragment remains available for compatibility
 * with the existing place page architecture.
 */
public class PlacePageLinksFragment extends Fragment
{
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    View view = inflater.inflate(
        R.layout.place_page_links_fragment,
        container,
        false);

    // Hide the entire links fragment.
    view.setVisibility(View.GONE);

    return view;
  }
}
