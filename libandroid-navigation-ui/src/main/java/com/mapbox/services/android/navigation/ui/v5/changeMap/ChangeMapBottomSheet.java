package com.mapbox.services.android.navigation.ui.v5.changeMap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.GsonBuilder;
import com.mapbox.api.directions.v5.DirectionsAdapterFactory;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.ThemeSwitcher;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackAdapter;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackBottomSheetListener;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackClickListener;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackItem;


public class ChangeMapBottomSheet extends BottomSheetDialogFragment implements ChangeMapClickListener.ClickCallback {

    public static final String TAG = ChangeMapBottomSheet.class.getSimpleName();

    private ChangeMapBottomSheetListener changeMapBottomSheetListener;
    private ChangeMapAdapter changeMapAdapter;
    private RecyclerView changeMapItems;


    public static ChangeMapBottomSheet newInstance(ChangeMapBottomSheetListener changeMapBottomSheetListener){
        ChangeMapBottomSheet changeMapBottomSheet = new ChangeMapBottomSheet();
        changeMapBottomSheet.setChangeMapBottomSheetListener(changeMapBottomSheetListener);
        changeMapBottomSheet.setRetainInstance(true);
        return changeMapBottomSheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NO_FRAME, R.style.Theme_Design_BottomSheetDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.changemap_bottom_sheet_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind(view);
        initChangeMapRecyclerView();
//        initBackground(view);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = bottomSheetDialog.findViewById(android.support.design.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                }
            }
        });
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        changeMapBottomSheetListener.onChangeMapDismissed();
    }

    @Override
    public void onDestroyView() {
        removeListener();
        removeDialogDismissMessage();
        super.onDestroyView();
    }


    @Override
    public void onChangeMapItemClick(int changeMapPosition) {
        ChangeMapItem changeMapItem = changeMapAdapter.getChangeMapItem(changeMapPosition);
        changeMapBottomSheetListener.onMapSelected(changeMapItem);
        dismiss();
    }

    public void setChangeMapBottomSheetListener(ChangeMapBottomSheetListener changeMapBottomSheetListener) {
        this.changeMapBottomSheetListener = changeMapBottomSheetListener;
    }

    private void bind(View bottomSheetView) {
        changeMapItems = bottomSheetView.findViewById(R.id.changeMapItems);
    }

    private void initChangeMapRecyclerView() {
        Context context = getContext();
        changeMapAdapter = new ChangeMapAdapter(context);
        changeMapItems.setAdapter(changeMapAdapter);
        changeMapItems.setOverScrollMode(RecyclerView.OVER_SCROLL_IF_CONTENT_SCROLLS);
        changeMapItems.addOnItemTouchListener(new ChangeMapClickListener(context, this));
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeMapItems.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
        } else {
            changeMapItems.setLayoutManager(new GridLayoutManager(context, 3));
        }
    }

//    private void initBackground(View view) {
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
//            int navigationViewPrimaryColor = ThemeSwitcher.retrieveThemeColor(getContext(),
//                    R.attr.navigationViewPrimary);
//            int navigationViewSecondaryColor = ThemeSwitcher.retrieveThemeColor(getContext(),
//                    R.attr.navigationViewSecondary);
//            // BottomSheet background
//            Drawable bottomSheetBackground = DrawableCompat.wrap(view.getBackground()).mutate();
//            DrawableCompat.setTint(bottomSheetBackground, navigationViewPrimaryColor);
//        }
//    }

    private void removeListener() {
        changeMapBottomSheetListener = null;
    }

    private void removeDialogDismissMessage() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
    }



}
