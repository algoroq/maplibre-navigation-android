package com.mapbox.services.android.navigation.ui.v5.changeMap;

import android.support.constraint.ConstraintLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.services.android.navigation.ui.v5.R;

class ChangeMapViewHolder extends RecyclerView.ViewHolder {
    private TextView mapTitle;
    private ImageView mapImage;
    private ImageView checkedIcon;
    private ConstraintLayout stroke;

    ChangeMapViewHolder(View itemView){
        super(itemView);
        mapTitle = itemView.findViewById(R.id.mapTitle);
        mapImage = itemView.findViewById(R.id.mapImage);
        checkedIcon = itemView.findViewById(R.id.imageViewChecked);
        stroke = itemView.findViewById(R.id.stroke);
    }

    void setMapImage(int mapImageId){
        mapImage.setImageDrawable(AppCompatResources.getDrawable(mapImage.getContext(), mapImageId));
    }

    void setMapTitle(String mapTitle){
        this.mapTitle.setText(mapTitle);
    }

    void setChecked(Boolean checked){
        if(checked){
            checkedIcon.setVisibility(View.VISIBLE);
            stroke.setBackgroundResource(R.color.green);
        }else{
            checkedIcon.setVisibility(View.GONE);
            stroke.setBackgroundResource(R.color.black);
        }
    }

}
