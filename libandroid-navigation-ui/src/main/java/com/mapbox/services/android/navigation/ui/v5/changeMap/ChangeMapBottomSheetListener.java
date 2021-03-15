package com.mapbox.services.android.navigation.ui.v5.changeMap;


public interface ChangeMapBottomSheetListener {
    void onMapSelected(ChangeMapItem changeMapItem);

    void onChangeMapDismissed();
}
