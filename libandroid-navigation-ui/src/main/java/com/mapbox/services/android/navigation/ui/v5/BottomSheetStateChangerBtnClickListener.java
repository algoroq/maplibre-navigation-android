package com.mapbox.services.android.navigation.ui.v5;

import android.view.View;

class BottomSheetStateChangerBtnClickListener implements View.OnClickListener {

  private NavigationPresenter presenter;

  BottomSheetStateChangerBtnClickListener(NavigationPresenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onClick(View view) {
    presenter.onBottomSheetChangerButtonClick();
  }
}
