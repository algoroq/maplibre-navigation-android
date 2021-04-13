package com.mapbox.services.android.navigation.ui.v5.summary.list;

import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.view.View;

import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteLegProgress;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.utils.DistanceFormatter;
import com.mapbox.services.android.navigation.v5.utils.RouteUtils;

import java.util.ArrayList;
import java.util.List;

public class InstructionListPresenter {

    private static final int TWO_LINES = 2;
    private static final int ONE_LINE = 1;
    private static final float TWO_LINE_BIAS = 0.65f;
    private static final float ONE_LINE_BIAS = 0.5f;
    private static final int FIRST_INSTRUCTION_INDEX = 0;
    private final RouteUtils routeUtils;
    private DistanceFormatter distanceFormatter;
    private List<CustomInstructionsBanner> instructions;
    private RouteLeg currentLeg;

    InstructionListPresenter(RouteUtils routeUtils, DistanceFormatter distanceFormatter) {
        this.routeUtils = routeUtils;
        this.distanceFormatter = distanceFormatter;
        instructions = new ArrayList<>();
    }

    void onBindInstructionListViewAtPosition(int position, @NonNull InstructionListView listView) {
        CustomInstructionsBanner bannerInstructions = instructions.get(position);
        double distance = bannerInstructions.getDistance();
        SpannableString distanceText = distanceFormatter.formatDistance(distance);
        updateListView(listView, bannerInstructions, distanceText);
    }

    int retrieveBannerInstructionListSize() {
        return instructions.size();
    }

    boolean updateBannerListWith(RouteProgress routeProgress) {
        addBannerInstructions(routeProgress);
        return updateInstructionList(routeProgress);
    }

    void updateDistanceFormatter(DistanceFormatter distanceFormatter) {
        if (shouldUpdate(distanceFormatter)) {
            this.distanceFormatter = distanceFormatter;
        }
    }

    private boolean shouldUpdate(DistanceFormatter distanceFormatter) {
        return distanceFormatter != null
                && (this.distanceFormatter == null || !this.distanceFormatter.equals(distanceFormatter));
    }

    private void updateListView(@NonNull InstructionListView listView, CustomInstructionsBanner bannerInstructions,
                                SpannableString distanceText) {
        listView.updatePrimaryText(bannerInstructions.getInstruction());
        //TODO updateManeuverView(listView, bannerInstructions);
        listView.updateDistanceText(distanceText);
    }


    private void updateManeuverView(@NonNull InstructionListView listView, BannerInstructions bannerInstructions) {
        String maneuverType = bannerInstructions.primary().type();
        String maneuverModifier = bannerInstructions.primary().modifier();
        listView.updateManeuverViewTypeAndModifier(maneuverType, maneuverModifier);

        Double roundaboutDegrees = bannerInstructions.primary().degrees();
        if (roundaboutDegrees != null) {
            listView.updateManeuverViewRoundaboutDegrees(roundaboutDegrees.floatValue());
        }
    }

    private void addBannerInstructions(RouteProgress routeProgress) {
        if (isNewLeg(routeProgress)) {
            instructions = new ArrayList<>();
            currentLeg = routeProgress.currentLeg();
            List<LegStep> steps = currentLeg.steps();
            for (LegStep step : steps) {
                List<CustomInstructionsBanner> customInstructionsBanners = new ArrayList<>();
                double distanceFromStart = step.distance();
                double distanceTraveled = routeProgress.distanceTraveled();
                double distanceToManeuver = distanceFromStart - distanceTraveled;
                    customInstructionsBanners.add(
                            new CustomInstructionsBanner(
                                    step.maneuver().instruction(),
                                    distanceFromStart
                            )
                    );


                if (customInstructionsBanners != null && !customInstructionsBanners.isEmpty()) {
                    instructions.addAll(customInstructionsBanners);
                }
            }
        }
    }

    private boolean isNewLeg(RouteProgress routeProgress) {
        return currentLeg == null || !currentLeg.equals(routeProgress.currentLeg());
    }

    private boolean updateInstructionList(RouteProgress routeProgress) {
        if (instructions.isEmpty()) {
            return false;
        }
        RouteLegProgress legProgress = routeProgress.currentLegProgress();
        LegStep currentStep = legProgress.currentStep();
        double stepDistanceRemaining = legProgress.currentStepProgress().distanceRemaining();
        String currentBannerInstructions = routeUtils.findCurrentInstruction(currentStep);
//    BannerInstructions currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
//      currentStep, stepDistanceRemaining
//    );
        int index = -1;
        boolean found = false;
        boolean searching = true;

        while (searching && index < instructions.size()-1){
            index++;
            if(instructions.get(index).getInstruction().equals(currentBannerInstructions)){
                searching = false;
                found = true;
            }
        }
        if(!found) return false;

        return removeInstructionsFrom(index);
    }

    private boolean removeInstructionsFrom(int currentInstructionIndex) {
        if (currentInstructionIndex == FIRST_INSTRUCTION_INDEX) {
            instructions.remove(FIRST_INSTRUCTION_INDEX);
            return true;
        } else if (currentInstructionIndex <= instructions.size()) {
            instructions.subList(FIRST_INSTRUCTION_INDEX, currentInstructionIndex).clear();
            return true;
        }
        return false;
    }
}
