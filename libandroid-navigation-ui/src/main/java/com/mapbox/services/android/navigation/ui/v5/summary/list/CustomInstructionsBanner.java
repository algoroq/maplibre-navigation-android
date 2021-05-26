package com.mapbox.services.android.navigation.ui.v5.summary.list;


import com.mapbox.api.directions.v5.models.StepManeuver;
import com.mapbox.geojson.Point;



public class CustomInstructionsBanner {
    private String instruction;
    private double distance;
    private String maneuverType;
    private String maneuverModifier;
    private Point location;


    public CustomInstructionsBanner(StepManeuver maneuver, double distance){
        this.instruction = maneuver.instruction();
        this.distance = distance;
        this.maneuverModifier = maneuver.modifier();
        this.maneuverType = maneuver.type();
        this.location = maneuver.location();

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

    public Point getLocation() {return location;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomInstructionsBanner newOne = (CustomInstructionsBanner) o;

        boolean sameInstruction = instruction.equals(newOne.instruction);
        boolean sameType = maneuverType.equals(newOne.maneuverType);

        boolean sameModifier = maneuverModifier == null && newOne.maneuverModifier == null;
        if(maneuverModifier != null && newOne.maneuverModifier != null){
            sameModifier = maneuverModifier.equals(newOne.maneuverModifier);
        }

        boolean sameLocation = location.equals(newOne.location);

        return sameInstruction && sameType && sameModifier && sameLocation;
    }
}