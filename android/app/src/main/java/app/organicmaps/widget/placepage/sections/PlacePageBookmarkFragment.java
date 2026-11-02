
package app.organicmaps.widget.placepage.sections;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import app.organicmaps.R;
import app.organicmaps.sdk.bookmarks.data.Bookmark;
import app.organicmaps.sdk.bookmarks.data.BookmarkManager;
import app.organicmaps.sdk.bookmarks.data.MapObject;
import app.organicmaps.sdk.bookmarks.data.Track;
import app.organicmaps.sdk.util.StringUtils;
import app.organicmaps.util.UiUtils;
import app.organicmaps.util.Utils;
import app.organicmaps.widget.placepage.EditBookmarkFragment;
import app.organicmaps.widget.placepage.PlacePageViewModel;
import com.google.android.material.textview.MaterialTextView;

public class PlacePageBookmarkFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener,
                                                                   Observer<MapObject>,
                                                                   EditBookmarkFragment.EditBookmarkListener
{
  private View mFrame;
  private MaterialTextView mTvBookmarkNote;

  private PlacePageViewModel mViewModel;

  private Bookmark currentBookmark;
  private Track currentTrack;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    mViewModel = new ViewModelProvider(requireActivity()).get(PlacePageViewModel.class);
    return inflater.inflate(R.layout.place_page_bookmark_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    mFrame = view;
    mTvBookmarkNote = mFrame.findViewById(R.id.tv__bookmark_notes);
    mTvBookmarkNote.setOnLongClickListener(this);

    // Browser-free build: notes must never activate links.
    mTvBookmarkNote.setAutoLinkMask(0);
    mTvBookmarkNote.setLinksClickable(false);
    mTvBookmarkNote.setMovementMethod(null);
  }

  @Override
  public void onStart()
  {
    super.onStart();
    mViewModel.getMapObject().observe(requireActivity(), this);
  }

  @Override
  public void onStop()
  {
    super.onStop();
    mViewModel.getMapObject().removeObserver(this);
  }

  private void updateBookmarkDetails()
  {
    String notes = null;

    if (currentBookmark != null)
      notes = currentBookmark.getBookmarkDescription();

    if (currentTrack != null)
      notes = currentTrack.getTrackDescription();

    if (TextUtils.isEmpty(notes))
    {
      UiUtils.hide(mTvBookmarkNote);
      return;
    }

    if (StringUtils.nativeIsHtml(notes))
    {
      // Render HTML as native text, without a WebView.
      Spanned parsed = HtmlCompat.fromHtml(
          notes,
          HtmlCompat.FROM_HTML_MODE_COMPACT);

      SpannableStringBuilder safeText =
          new SpannableStringBuilder(parsed);

      // Remove clickable links from imported HTML.
      ClickableSpan[] links =
          safeText.getSpans(0, safeText.length(), ClickableSpan.class);

      for (ClickableSpan link : links)
        safeText.removeSpan(link);

      mTvBookmarkNote.setText(safeText);
    }
    else
    {
      // Plain text stays plain text. No automatic URL linking.
      mTvBookmarkNote.setText(notes);
    }

    UiUtils.show(mTvBookmarkNote);
  }

  @Override
  public void onClick(View v)
  {
    final FragmentActivity activity = requireActivity();

    if (currentBookmark != null)
    {
      EditBookmarkFragment.editBookmark(
          currentBookmark.getCategoryId(),
          currentBookmark.getBookmarkId(),
          activity,
          getChildFragmentManager(),
          PlacePageBookmarkFragment.this);
    }
    else if (currentTrack != null)
    {
      EditBookmarkFragment.editBookmark(
          currentTrack.getCategoryId(),
          currentTrack.getTrackId(),
          activity,
          getChildFragmentManager(),
          PlacePageBookmarkFragment.this);
    }
  }

  @Override
  public boolean onLongClick(View v)
  {
    final String notes = mTvBookmarkNote.getText().toString();

    final Context ctx = requireContext();
    Utils.copyTextToClipboard(ctx, notes);

    KeyguardManager keyguardManager =
        (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        keyguardManager.isDeviceLocked())
    {
      Utils.showSnackbarAbove(
          mFrame.getRootView().findViewById(R.id.pp_buttons_layout),
          mFrame,
          ctx.getString(R.string.copied_to_clipboard, notes));
    }

    return true;
  }

  @Override
  public void onChanged(@Nullable MapObject mapObject)
  {
    if (mapObject != null && mapObject.isBookmark())
    {
      currentBookmark = (Bookmark) mapObject;
      currentTrack = null;
      updateBookmarkDetails();
    }

    if (mapObject != null && mapObject.isTrack())
    {
      currentTrack = (Track) mapObject;
      currentBookmark = null;
      updateBookmarkDetails();
    }
  }

  @Override
  public void onBookmarkSaved(long bookmarkId, boolean movedFromCategory)
  {
    Bookmark updatedBookmark =
        BookmarkManager.INSTANCE.updateBookmarkPlacePage(bookmarkId);

    if (updatedBookmark == null)
      return;

    mViewModel.setMapObject(updatedBookmark);
  }
}
