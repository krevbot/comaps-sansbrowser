
package app.organicmaps;

import android.content.Intent;
import android.view.View;
import androidx.annotation.NonNull;
import app.organicmaps.base.OnBackPressListener;
import app.organicmaps.util.UiUtils;

public abstract class WebContainerDelegate implements OnBackPressListener
{
  /**
   * Browser-free build.
   *
   * Embedded web content is intentionally disabled.
   * This class must not create or load a WebView,
   * execute JavaScript, or launch external URLs.
   */
  public WebContainerDelegate(@NonNull View frame, @NonNull String url)
  {
    // Hide any WebView already defined in the layout.
    View webView = frame.findViewById(R.id.webview);
    if (webView != null)
      UiUtils.hide(webView);

    // Hide the loading indicator.
    View progress = frame.findViewById(R.id.progress);
    if (progress != null)
      UiUtils.hide(progress);
  }

  @Override
  public boolean onBackPressed()
  {
    // No browsing history exists.
    return false;
  }

  /**
   * Retained for compatibility with existing subclasses.
   * No activity is launched by this delegate.
   */
  protected abstract void doStartActivity(Intent intent);
}
