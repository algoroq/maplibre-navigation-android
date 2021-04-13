package com.mapbox.services.android.navigation.ui.v5.summary.list;

public class CustomInstructionsBanner {
    private String instruction;
    private double distance;
    public CustomInstructionsBanner(String instruction, double distance){
        this.instruction = instruction;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public String getInstruction() {
        return instruction;
    }
}
