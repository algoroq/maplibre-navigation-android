package com.mapbox.services.android.navigation.ui.v5.summary.list;

import android.text.SpannableString;

interface InstructionListView {

  void updateManeuverViewTypeAndModifier(String maneuverType, String maneuverModifier);

  void updateManeuverViewRoundaboutDegrees(float roundaboutAngle);

  void updateDistanceText(SpannableString distanceText);

  void updatePrimaryText(String primaryText);

  void updatePrimaryMaxLines(int maxLines);



  void updateBannerVerticalBias(float bias);
}
