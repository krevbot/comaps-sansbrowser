  private void detachCountry()
  {
    if (mStorageCallbackSlot == 0 || mCurrentCountry == null)
      return;

    MapManager.nativeUnsubscribe(mStorageCallbackSlot);
    mStorageCallbackSlot = 0;
    mCurrentCountry = null;
    mDownloaderIcon.setOnIconClickListener(null).setOnCancelClickListener(null);
    mDownloaderIcon.show(false);
    UiUtils.hide(mDownloaderInfo);
  }

  @Override
  public void onChanged(@Nullable MapObject mapObject)
  {
    if (mapObject == null)
      return;
    // Starting the download will fire this callback but the object will be the same
    // Detaching the country in that case will crash the app
    if (!mapObject.sameAs(mMapObject))
      detachCountry();
    setCurrentCountry();

    mMapObject = mapObject;

    refreshViews();
    // In case the place page has already some data, make sure to call the onPlacePageContentChanged callback
    // to catch cases where the new data has the exact same height as the previous one (eg 2 address nodes)
    if (mFrame.getHeight() > 0)
      mPlacePageViewListener.onPlacePageContentChanged(mPreview.getHeight(), mFrame.getHeight());
  }

  @Override
  public void onLocationUpdated(@NonNull Location location)
  {
    if (mMapObject == null)
      return;
    if (mMapObject.isMyPosition())
      refreshMyPosition(location);
    else
      refreshDistanceToObject(location);
  }

  @Override
  public void onCompassUpdated(double north)
  {
    if (mMapObject == null || mMapObject.isMyPosition() || mMapObject.isTrack())
      return;

    final Location location = MwmApplication.from(requireContext()).getLocationHelper().getSavedLocation();
    if (location == null)
    {
      UiUtils.hide(mAvDirection);
      return;
    }

    final double azimuth =
        Framework
            .nativeGetDistanceAndAzimuthFromLatLon(mMapObject.getLat(), mMapObject.getLon(), location.getLatitude(),
                                                   location.getLongitude(), north)
            .getAzimuth();
    UiUtils.showIf(azimuth >= 0, mAvDirection);
    if (azimuth >= 0)
    {
      mAvDirection.setAzimuth(azimuth);
    }
  }

  void shareClickListener(View v)
  {
    if (mMapObject.isTrack())
    {
      MenuBottomSheetFragment.newInstance(TRACK_SHARE_MENU_ID, getString(R.string.share_track))
          .show(getChildFragmentManager(), TRACK_SHARE_MENU_ID);
    }
    else
      SharingUtils.shareMapObject(requireContext(), mMapObject);
  }

  private void onShareTrackSelected(long trackId, KmlFileType kmlFileType)
  {
    BookmarksSharingHelper.INSTANCE.prepareTrackForSharing(requireActivity(), trackId, kmlFileType);
  }

  @Nullable
  @Override
  public ArrayList<MenuBottomSheetItem> getMenuBottomSheetItems(String id)
  {
    return switch (id)
    {
      case TRACK_SHARE_MENU_ID -> getTrackShareMenuItems();
      default -> null;
    };
  }

  public ArrayList<MenuBottomSheetItem> getTrackShareMenuItems()
  {
    Track track = (Track) mMapObject;
    ArrayList<MenuBottomSheetItem> items = new ArrayList<>();
    items.add(new MenuBottomSheetItem(R.string.export_file, R.drawable.ic_file_kmz,
                                      () -> onShareTrackSelected(track.getTrackId(), KmlFileType.Text)));
    items.add(new MenuBottomSheetItem(R.string.export_file_gpx, R.drawable.ic_file_gpx,
                                      () -> onShareTrackSelected(track.getTrackId(), KmlFileType.Gpx)));
    return items;
  }

  @Override
  public void onPreparedFileForSharing(@NonNull BookmarkSharingResult result)
  {
    BookmarksSharingHelper.INSTANCE.onPreparedFileForSharing(requireActivity(), shareLauncher, result);
  }

  public interface PlacePageViewListener
  {
    // Called when the content has actually changed and we are ready to compute the peek height
    void onPlacePageContentChanged(int previewHeight, int frameHeight);

    void onPlacePageRequestToggleState();
    void onPlacePageRequestClose();
  }
}
