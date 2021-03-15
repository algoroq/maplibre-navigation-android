package com.mapbox.services.android.navigation.ui.v5;

import android.location.Location;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

public interface NavigationContract {

  interface View {

    boolean isRestored();

    void setSummaryBehaviorState(int state);

    void setSummaryBehaviorHideable(boolean isHideable);

    boolean isSummaryBottomSheetHidden();

    boolean isSummaryBottomSheetCollapsed();

    boolean isSummaryBottomSheetExpanded();

    void setBottomSheetChangerBtnResource(int resource);

    void updateWaynameVisibility(boolean isVisible);

    void updateCameraTrackingEnabled(boolean isEnabled);

    void resetCameraPosition();

    void showRecenterBtn();

    void hideRecenterBtn();

    void drawRoute(DirectionsRoute directionsRoute);

    void addMarker(Point point);

    void takeScreenshot();

    void startCamera(DirectionsRoute directionsRoute);

    void resumeCamera(Location location);

    void updateNavigationMap(Location location);

    boolean isRecenterButtonVisible();

    void updateCameraRouteOverview();

    void showChangeMapBottomSheet();

  }
}
