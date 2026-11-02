package app.organicmaps.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import app.organicmaps.R;
import app.organicmaps.base.BaseMwmFragment;
import app.organicmaps.util.WindowInsetUtils;

public class CopyrightFragment extends BaseMwmFragment
{
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    ScrollView scrollView = new ScrollView(requireContext());

    TextView textView = new TextView(requireContext());
    textView.setText(
        "Copyright and Licenses\n\n"
        + "This application is based on the open-source CoMaps project.\n\n"
        + "Map data © OpenStreetMap contributors.\n\n"
        + "This browser-free edition does not open external websites.\n\n"
        + "Full open-source license notices must remain available "
        + "in the application or its accompanying distribution.");
    textView.setTextSize(16);

    int padding = (int) (24 * getResources().getDisplayMetrics().density);
    textView.setPadding(padding, padding, padding, padding);

    scrollView.addView(textView);

    ViewCompat.setOnApplyWindowInsetsListener(
        scrollView,
        WindowInsetUtils.PaddingInsetsListener.excludeTop());

    return scrollView;
  }

  @Override
  public boolean onBackPressed()
  {
    ((HelpActivity) requireActivity()).stackFragment(
        HelpFragment.class,
        getString(R.string.about_help),
        null);

    return true;
  }
}
