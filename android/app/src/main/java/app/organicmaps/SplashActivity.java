package app.organicmaps;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.organicmaps.downloader.DownloaderActivity;
import app.organicmaps.intent.Factory;
import app.organicmaps.sdk.location.LocationHelper;
import app.organicmaps.sdk.util.Config;
import app.organicmaps.sdk.util.LocationUtils;
import app.organicmaps.sdk.util.concurrency.UiThread;
import app.organicmaps.sdk.util.log.Logger;
import app.organicmaps.util.SharingUtils;
import app.organicmaps.util.Utils;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity
{
  private static final String TAG = SplashActivity.class.getSimpleName();

  private static final long DELAY = 100;

  private boolean mCanceled = false;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private ActivityResultLauncher<Intent> mApiRequest;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private ActivityResultLauncher<String[]> mPermissionRequest;

  @NonNull
  private ActivityResultLauncher<SharingUtils.SharingIntent> mShareLauncher;

  @NonNull
  private final Runnable mInitCoreDelayedTask = this::init;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    UiThread.cancelDelayedTasks(mInitCoreDelayedTask);

    setContentView(R.layout.activity_splash);

    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.root_view),
        new OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              @NonNull View v,
              @NonNull WindowInsetsCompat insets)
          {
            Insets systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(0, 0, 0, systemBars.bottom);

            return insets;
          }
        });

    mPermissionRequest = registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(),
        result -> Config.setLocationRequested());

    mApiRequest = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          setResult(result.getResultCode(), result.getData());
          finish();
        });

    mShareLauncher = SharingUtils.RegisterLauncher(this);

    if (MwmApplication.from(this).getDisplayManager().isCarDisplayUsed())
    {
      startActivity(new Intent(this, MapPlaceholderActivity.class));
      finish();
    }
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    if (mCanceled)
      return;

    if (!Config.isLocationRequested()
        && !LocationUtils.checkLocationPermission(this))
    {
      Logger.d(TAG, "Requesting location permissions");

      mPermissionRequest.launch(
          new String[] {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION});

      return;
    }

    UiThread.runLater(mInitCoreDelayedTask, DELAY);
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    UiThread.cancelDelayedTasks(mInitCoreDelayedTask);
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();

    mPermissionRequest.unregister();
    mPermissionRequest = null;

    mApiRequest.unregister();
    mApiRequest = null;
  }

  private void showFatalErrorDialog(
      @StringRes int titleId,
      @StringRes int messageId,
      Exception error)
  {
    mCanceled = true;

    new MaterialAlertDialogBuilder(
        this,
        R.style.MwmTheme_M3_AlertDialog)
        .setTitle(titleId)
        .setMessage(messageId)
        .setPositiveButton(
            R.string.report_a_bug,
            (dialog, which) -> Utils.sendBugReport(
                mShareLauncher,
                this,
                "Fatal Error",
                Log.getStackTraceString(error)))
        .setCancelable(false)
        .show();
  }

  private void init()
  {
    MwmApplication app = MwmApplication.from(this);

    boolean asyncContinue = false;

    try
    {
      asyncContinue = app.initOrganicMaps(this::processNavigation);
    }
    catch (IOException error)
    {
      showFatalErrorDialog(
          R.string.dialog_error_storage_title,
          R.string.dialog_error_storage_message,
          error);

      return;
    }

    if (Config.isFirstLaunch(this)
        && LocationUtils.checkLocationPermission(this))
    {
      final LocationHelper locationHelper = app.getLocationHelper();

      locationHelper.onEnteredIntoFirstRun();

      if (!locationHelper.isActive())
        locationHelper.start();
    }

    if (!asyncContinue)
      processNavigation();
  }

  /**
   * SansBrowser:
   *
   * Only allow supported map and local-document URI schemes
   * to pass through the splash activity.
   *
   * Reject HTTP, HTTPS, intent, market, and all other
   * unsupported URI schemes.
   *
   * This protects against explicit ACTION_VIEW intents
   * even when their manifest intent filters are absent.
   */
  private static boolean isUnsupportedViewIntent(@NonNull Intent intent)
  {
    if (!Intent.ACTION_VIEW.equals(intent.getAction()))
      return false;

    Uri uri = intent.getData();

    if (uri == null)
      return false;

    String scheme = uri.getScheme();

    if (scheme == null)
      return false;

    switch (scheme.toLowerCase(java.util.Locale.ROOT))
    {
      case "geo":
      case "ge0":
      case "cm":
      case "comaps":
      case "mapsme":
      case "content":
      case "file":
      case "data":
        return false;

      default:
        return true;
    }
  }

  // Called from MwmApplication::nativeInitFramework like callback.
  @Keep
  @SuppressWarnings({"unused", "unchecked"})
  public void processNavigation()
  {
    if (isDestroyed())
    {
      Logger.w(
          TAG,
          "Ignore late callback from core because activity is already destroyed");

      return;
    }

    // Re-use original intent with the known safe subset
    // of flags to retain security permissions.
    //
    // https://github.com/organicmaps/organicmaps/issues/6944
    final Intent intent = Objects.requireNonNull(getIntent());

    /*
     * SansBrowser:
     *
     * Reject unsupported incoming URI schemes.
     *
     * Instead of forwarding the URI, open the normal
     * mapping activity with a fresh intent.
     *
     * Preserve the special Manage Space entry point.
     */
    if (!isManageSpaceActivity(intent)
        && isUnsupportedViewIntent(intent))
    {
      Logger.w(
          TAG,
          "SansBrowser: blocked unsupported external VIEW intent");

      Intent safeIntent =
          new Intent(this, DownloadResourcesLegacyActivity.class);

      safeIntent.setAction(Intent.ACTION_MAIN);

      startActivity(safeIntent);
      finish();

      return;
    }

    if (isManageSpaceActivity(intent))
    {
      intent.setComponent(
          new ComponentName(this, DownloaderActivity.class));
    }
    else
    {
      intent.setComponent(
          new ComponentName(this, DownloadResourcesLegacyActivity.class));
    }

    // FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
    // break the cold start.
    //
    // FORWARD_RESULT_FLAG conflicts with the ActivityResultLauncher.
    //
    // Preserve read permission for imported local documents.
    intent.setFlags(
        intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION);

    if (Factory.isStartedForApiResult(intent))
    {
      // Wait for the result from MwmActivity for API callers.
      mApiRequest.launch(intent);
      return;
    }

    Config.setFirstStartDialogSeen(this);

    startActivity(intent);
    finish();
  }

  private boolean isManageSpaceActivity(Intent intent)
  {
    var component = intent.getComponent();

    if (!Intent.ACTION_VIEW.equals(intent.getAction()))
      return false;

    if (component == null)
      return false;

    var manageSpaceActivityName =
        BuildConfig.APPLICATION_ID + ".ManageSpaceActivity";

    return manageSpaceActivityName.equals(component.getClassName());
  }
}
