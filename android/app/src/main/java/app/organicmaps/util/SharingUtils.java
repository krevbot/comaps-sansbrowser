package app.organicmaps.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Pair;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import app.organicmaps.R;
import app.organicmaps.sdk.bookmarks.data.BookmarkInfo;
import app.organicmaps.sdk.bookmarks.data.MapObject;
import app.organicmaps.sdk.util.StorageUtils;
import app.organicmaps.sdk.util.log.Logger;

import java.io.IOException;

public class SharingUtils
{
  private static final String TAG = SharingUtils.class.getSimpleName();

  private static final String KML_MIME_TYPE = "application/vnd.google-earth.kml+xml";
  private static final String KMZ_MIME_TYPE = "application/vnd.google-earth.kmz";
  private static final String GPX_MIME_TYPE = "application/gpx+xml";
  private static final String TEXT_MIME_TYPE = "text/plain";

  public static class ShareInfo
  {
    public String mMimeType = "";
    public String mSubject = "";
    public String mText = "";
    public String mMail = "";
    public String mFileName = "";

    ShareInfo() {}

    ShareInfo(@NonNull String mimeType, String subject, String text, String mail, String fileName)
    {
      mMimeType = mimeType;
      mSubject = subject;
      mText = text;
      mMail = mail;
      mFileName = fileName;
    }
  }

  public static class SharingIntent
  {
    private final Intent mIntent;
    private Uri mSource;

    SharingIntent(@NonNull Intent intent, Uri source)
    {
      mIntent = intent;
      mSource = source;
    }

    SharingIntent(@NonNull Intent intent)
    {
      mIntent = intent;
    }

    public void SetSourceFile(@NonNull Uri source)
    {
      mSource = source;
    }

    Intent GetIntent()
    {
      return mIntent;
    }

    Uri GetSourceFile()
    {
      return mSource;
    }
  }

  public static class SharingContract
      extends ActivityResultContract<SharingIntent, Pair<Uri, Uri>>
  {
    private Uri sourceUri;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, SharingIntent input)
    {
      sourceUri = input.GetSourceFile();
      return input.GetIntent();
    }

    @Override
    public Pair<Uri, Uri> parseResult(int resultCode, Intent intent)
    {
      if (resultCode == Activity.RESULT_OK && intent != null)
      {
        Uri destinationUri = intent.getData();

        if (sourceUri != null && destinationUri != null)
          return new Pair<>(sourceUri, destinationUri);
      }

      return null;
    }
  }

  // Utility class: static methods only.
  private SharingUtils() {}

  /**
   * SansBrowser:
   * Disable sharing the current location through external apps.
   */
  public static void shareLocation(@NonNull Context context,
                                   @NonNull Location loc)
  {
    // External sharing intentionally disabled.
  }

  /**
   * SansBrowser:
   * Disable sharing map objects through external apps.
   */
  public static void shareMapObject(@NonNull Context context,
                                    @NonNull MapObject object)
  {
    // External sharing intentionally disabled.
  }

  /**
   * SansBrowser:
   * Disable sharing bookmarks through external apps.
   */
  public static void shareBookmark(@NonNull Context context,
                                   @NonNull BookmarkInfo bookmark)
  {
    // External sharing intentionally disabled.
  }

  /**
   * Copy an exported file to a location selected by the user.
   */
  private static void ProcessShareResult(
      @NonNull ContentResolver resolver,
      Pair<Uri, Uri> result)
  {
    if (result == null)
      return;

    Uri sourceUri = result.first;
    Uri destinationUri = result.second;

    if (sourceUri == null || destinationUri == null)
      return;

    try
    {
      StorageUtils.copyFile(resolver, sourceUri, destinationUri);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static ActivityResultLauncher<SharingIntent> RegisterLauncher(
      @NonNull Fragment fragment)
  {
    return fragment.registerForActivityResult(
        new SharingContract(),
        result -> ProcessShareResult(
            fragment.requireContext().getContentResolver(),
            result));
  }

  public static ActivityResultLauncher<SharingIntent> RegisterLauncher(
      @NonNull AppCompatActivity activity)
  {
    return activity.registerForActivityResult(
        new SharingContract(),
        result -> ProcessShareResult(
            activity.getContentResolver(),
            result));
  }

  /**
   * SansBrowser:
   *
   * Export files only through Android's document picker.
   *
   * No ACTION_SEND.
   * No ACTION_SEND_MULTIPLE.
   * No Intent.createChooser().
   * No external sharing applications.
   */
  public static void shareFile(
      Context context,
      ActivityResultLauncher<SharingIntent> launcher,
      ShareInfo info)
  {
    if (info == null || info.mFileName.isEmpty())
      return;

    final Uri fileUri =
        StorageUtils.getUriForFilePath(context, info.mFileName);

    Logger.i(TAG, "Exporting file " + info.mMimeType
                   + " " + info.mFileName);

    Intent saveIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    saveIntent.addCategory(Intent.CATEGORY_OPENABLE);

    String mimeType = info.mMimeType;

    if (mimeType == null || mimeType.isEmpty())
      mimeType = "application/octet-stream";

    saveIntent.setType(mimeType);

    final String fileName = info.mFileName.substring(
        info.mFileName.lastIndexOf('/') + 1);

    saveIntent.putExtra(Intent.EXTRA_TITLE, fileName);

    SharingIntent sharingIntent =
        new SharingIntent(saveIntent, fileUri);

    launcher.launch(sharingIntent);
  }

  /**
   * Export bookmark files locally without using
   * Android's external sharing interface.
   */
  public static void shareBookmarkFile(
      Context context,
      ActivityResultLauncher<SharingIntent> launcher,
      String fileName,
      String mimeType)
  {
    final String subject =
        context.getString(R.string.share_bookmarks_email_subject);

    final String text =
        context.getString(R.string.share_bookmarks_email_body);

    ShareInfo info =
        new ShareInfo(mimeType, subject, text, "", fileName);

    shareFile(context, launcher, info);
  }
}
