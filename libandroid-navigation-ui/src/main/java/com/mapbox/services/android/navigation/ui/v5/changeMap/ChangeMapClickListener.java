package com.mapbox.services.android.navigation.ui.v5.changeMap;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackClickListener;


public class ChangeMapClickListener implements RecyclerView.OnItemTouchListener {
    private GestureDetector gestureDetector;
    private ClickCallback callback;


    ChangeMapClickListener(Context context, ClickCallback callback){
        this.gestureDetector = new GestureDetector(context, new ResultGestureListener());
        this.callback = callback;
    }
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        View child = rv.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (child != null && gestureDetector.onTouchEvent(motionEvent)) {
            child.playSoundEffect(SoundEffectConstants.CLICK);
            int position = rv.getChildAdapterPosition(child);
            callback.onChangeMapItemClick(position);
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private static class ResultGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return true;
        }
    }

    public interface ClickCallback {

        void onChangeMapItemClick(int changeMapPosition);
    }
}
