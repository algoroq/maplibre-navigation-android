package com.mapbox.services.android.navigation.ui.v5;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.directions.v5.models.StepManeuver;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.services.android.navigation.ui.v5.camera.DynamicCamera;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackItem;
import com.mapbox.services.android.navigation.ui.v5.instruction.BannerInstructionModel;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionModel;
import com.mapbox.services.android.navigation.ui.v5.location.LocationEngineConductor;
import com.mapbox.services.android.navigation.ui.v5.location.LocationEngineConductorListener;
import com.mapbox.services.android.navigation.ui.v5.route.OffRouteEvent;
import com.mapbox.services.android.navigation.ui.v5.route.ViewRouteFetcher;
import com.mapbox.services.android.navigation.ui.v5.route.ViewRouteListener;
import com.mapbox.services.android.navigation.ui.v5.summary.SummaryModel;
import com.mapbox.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechPlayer;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechPlayerProvider;
import com.mapbox.services.android.navigation.v5.milestone.BannerInstructionMilestone;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.milestone.VoiceInstructionMilestone;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationTimeFormat;
import com.mapbox.services.android.navigation.v5.navigation.camera.Camera;
import com.mapbox.services.android.navigation.v5.navigation.metrics.FeedbackEvent;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.route.FasterRouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.utils.DistanceFormatter;
import com.mapbox.services.android.navigation.v5.utils.LocaleUtils;
import com.mapbox.services.android.navigation.v5.utils.RouteUtils;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfJoins;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

public class NavigationViewModel extends AndroidViewModel {

    private static final String EMPTY_STRING = "";

    public final MutableLiveData<InstructionModel> instructionModel = new MutableLiveData<>();
    public final MutableLiveData<BannerInstructionModel> bannerInstructionModel = new MutableLiveData<>();
    public final MutableLiveData<SummaryModel> summaryModel = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isOffRoute = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isOffRouteOfflineMode = new MutableLiveData<>();
    public final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    final MutableLiveData<Location> navigationLocation = new MutableLiveData<>();
    final MutableLiveData<DirectionsRoute> route = new MutableLiveData<>();
    final MutableLiveData<Point> destination = new MutableLiveData<>();
    final MutableLiveData<Boolean> shouldRecordScreenshot = new MutableLiveData<>();
    public MutableLiveData<Boolean> isVoiceAvailable;
    public boolean wasVoiceAlertNotAvailableShown = false;

    private final MutableLiveData<List<Polygon>> routePolygons = new MutableLiveData<>();

    private final int distanceFromManeuverToReadInstruction = 10;

    private MapboxNavigation navigation;
    private ViewRouteFetcher navigationViewRouteEngine;
    private LocationEngineConductor locationEngineConductor;
    private NavigationViewEventDispatcher navigationViewEventDispatcher;
    private SpeechPlayer speechPlayer;
    private ConnectivityManager connectivityManager;
    private RouteProgress routeProgress;
    private String feedbackId;
    private String screenshot;
    private String language;
    private RouteUtils routeUtils;
    private LocaleUtils localeUtils;
    private DistanceFormatter distanceFormatter;
    private String accessToken;
    @NavigationTimeFormat.Type
    private int timeFormatType;
    private boolean isRunning;
    private boolean isChangingConfigurations;

    public NavigationViewModel(Application application) {
        super(application);
        this.accessToken = Mapbox.getAccessToken();
        initializeConnectivityManager(application);
        initializeNavigationRouteEngine();
        initializeNavigationLocationEngine();

        routeUtils = new RouteUtils();
        localeUtils = new LocaleUtils();
    }

    public void onCreate() {
        if (!isRunning) {
            locationEngineConductor.onCreate();
        }
    }

    public void onDestroy(boolean isChangingConfigurations) {
        this.isChangingConfigurations = isChangingConfigurations;
        if (!isChangingConfigurations) {
            locationEngineConductor.onDestroy();
            deactivateInstructionPlayer();
            endNavigation();
            isRunning = false;
        }
        clearDynamicCameraMap();
        navigationViewEventDispatcher = null;
    }

    public void setMuted(boolean isMuted) {
        speechPlayer.setMuted(isMuted);
    }

    /**
     * Records a general feedback item with source
     */
    public void recordFeedback(@FeedbackEvent.FeedbackSource String feedbackSource) {
        feedbackId = navigation.recordFeedback(FeedbackEvent.FEEDBACK_TYPE_GENERAL_ISSUE, EMPTY_STRING, feedbackSource);
        shouldRecordScreenshot.setValue(true);
    }

    /**
     * Used to update an existing {@link FeedbackItem}
     * with a feedback type and description.
     * <p>
     * Uses cached feedbackId to ensure the proper item is updated.
     *
     * @param feedbackItem item to be updated
     * @since 0.7.0
     */
    public void updateFeedback(FeedbackItem feedbackItem) {
        if (!TextUtils.isEmpty(feedbackId)) {
            navigation.updateFeedback(feedbackId, feedbackItem.getFeedbackType(), feedbackItem.getDescription(), screenshot);
            sendEventFeedback(feedbackItem);
            feedbackId = null;
            screenshot = null;
        }
    }

    /**
     * Used to cancel an existing {@link FeedbackItem}.
     * <p>
     * Uses cached feedbackId to ensure the proper item is cancelled.
     *
     * @since 0.7.0
     */
    public void cancelFeedback() {
        if (!TextUtils.isEmpty(feedbackId)) {
            navigation.cancelFeedback(feedbackId);
            feedbackId = null;
        }
    }

    /**
     * Returns the current instance of {@link MapboxNavigation}.
     * <p>
     * Will be null if navigation has not been initialized.
     */
    @Nullable
    public MapboxNavigation retrieveNavigation() {
        return navigation;
    }

    void initializeEventDispatcher(NavigationViewEventDispatcher navigationViewEventDispatcher) {
        this.navigationViewEventDispatcher = navigationViewEventDispatcher;
    }

    /**
     * This method will pass {@link MapboxNavigationOptions} from the {@link NavigationViewOptions}
     * to this view model to be used to initialize {@link MapboxNavigation}.
     *
     * @param options to init MapboxNavigation
     */
    MapboxNavigation initialize(NavigationViewOptions options) {
        MapboxNavigationOptions navigationOptions = options.navigationOptions();
        navigationOptions = navigationOptions.toBuilder().isFromNavigationUi(true).build();
        initializeLanguage(options);
        initializeTimeFormat(navigationOptions);
        initializeDistanceFormatter(options);
        initializeNavigationSpeechPlayer(options);
        if (!isRunning) {
            LocationEngine locationEngine = initializeLocationEngineFrom(options);
            initializeNavigation(getApplication(), navigationOptions, locationEngine);
            addMilestones(options);
        }
        navigationViewRouteEngine.extractRouteOptions(options);
        return navigation;
    }

    void updateFeedbackScreenshot(String screenshot) {
        if (!TextUtils.isEmpty(feedbackId)) {
            this.screenshot = screenshot;
        }
        shouldRecordScreenshot.setValue(false);
    }

    boolean isRunning() {
        return isRunning;
    }

    void stopNavigation() {
        navigation.stopNavigation();
    }

    private void initializeConnectivityManager(Application application) {
        connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void initializeNavigationRouteEngine() {
        navigationViewRouteEngine = new ViewRouteFetcher(getApplication(), accessToken, routeEngineListener);
    }

    private void initializeNavigationLocationEngine() {
        locationEngineConductor = new LocationEngineConductor(locationEngineCallback);

    }

    private void initializeLanguage(NavigationUiOptions options) {
        RouteOptions routeOptions = options.directionsRoute().routeOptions();
        language = localeUtils.inferDeviceLanguage(getApplication());
        if (routeOptions != null) {
            language = routeOptions.language();
        }
    }

    private String initializeUnitType(NavigationUiOptions options) {
        RouteOptions routeOptions = options.directionsRoute().routeOptions();
        String unitType = localeUtils.getUnitTypeForDeviceLocale(getApplication());
        if (routeOptions != null) {
            unitType = routeOptions.voiceUnits();
        }
        return unitType;
    }

    private void initializeTimeFormat(MapboxNavigationOptions options) {
        timeFormatType = options.timeFormatType();
    }

    private int initializeRoundingIncrement(NavigationViewOptions options) {
        MapboxNavigationOptions navigationOptions = options.navigationOptions();
        return navigationOptions.roundingIncrement();
    }

    private void initializeDistanceFormatter(NavigationViewOptions options) {
        String unitType = initializeUnitType(options);
        int roundingIncrement = initializeRoundingIncrement(options);
        distanceFormatter = new DistanceFormatter(getApplication(), language, unitType, roundingIncrement);
    }

    private void initializeNavigationSpeechPlayer(NavigationViewOptions options) {
        SpeechPlayer speechPlayer = options.speechPlayer();
        if (speechPlayer != null) {
            this.speechPlayer = speechPlayer;
            return;
        }
        SpeechPlayerProvider speechPlayerProvider = initializeSpeechPlayerProvider();
        this.speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);

       isVoiceAvailable = this.speechPlayer.voiceAvailable();
    }

    @NonNull
    private SpeechPlayerProvider initializeSpeechPlayerProvider() {
        return new SpeechPlayerProvider(getApplication());
    }

    private LocationEngine initializeLocationEngineFrom(NavigationViewOptions options) {
        LocationEngine locationEngine = options.locationEngine();
        boolean shouldReplayRoute = options.shouldSimulateRoute();
        locationEngineConductor.initializeLocationEngine(getApplication(), locationEngine, shouldReplayRoute);
        return locationEngineConductor.obtainLocationEngine();
    }

    private void initializeNavigation(Context context, MapboxNavigationOptions options, LocationEngine locationEngine) {
        navigation = new MapboxNavigation(context, accessToken, options, locationEngine);
        addNavigationListeners();
    }

    private void addNavigationListeners() {
        navigation.addProgressChangeListener(progressChangeListener);
        navigation.addOffRouteListener(offRouteListener);
        navigation.addMilestoneEventListener(milestoneEventListener);
        navigation.addNavigationEventListener(navigationEventListener);
        navigation.addFasterRouteListener(fasterRouteListener);
    }

    private void addMilestones(NavigationViewOptions options) {
        List<Milestone> milestones = options.milestones();
        if (milestones != null && !milestones.isEmpty()) {
            navigation.addMilestones(milestones);
        }
    }

    @Nullable
    private LegStep currentStepStamp = null;
    private ProgressChangeListener progressChangeListener = new ProgressChangeListener() {
        @Override
        public void onProgressChange(Location location, RouteProgress routeProgress) {
            //JV IF
            if (routeProgress.durationRemaining() >= 1) {
                if (isOffRouteOfflineMode.getValue() != null) {
                    if (isOffRouteOfflineMode.getValue()) {
                        Point point = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                        if (isPointOnRoute(point)) {
                            isOffRouteOfflineMode.setValue(false);
                        }
                    }
                }

                LegStep currentStep = routeProgress.currentLegProgress().currentStep();
                Point currentPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                List<Double> coordinatesOfManeuver = currentStep.geometry().coordinates().get(currentStep.geometry().coordinates().size() - 1);
                Point pointOfManeuver = Point.fromLngLat(coordinatesOfManeuver.get(0), coordinatesOfManeuver.get(1));
// přečíst
                String instruction = currentStep.maneuver().instruction();

                //instrukce pro step je vždy na začátku stepu -> instrukce, která má být zobrazena na konci současného stepu je v následujícím stepu.
                //step "zacína" instrukcí.. ale my potrebujeme, aby step koncil instrukcí.
                @Nullable
                LegStep nextStep = routeProgress.currentLegProgress().upComingStep();
                if (nextStep != null) {
                    instruction = nextStep.maneuver().instruction();
                }


//                String instructionWithDistance = "Za " + currentStep.distance() + "metrů " + nextStepInstruction;


                double distanceToManeuver = TurfMeasurement.distance(currentPoint, pointOfManeuver, TurfConstants.UNIT_METERS);

                if (distanceToManeuver <= distanceFromManeuverToReadInstruction) {
                    if (currentStepStamp == null) {
                        playVoiceAnnouncement(instruction);
                        currentStepStamp = currentStep;
                    }
                }

                if (currentStepStamp != currentStep) {
                    currentStepStamp = null;
                }

                NavigationViewModel.this.routeProgress = routeProgress;
                instructionModel.setValue(new InstructionModel(distanceFormatter, routeProgress));
                summaryModel.setValue(new SummaryModel(getApplication(), distanceFormatter, routeProgress, timeFormatType));
                navigationLocation.setValue(location);
            } else {
                endNavigation();
            }
        }
    };

    private Boolean isPointOnRoute(Point point) {
        int i = 0;
        boolean searching = true;
        boolean found = false;
        List<Polygon> polygons = routePolygons.getValue();
        while (i < polygons.size() && searching) {

            if (TurfJoins.inside(point, polygons.get(i))) {
                searching = false;
                found = true;
            }
            i++;

        }
        return found;
    }


    private OffRouteListener offRouteListener = new OffRouteListener() {
        @Override
        public void userOffRoute(Location location, boolean offlineMode) {
            isOffRouteOfflineMode.setValue(offlineMode);
            System.out.println("XXXXXXX -" + offlineMode);
            if (!hasNetworkConnection() || offlineMode) return;
            speechPlayer.onOffRoute();
            Point newOrigin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            sendEventOffRoute(newOrigin);

        }
    };

    private MilestoneEventListener milestoneEventListener = new MilestoneEventListener() {
        @Override
        public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
            System.out.println("bbbbb - play voice - Milestone event");
//             playVoiceAnnouncement(milestone);
            updateBannerInstruction(routeProgress, milestone);
            sendEventArrival(routeProgress, milestone);
        }
    };

    private NavigationEventListener navigationEventListener = new NavigationEventListener() {
        @Override
        public void onRunning(boolean isRunning) {
            NavigationViewModel.this.isRunning = isRunning;
            sendNavigationStatusEvent(isRunning);
        }
    };

    private FasterRouteListener fasterRouteListener = new FasterRouteListener() {
        @Override
        public void fasterRouteFound(DirectionsRoute directionsRoute) {
            updateRoute(directionsRoute);
        }
    };

    private ViewRouteListener routeEngineListener = new ViewRouteListener() {
        @Override
        public void onRouteUpdate(DirectionsRoute directionsRoute) {
            updateRoute(directionsRoute);
        }

        @Override
        public void onRouteRequestError(Throwable throwable) {
            if (isOffRoute()) {
                String errorMessage = throwable.getMessage();
                sendEventFailedReroute(errorMessage);
            }
        }

        @Override
        public void onDestinationSet(Point destination) {
            NavigationViewModel.this.destination.setValue(destination);
        }
    };

    private LocationEngineConductorListener locationEngineCallback = new LocationEngineConductorListener() {
        @Override
        public void onLocationUpdate(Location location) {


            navigationViewRouteEngine.updateRawLocation(location);
            currentLocation.setValue(location);
        }
    };

    private void updateRoute(DirectionsRoute route) {
        this.route.setValue(route);
        createRoutesArea(route.geometry().coordinates());
        startNavigation(route);
        updateSimulatedRoute(route);
        resetConfigurationFlag();
        sendEventOnRerouteAlong(route);
        isOffRoute.setValue(false);
        //isOffRouteOfflineMode.setValue(false);
    }

    private void createRoutesArea(List<List<Double>> coordinates) {
        List<Point> points = new ArrayList<>();
        for (List<Double> coordinate : coordinates) {
            Point point = Point.fromLngLat(coordinate.get(0), coordinate.get(1));
            points.add(point);
        }
        LineString line = LineString.fromLngLats(points);
        List<Point> p = line.coordinates();
        List<Polygon> polygons = new ArrayList<>();
        for (Point point : p) {
            List<List<Point>> helper = new ArrayList<>();
            helper.add(getCirclePoints(point));
            polygons.add(Polygon.fromLngLats(helper));
        }
        routePolygons.setValue(polygons);
    }

    private ArrayList<Point> getCirclePoints(
            Point position
    ) {
        float radius = 10;
        int degreesBetweenPoints = 10; // change here for shape
        int numberOfPoints = (int) (Math.floor(360 / (double) degreesBetweenPoints));

        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = position.latitude() * Math.PI / 180;
        double centerLonRadians = position.longitude() * Math.PI / 180;
        ArrayList<Point> polygons = new ArrayList<>(); // array to hold all the points

        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = (double) (index * degreesBetweenPoints);
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(
                    Math.sin(centerLatRadians) * Math.cos(distRadians)
                            + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians)
            );
            double pointLonRadians = centerLonRadians + Math.atan2(
                    Math.sin(degreeRadians)
                            * Math.sin(distRadians) * Math.cos(centerLatRadians),
                    Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians)
            );
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            Point point = Point.fromLngLat(pointLon, pointLat);
            polygons.add(point);
        }
        // add first point at end to close circle
        polygons.add(polygons.get(0));
        return polygons;
    }

    private boolean isOffRoute() {
        try {
            return isOffRoute.getValue();
        } catch (NullPointerException exception) {
            return false;
        }
    }

    private void startNavigation(DirectionsRoute route) {
        if (route != null) {
            navigation.startNavigation(route);
        }
    }

    private void endNavigation() {
        if (navigation != null) {
            navigation.onDestroy();
        }
    }

    private void clearDynamicCameraMap() {
        if (navigation != null) {
            Camera cameraEngine = navigation.getCameraEngine();
            boolean isDynamicCamera = cameraEngine instanceof DynamicCamera;
            if (isDynamicCamera) {
                ((DynamicCamera) cameraEngine).clearMap();
            }
        }
    }

    private void deactivateInstructionPlayer() {
        if (speechPlayer != null) {
            speechPlayer.onDestroy();
        }
    }

    private void playVoiceAnnouncement(Milestone milestone) {
        if (milestone instanceof VoiceInstructionMilestone) {
            SpeechAnnouncement announcement = SpeechAnnouncement.builder()
                    .voiceInstructionMilestone((VoiceInstructionMilestone) milestone).build();
            announcement = retrieveAnnouncementFromSpeechEvent(announcement);

            speechPlayer.play(announcement);
        }
    }

    private void playVoiceAnnouncement(String textToSpeak) {
        SpeechAnnouncement announcement = SpeechAnnouncement.builder().announcement(textToSpeak).build();
        announcement = retrieveAnnouncementFromSpeechEvent(announcement);
        speechPlayer.play(announcement);
    }

    private void updateBannerInstruction(RouteProgress routeProgress, Milestone milestone) {
        if (milestone instanceof BannerInstructionMilestone) {
            BannerInstructions instructions = ((BannerInstructionMilestone) milestone).getBannerInstructions();
            instructions = retrieveInstructionsFromBannerEvent(instructions);
            if (instructions != null) {
                BannerInstructionModel model = new BannerInstructionModel(distanceFormatter, routeProgress, instructions);
                bannerInstructionModel.setValue(model);
            }
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private boolean hasNetworkConnection() {
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void sendEventFeedback(FeedbackItem feedbackItem) {
        if (navigationViewEventDispatcher != null) {
            navigationViewEventDispatcher.onFeedbackSent(feedbackItem);
        }
    }

    private void sendEventArrival(RouteProgress routeProgress, Milestone milestone) {
        if (navigationViewEventDispatcher != null && routeUtils.isArrivalEvent(routeProgress, milestone)) {
            navigationViewEventDispatcher.onArrival();
        }
    }

    private void sendEventOffRoute(Point newOrigin) {
        if (navigationViewEventDispatcher != null && navigationViewEventDispatcher.allowRerouteFrom(newOrigin)) {
            navigationViewEventDispatcher.onOffRoute(newOrigin);
            OffRouteEvent event = new OffRouteEvent(newOrigin, routeProgress);
            navigationViewRouteEngine.fetchRouteFromOffRouteEvent(event);
            isOffRoute.setValue(true);
        }
    }

    private void sendNavigationStatusEvent(boolean isRunning) {
        if (navigationViewEventDispatcher != null) {
            if (isRunning) {
                navigationViewEventDispatcher.onNavigationRunning();
            } else {
                navigationViewEventDispatcher.onNavigationFinished();
            }
        }
    }

    private void sendEventFailedReroute(String errorMessage) {
        if (navigationViewEventDispatcher != null) {
            navigationViewEventDispatcher.onFailedReroute(errorMessage);
        }
    }

    private void updateSimulatedRoute(DirectionsRoute route) {
        if (!isChangingConfigurations) {
            locationEngineConductor.updateSimulatedRoute(route);
        }
    }

    private void resetConfigurationFlag() {
        if (isChangingConfigurations) {
            isChangingConfigurations = false;
        }
    }

    private void sendEventOnRerouteAlong(DirectionsRoute route) {
        if (navigationViewEventDispatcher != null && isOffRoute()) {
            navigationViewEventDispatcher.onRerouteAlong(route);
        }
    }


    private SpeechAnnouncement retrieveAnnouncementFromSpeechEvent(SpeechAnnouncement announcement) {
        if (navigationViewEventDispatcher != null) {
            announcement = navigationViewEventDispatcher.onAnnouncement(announcement);
        }
        return announcement;
    }

    private BannerInstructions retrieveInstructionsFromBannerEvent(BannerInstructions instructions) {
        if (navigationViewEventDispatcher != null) {
            instructions = navigationViewEventDispatcher.onBannerDisplay(instructions);
        }
        return instructions;
    }


}
