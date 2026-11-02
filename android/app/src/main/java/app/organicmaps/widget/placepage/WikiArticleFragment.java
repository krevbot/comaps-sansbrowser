package app.organicmaps.widget.placepage;

import android.os.Bundle;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import app.organicmaps.R;
import app.organicmaps.base.BaseMwmFragment;
import app.organicmaps.util.WindowInsetUtils;
import androidx.core.view.ViewCompat;

import java.util.Objects;

public class WikiArticleFragment extends BaseMwmFragment
{
  public static final String EXTRA_WIKI_ARTICLE = "description";

  @NonNull
  private String mDescription;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mDescription = Objects.requireNonNull(
        requireArguments().getString(EXTRA_WIKI_ARTICLE));
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    View root = inflater.inflate(
        R.layout.fragment_place_description,
        container,
        false);

    TextView articleView = root.findViewById(R.id.wiki_article_text);

    // Convert stored HTML into native Android text.
    Spanned parsed = HtmlCompat.fromHtml(
        mDescription,
        HtmlCompat.FROM_HTML_MODE_COMPACT);

    // Remove URL and clickable spans, including links
    // embedded in locally stored article content.
    android.text.SpannableStringBuilder safeText =
        new android.text.SpannableStringBuilder(parsed);

    ClickableSpan[] clickableSpans =
        safeText.getSpans(0, safeText.length(), ClickableSpan.class);

    for (ClickableSpan span : clickableSpans)
      safeText.removeSpan(span);

    articleView.setText(safeText);

    // No LinkMovementMethod is installed.
    // Text is readable but cannot launch websites.

    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        WindowInsetUtils.PaddingInsetsListener.excludeTop());

    return root;
  }
}
