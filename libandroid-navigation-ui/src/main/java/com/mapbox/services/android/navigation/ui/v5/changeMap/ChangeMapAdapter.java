package com.mapbox.services.android.navigation.ui.v5.changeMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.services.android.navigation.ui.v5.R;

import java.util.ArrayList;
import java.util.List;

public class ChangeMapAdapter extends RecyclerView.Adapter<ChangeMapViewHolder> {
    private List<ChangeMapItem> changeMapItems = new ArrayList<>();
    private Context context;

    ChangeMapAdapter(Context context){
        this.context = context;
        changeMapItems.add(new ChangeMapItem("Letecká mapa", R.drawable.full_color_test_smazat, 1L));
        changeMapItems.add(new ChangeMapItem("Turistická mapa", R.drawable.full_color_test_smazat, 2L));
        changeMapItems.add(new ChangeMapItem("Zimní mapa", R.drawable.full_color_test_smazat, 3L));
        changeMapItems.add(new ChangeMapItem("Základní mapa", R.drawable.full_color_test_smazat, 4L));
        changeMapItems.add(new ChangeMapItem("Dopravní mapa", R.drawable.full_color_test_smazat, 5L));
        changeMapItems.add(new ChangeMapItem("Kreslená mapa", R.drawable.full_color_test_smazat, 6L));
    }

    @NonNull
    @Override
    public ChangeMapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.change_map_viewholder_layout, parent, false);
        return new ChangeMapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChangeMapViewHolder holder, int position) {
        holder.setMapImage(changeMapItems.get(position).getMapImage());
        holder.setMapTitle(changeMapItems.get(position).getTitle());
        if(getSelectedMapIdFromSharedPreferences() != -1){
            holder.setChecked(getSelectedMapIdFromSharedPreferences() == changeMapItems.get(position).getId());
        }else{
            holder.setChecked(false);
        }    }

    @Override
    public int getItemCount() {
        return changeMapItems.size();
    }

    ChangeMapItem getChangeMapItem(int changeMapPosition) {
        if (changeMapPosition < changeMapItems.size()) {
            return changeMapItems.get(changeMapPosition);
        } else {
            return null;
        }
    }

    private long getSelectedMapIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(context.getString(R.string.selected_map_index_key), -1);
    }
}
