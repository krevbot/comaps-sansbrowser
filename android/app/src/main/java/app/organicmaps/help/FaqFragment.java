package app.organicmaps.help;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import app.organicmaps.R;
import app.organicmaps.base.BaseMwmFragment;
import app.organicmaps.util.SharingUtils;
import app.organicmaps.util.Utils;
import app.organicmaps.util.WindowInsetUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FaqFragment extends BaseMwmFragment
{
  private ActivityResultLauncher<SharingUtils.SharingIntent> shareLauncher;

  private final DialogInterface.OnClickListener mDialogClickListener =
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          switch (which)
          {
            case 0 -> Utils.sendFeedback(shareLauncher, requireActivity());
            case 1 -> Utils.sendBugReport(shareLauncher, requireActivity(), "", "");
          }
        }
      };

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    View root = inflater.inflate(R.layout.fragment_prefs_faq, container, false);

    ViewCompat.setOnApplyWindowInsetsListener(
        root, WindowInsetUtils.PaddingInsetsListener.excludeTop());

    // Browser-free build: no online FAQ or embedded WebView.
    View webView = root.findViewById(R.id.webview);
    if (webView != null)
      webView.setVisibility(View.GONE);

    View progress = root.findViewById(R.id.progress);
    if (progress != null)
      progress.setVisibility(View.GONE);

    // Display a simple offline message.
    if (root instanceof ViewGroup)
    {
      TextView message = new TextView(requireContext());
      message.setText(
          "Online help is unavailable in this browser-free edition.\n\n"
          + "Offline maps, search, and navigation remain available.");
      message.setTextSize(16);
      int padding = (int) (24 * getResources().getDisplayMetrics().density);
      message.setPadding(padding, padding, padding, padding);
      ((ViewGroup) root).addView(message);
    }

    shareLauncher = SharingUtils.RegisterLauncher(this);

    FloatingActionButton feedbackFab = root.findViewById(R.id.feedback_fab);
    if (feedbackFab != null)
    {
      feedbackFab.setOnClickListener(
          v -> new MaterialAlertDialogBuilder(requireActivity())
                   .setTitle(R.string.feedback)
                   .setNegativeButton(R.string.cancel, null)
                   .setItems(
                       new CharSequence[] {
                           getString(R.string.feedback_general),
                           getString(R.string.report_a_bug)
                       },
                       mDialogClickListener)
                   .show());
    }

    return root;
  }
}
