package app.organicmaps.intent;

import static app.organicmaps.api.Const.EXTRA_PICK_POINT;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;

import app.organicmaps.MwmActivity;
import app.organicmaps.MwmApplication;
import app.organicmaps.sdk.Framework;
import app.organicmaps.sdk.Map;
import app.organicmaps.sdk.api.ParsedRoutingData;
import app.organicmaps.sdk.api.ParsedSearchRequest;
import app.organicmaps.sdk.api.RequestType;
import app.organicmaps.sdk.api.RoutePoint;
import app.organicmaps.sdk.bookmarks.data.BookmarkManager;
import app.organicmaps.sdk.bookmarks.data.FeatureId;
import app.organicmaps.sdk.bookmarks.data.MapObject;
import app.organicmaps.sdk.routing.RoutingController;
import app.organicmaps.sdk.search.SearchEngine;
import app.organicmaps.sdk.util.StorageUtils;
import app.organicmaps.sdk.util.concurrency.ThreadPool;
import app.organicmaps.search.SearchActivity;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Factory
{
  /**
   * SansBrowser:
   *
   * Permit local document URIs for importing bookmarks,
   * tracks, and other supported map files.
   *
   * Reject HTTP, HTTPS, and other URI schemes.
   */
  private static boolean isLocalDocumentUri(Uri uri)
  {
    if (uri == null || uri.getScheme() == null)
      return false;

    String scheme = uri.getScheme().toLowerCase(Locale.ROOT);

    return "content".equals(scheme) || "file".equals(scheme);
  }

  /**
   * SansBrowser:
   *
   * Permit only supported map-navigation URI schemes.
   *
   * HTTP, HTTPS, OAuth callbacks, and arbitrary external
   * URI schemes are not permitted.
   */
  private static boolean isSupportedMapUri(Uri uri)
  {
    if (uri == null || uri.getScheme() == null)
      return false;

    String scheme = uri.getScheme().toLowerCase(Locale.ROOT);

    switch (scheme)
    {
      case "geo":
      case "ge0":
      case "cm":
      case "comaps":
      case "mapsme":
        return true;

      default:
        return false;
    }
  }

  public static boolean isStartedForApiResult(@NonNull Intent intent)
  {
    // Previously, we relied on the implicit FORWARD_RESULT_FLAG
    // to detect if the caller was waiting for a result.
    //
    // The explicit EXTRA_PICK_POINT flag is more reliable.
    //
    // https://github.com/organicmaps/organicmaps/pull/8910

    return intent.getBooleanExtra(EXTRA_PICK_POINT, false);
  }

  public static class KmzKmlProcessor implements IntentProcessor
  {
    @Override
    public boolean process(@NonNull Intent intent,
                           @NonNull MwmActivity activity)
    {
      // See KML/KMZ/KMB intent filters in the manifest.

      final List<Uri> uris;

      if (Intent.ACTION_VIEW.equals(intent.getAction()))
      {
        uris = Collections.singletonList(intent.getData());
      }
      else if (Intent.ACTION_SEND.equals(intent.getAction()))
      {
        uris = Collections.singletonList(
            IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_STREAM,
                Uri.class));
      }
      else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction()))
      {
        uris = intent.getParcelableArrayListExtra(
            Intent.EXTRA_STREAM);
      }
      else
      {
        uris = null;
      }

      if (uris == null || uris.isEmpty())
        return false;

      /*
       * SansBrowser:
       *
       * Reject the entire import if any URI is not a
       * local document.
       *
       * This prevents incoming HTTP, HTTPS, or other
       * unsupported URI schemes from reaching the
       * bookmark import process.
       */
      for (Uri uri : uris)
      {
        if (!isLocalDocumentUri(uri))
          return false;
      }

      MwmApplication app = MwmApplication.from(activity);

      final File tempDir =
          new File(StorageUtils.getTempPath(app));

      final ContentResolver resolver =
          activity.getContentResolver();

      ThreadPool.getStorage().execute(
          () -> BookmarkManager.INSTANCE.importBookmarksFiles(
              resolver,
              uris,
              tempDir));

      return false;
    }
  }

  public static class UrlProcessor implements IntentProcessor
  {
    private static final int SEARCH_IN_VIEWPORT_ZOOM = 16;

    @Override
    public boolean process(@NonNull Intent intent,
                           @NonNull MwmActivity target)
    {
      /*
       * SansBrowser:
       *
       * Process only supported map VIEW intents.
       * Never pass arbitrary web URLs to the
       * native URL parser.
       */
      if (!Intent.ACTION_VIEW.equals(intent.getAction()))
        return false;

      final Uri uri = intent.getData();

      if (!isSupportedMapUri(uri))
        return false;

      switch (Framework.nativeParseAndSetApiUrl(uri.toString()))
      {
        case RequestType.INCORRECT:
          return false;

        case RequestType.MAP:
        {
          SearchEngine.INSTANCE.cancelInteractiveSearch();

          Map.executeMapApiRequest();

          return true;
        }

        case RequestType.ROUTE:
        {
          SearchEngine.INSTANCE.cancelInteractiveSearch();

          final ParsedRoutingData data =
              Framework.nativeGetParsedRoutingData();

          RoutingController.get().setRouterType(
              data.mRouterType);

          final RoutePoint from = data.mPoints[0];
          final RoutePoint to = data.mPoints[1];

          RoutingController.get().prepare(
              MapObject.createMapObject(
                  FeatureId.EMPTY,
                  MapObject.API_POINT,
                  from.mName,
                  "",
                  from.mLat,
                  from.mLon),
              MapObject.createMapObject(
                  FeatureId.EMPTY,
                  MapObject.API_POINT,
                  to.mName,
                  "",
                  to.mLat,
                  to.mLon));

          return true;
        }

        case RequestType.SEARCH:
        {
          SearchEngine.INSTANCE.cancelInteractiveSearch();

          final ParsedSearchRequest request =
              Framework.nativeGetParsedSearchRequest();

          final double[] latlon =
              Framework.nativeGetParsedCenterLatLon();

          if (latlon != null)
          {
            Framework.nativeStopLocationFollow();

            Framework.nativeSetViewportCenter(
                latlon[0],
                latlon[1],
                SEARCH_IN_VIEWPORT_ZOOM);

            // Update the search viewport manually because
            // the drape engine does not notify subscribers
            // when the search activity is shown.
            if (!request.mIsSearchOnMap)
            {
              Framework.nativeSetSearchViewport(
                  latlon[0],
                  latlon[1],
                  SEARCH_IN_VIEWPORT_ZOOM);
            }
          }

          SearchActivity.start(
              target,
              request.mQuery,
              request.mLocale,
              request.mIsSearchOnMap);

          return true;
        }

        case RequestType.CROSSHAIR:
        {
          SearchEngine.INSTANCE.cancelInteractiveSearch();

          target.showPositionChooserForAPI(
              Framework.nativeGetParsedAppName());

          final double[] latlon =
              Framework.nativeGetParsedCenterLatLon();

          if (latlon != null)
          {
            Framework.nativeStopLocationFollow();

            Framework.nativeSetViewportCenter(
                latlon[0],
                latlon[1],
                SEARCH_IN_VIEWPORT_ZOOM);
          }

          return true;
        }

        case RequestType.OAUTH2:
        {
          /*
           * SansBrowser:
           *
           * OpenStreetMap OAuth callbacks are disabled.
           * Do not open the login activity or launch
           * any external authentication flow.
           */
          return false;
        }

        // Menu and Settings URL types are not implemented.
        case RequestType.MENU:
        case RequestType.SETTINGS:
          return false;

        default:
          return false;
      }
    }
  }
}
