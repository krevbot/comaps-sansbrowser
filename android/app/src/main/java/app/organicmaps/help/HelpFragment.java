
package app.organicmaps.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import app.organicmaps.BuildConfig;
import app.organicmaps.R;
import app.organicmaps.base.BaseMwmFragment;
import app.organicmaps.sdk.Framework;
import app.organicmaps.sdk.util.DateUtils;
import app.organicmaps.util.WindowInsetUtils.ScrollableContentInsetsListener;
import com.google.android.material.textview.MaterialTextView;

public class HelpFragment extends BaseMwmFragment
{
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState)
  {
    View root = inflater.inflate(R.layout.about, container, false);

    MaterialTextView version = root.findViewById(R.id.version);
    version.setText(BuildConfig.VERSION_NAME);

    MaterialTextView osmPresentation =
        root.findViewById(R.id.osm_presentation);

    if (osmPresentation != null)
    {
      String dataVersion = DateUtils.getShortDateFormatter()
          .format(Framework.getDataVersion());

      osmPresentation.setText(
          getString(R.string.osm_presentation, dataVersion));
    }

    ViewCompat.setOnApplyWindowInsetsListener(
        root, new ScrollableContentInsetsListener(root));

    return root;
  }
}
