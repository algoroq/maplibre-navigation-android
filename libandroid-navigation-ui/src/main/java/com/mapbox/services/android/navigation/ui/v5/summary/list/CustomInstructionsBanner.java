package com.mapbox.services.android.navigation.ui.v5.summary.list;

import com.mapbox.api.directions.v5.models.StepManeuver;

public class CustomInstructionsBanner {
    private String instruction;
    private double distance;
    private String maneuverType;
    private String maneuverModifier;

    public CustomInstructionsBanner(StepManeuver maneuver, double distance){
        this.instruction = maneuver.instruction();
        this.distance = distance;
        this.maneuverModifier = maneuver.modifier();
        this.maneuverType = maneuver.type();
    }

    public double getDistance() {
        return distance;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getManeuverModifier() {
        return maneuverModifier;
    }

    public String getManeuverType() {
        return maneuverType;
    }
}
