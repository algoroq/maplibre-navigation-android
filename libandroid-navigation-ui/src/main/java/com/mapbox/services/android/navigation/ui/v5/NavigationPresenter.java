package com.mapbox.services.android.navigation.ui.v5;

import android.location.Location;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;

class NavigationPresenter {

  private NavigationContract.View view;
  private boolean resumeState;

  NavigationPresenter(NavigationContract.View view) {
    this.view = view;
  }

  void updateResumeState(boolean resumeState) {
    this.resumeState = resumeState;
  }

  void onRecenterClick() {
    view.setSummaryBehaviorHideable(false);
    view.setSummaryBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
    view.updateWaynameVisibility(true);
    view.resetCameraPosition();
    view.hideRecenterBtn();
  }

  void onMapMoveEnd(){
    view.updateCameraTrackingEnabled(false);
    view.updateWaynameVisibility(false);
    view.showRecenterBtn();
  }

  void onMapMoveBegin(){
    view.setSummaryBehaviorHideable(false);
    view.setSummaryBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
  }


  void onSummaryBottomSheetStateChange(){
   if(view.isSummaryBottomSheetCollapsed()){
      view.setBottomSheetChangerBtnResource(R.drawable.ic_arrow_up);
    }else if (view.isSummaryBottomSheetExpanded()){
      view.setBottomSheetChangerBtnResource(R.drawable.ic_arrow_down);
    }
  }

  void onRouteUpdate(DirectionsRoute directionsRoute) {
    view.drawRoute(directionsRoute);
    if (!resumeState) {
      view.updateWaynameVisibility(true);
      view.startCamera(directionsRoute);
    }
  }

  void onDestinationUpdate(Point point) {
    view.addMarker(point);
  }

  void onShouldRecordScreenshot() {
    view.takeScreenshot();
  }

  void onNavigationLocationUpdate(Location location) {
    if (resumeState && !view.isRecenterButtonVisible()) {
      view.resumeCamera(location);
      resumeState = false;
    }
    view.updateNavigationMap(location);
  }

  void onInstructionListVisibilityChanged(boolean visible) {
    if(view.isRestored()){
      if(view.isSummaryBottomSheetCollapsed()){
        view.setSummaryBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
        view.setSummaryBehaviorHideable(false);
      } else if(view.isSummaryBottomSheetHidden()){
        view.setSummaryBehaviorHideable(true);
        view.setSummaryBehaviorState(BottomSheetBehavior.STATE_HIDDEN);
      }else{
        view.setSummaryBehaviorState(BottomSheetBehavior.STATE_EXPANDED);
        view.setSummaryBehaviorHideable(false);
      }
    }else{
      if(visible){
        view.hideRecenterBtn();
        view.setSummaryBehaviorHideable(true);
        view.setSummaryBehaviorState(BottomSheetBehavior.STATE_HIDDEN);
      }else{
        view.setSummaryBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
        view.setSummaryBehaviorHideable(false);
      }
    }
    onSummaryBottomSheetStateChange();
  }

  void onRouteOverviewClick() {
    view.updateWaynameVisibility(false);
    view.updateCameraRouteOverview();
    view.showRecenterBtn();
  }



  void onBottomSheetChangerButtonClick(){
    if(view.isSummaryBottomSheetExpanded()){
      view.setSummaryBehaviorHideable(false);
      view.setSummaryBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
    }else{
      view.setSummaryBehaviorHideable(false);
      view.setSummaryBehaviorState(BottomSheetBehavior.STATE_EXPANDED);
    }
    onSummaryBottomSheetStateChange();
  }

  void onBottomSheetExample1ButtonClick(){}
  void onBottomSheetExample2ButtonClick(){}
  void onBottomSheetExample3ButtonClick(){}
}
