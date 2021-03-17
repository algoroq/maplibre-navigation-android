package com.mapbox.services.android.navigation.ui.v5.summary;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.util.Pair;

import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.v5.navigation.NavigationTimeFormat;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.utils.DistanceFormatter;

import java.util.Calendar;

import static com.mapbox.services.android.navigation.v5.utils.time.TimeFormatter.formatTime;
import static com.mapbox.services.android.navigation.v5.utils.time.TimeFormatter.getTimeRemaining;

public class SummaryModel {

//  private final String distanceRemaining;
//  private final SpannableStringBuilder timeRemaining;
  private final String arrivalTime;

  private final String distanceRemainingV2;
  private final String distanceUnitsRemaining;
  private final String timeRemainingV2;
  private final String timeUnitsRemaining;

  private final String arrivalTitle;

  private final int progress;


  public SummaryModel(Context context, DistanceFormatter distanceFormatter, RouteProgress progress,
                      @NavigationTimeFormat.Type int timeFormatType) {

//    distanceRemaining = distanceFormatter.formatDistance(progress.distanceRemaining()).toString();
//    timeRemaining = formatTimeRemaining(context, progress.durationRemaining());



    if(progress.directionsRoute().distance() != null){
      this.progress = (int) (100*progress.distanceTraveled()/progress.directionsRoute().distance());
    }else{
      this.progress = 100;
    }


    Pair<String, String> remainingTime = getTimeRemaining(context, progress.durationRemaining());
    timeRemainingV2 = remainingTime.first;
    timeUnitsRemaining = remainingTime.second;

    Pair<String, String> remainingDistance = distanceFormatter.getDistanceRemaining(progress.distanceRemaining());
    distanceRemainingV2 = remainingDistance.first;
    distanceUnitsRemaining = remainingDistance.second;


    Calendar time = Calendar.getInstance();
    boolean isTwentyFourHourFormat = DateFormat.is24HourFormat(context);
    arrivalTime = formatTime(time, progress.durationRemaining(), timeFormatType, isTwentyFourHourFormat);
    arrivalTitle = context.getString(R.string.arrival);
  }

//  String getDistanceRemaining() {
//    return distanceRemaining;
//  }
//
//  SpannableStringBuilder getTimeRemaining() {
//    return timeRemaining;
//  }
//

  String getDistanceRemainingV2(){
    return distanceRemainingV2;
  }

  String getDistanceUnitsRemaining(){
    return distanceUnitsRemaining;
  }

  String getTimeRemainingV2(){
    return timeRemainingV2;
  }

  String getTimeUnitsRemaining(){
    return timeUnitsRemaining;
  }

  String getArrivalTitle(){
    return arrivalTitle;
  }

  String getArrivalTime() {
    return arrivalTime;
  }
  int getProgress(){return progress;}

}
