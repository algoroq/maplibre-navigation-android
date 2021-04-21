package com.mapbox.services.android.navigation.ui.v5.speedmeter;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;


import com.mapbox.services.android.navigation.ui.v5.NavigationViewModel;
import com.mapbox.services.android.navigation.ui.v5.R;



public class SpeedMeter extends ConstraintLayout {
    private TextView txtSpeed;
    @Nullable
    private Location locationStamp;
    @Nullable
    private Long timeStamp;
    private Location lastLocation;
    private int nullCounter = 0;


    public SpeedMeter(Context context) {
        this(context, null);
    }

    public SpeedMeter(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SpeedMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void subscribe(NavigationViewModel navigationViewModel) {
        navigationViewModel.currentLocation.observe((LifecycleOwner) getContext(), new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                lastLocation = location;
                if (locationStamp == null) locationStamp = location;
                double speed = location.getSpeed() * 3.6;
                String currentSpeed = String.valueOf(Math.round(speed));
                txtSpeed.setText(currentSpeed);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bind();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * Inflates the layout.
     */
    private void init() {
        inflate(getContext(), R.layout.speedmeter, this);
    }

    private void bind() {
        txtSpeed = findViewById(R.id.txt_speed_meter);

        onClickReset();
        onClickStart();

    }

    private boolean isSameAs(@Nullable Location location1, @Nullable Location location2) {
        if(location1 == null || location2 == null){
            nullCounter++;
            if(nullCounter == 10){
                nullCounter = 0;
                return true;
            }
            return false;
        }
        double lng1 = location1.getLongitude();
        double lat1 = location1.getLatitude();

        double lng2 = location2.getLongitude();
        double lat2 = location2.getLatitude();
        return lng1 == lng2 && lat1 == lat2;
    }


    public void onClickStart(){
        startRun=true;
        Timer();
    }

    public void onClickReset(){
        startRun=false;
        seconds=0;
    }

    private long seconds=0;
    private boolean startRun;
    private void Timer(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(startRun){
                    seconds++;
                    handler.postDelayed(this, 1000);
                }
                if (!isSameAs(lastLocation, locationStamp)) {
                    timeStamp = null;
                    locationStamp = null;
                    return;
                }
                if (timeStamp == null) timeStamp = seconds;
                if (seconds - timeStamp >= 3) {
                    txtSpeed.setText("0");
                }
            }
        });

    }
}
