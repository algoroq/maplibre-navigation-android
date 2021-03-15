package com.mapbox.services.android.navigation.ui.v5;

import android.view.View;

class BtnChangeMapClickListener implements View.OnClickListener {

  private NavigationPresenter presenter;

  BtnChangeMapClickListener(NavigationPresenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onClick(View view) {
    presenter.onChangeMapButtonClick();
  }
}
