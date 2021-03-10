package com.mapbox.services.android.navigation.ui.v5;

import android.view.View;

class BottomSheetExample3BtnClickListener implements View.OnClickListener {

  private NavigationPresenter presenter;

  BottomSheetExample3BtnClickListener(NavigationPresenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onClick(View view) {
    presenter.onBottomSheetExample3ButtonClick();
  }
}
