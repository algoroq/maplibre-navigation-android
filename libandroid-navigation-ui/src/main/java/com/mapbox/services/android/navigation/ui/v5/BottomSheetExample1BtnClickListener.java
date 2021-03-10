package com.mapbox.services.android.navigation.ui.v5;

import android.view.View;

class BottomSheetExample1BtnClickListener implements View.OnClickListener {

  private NavigationPresenter presenter;

  BottomSheetExample1BtnClickListener(NavigationPresenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onClick(View view) {
    presenter.onBottomSheetExample1ButtonClick();
  }
}
