package com.mapbox.services.android.navigation.ui.v5.changeMap;

public class ChangeMapItem {
    private String title;
    private int mapImage;
    private long id;

    ChangeMapItem(String title, int mapImage, long id){
        this.mapImage = mapImage;
        this.title = title;
        this.id = id;
    }

   public String getTitle(){return title;}
    public int getMapImage(){return mapImage;}
    public long getId(){return id;}

}
