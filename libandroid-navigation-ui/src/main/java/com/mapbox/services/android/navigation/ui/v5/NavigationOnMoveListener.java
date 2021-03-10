package com.mapbox.services.android.navigation.ui.v5;

import android.support.design.widget.BottomSheetBehavior;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.mapboxsdk.maps.MapboxMap;

class NavigationOnMoveListener implements MapboxMap.OnMoveListener {
  private final NavigationPresenter navigationPresenter;
  private final BottomSheetBehavior summaryBehavior;

  NavigationOnMoveListener(NavigationPresenter navigationPresenter, BottomSheetBehavior summaryBehavior) {
    this.navigationPresenter = navigationPresenter;
    this.summaryBehavior = summaryBehavior;
  }

  @Override
  public void onMoveBegin(MoveGestureDetector detector) {
      if (summaryBehavior.getState() != BottomSheetBehavior.STATE_DRAGGING) {
        navigationPresenter.onMapMoveBegin();
    }
  }

  @Override
  public void onMove(MoveGestureDetector detector) {
    // Intentionally empty
  }

  @Override
  public void onMoveEnd(MoveGestureDetector detector) {
    if (summaryBehavior.getState() != BottomSheetBehavior.STATE_DRAGGING) {
      navigationPresenter.onMapMoveEnd();
    }
  }

}
