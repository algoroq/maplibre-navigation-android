package com.mapbox.services.android.navigation.ui.v5.voice;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

/**
 * Default player used to play voice instructions when a connection to Polly is unable to be established.
 * <p>
 * This instruction player uses {@link TextToSpeech} to play voice instructions.
 *
 * @since 0.6.0
 */
class AndroidSpeechPlayer implements SpeechPlayer {

    private static final String DEFAULT_UTTERANCE_ID = "default_id";
    private static final String GOOGLE_TTS_ENGINE = "com.google.android.tts";

    private TextToSpeech textToSpeech;
    private SpeechListener speechListener;

    private boolean isMuted;
    private boolean languageSupported = false;
    private MutableLiveData<Boolean> voiceAvailable = new MutableLiveData<>();

    /**
     * Creates an instance of {@link AndroidSpeechPlayer}.
     *
     * @param context used to create an instance of {@link TextToSpeech}
     * @since 0.6.0
     */
    AndroidSpeechPlayer(final Context context, final SpeechListener speechListener, final Locale locale) {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                boolean ableToInitialize = status == TextToSpeech.SUCCESS;
                if (!ableToInitialize) {
                    Timber.e("There was an error initializing native TTS");
                    return;
                }
                setSpeechListener(speechListener);
                initializeWithLanguage(locale);
            }
        });
    }



    AndroidSpeechPlayer(final Context context, final SpeechListener speechListener) {
        TextToSpeech.OnInitListener initListener = status -> {
            boolean ableToInitialize = status == TextToSpeech.SUCCESS;
            if (!ableToInitialize) {
                Timber.e("There was an error initializing native TTS");
                return;
            }
            setSpeechListener(speechListener);
            initializeWithLanguage(inferDeviceLocale(context));
        };
        try {
            textToSpeech = new TextToSpeech(context, initListener, GOOGLE_TTS_ENGINE);
        }catch (Exception e){
            textToSpeech = new TextToSpeech(context, initListener);
        }
    }

    /**
     * Plays the given voice instruction using TTS
     *
     * @param speechAnnouncement with voice instruction to be synthesized and played
     */
    @Override
    public void play(SpeechAnnouncement speechAnnouncement) {
        boolean isValidAnnouncement = speechAnnouncement != null
                && !TextUtils.isEmpty(speechAnnouncement.announcement());

        boolean canPlay = isValidAnnouncement && languageSupported && !isMuted;
        if (!canPlay) {
            return;
        }

        fireInstructionListenerIfApi14();

        HashMap<String, String> params = new HashMap<>(1);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, DEFAULT_UTTERANCE_ID);
        textToSpeech.speak(speechAnnouncement.announcement(), TextToSpeech.QUEUE_FLUSH, params);
    }




    /**
     * Returns whether or not the AndroidSpeechPlayer is currently muted
     *
     * @return true if muted, false if not
     */
    @Override
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Mutes or un-mutes the AndroidSpeechPlayer, canceling any instruction currently being voiced,
     * and preventing subsequent instructions from being voiced
     *
     * @param isMuted true if should be muted, false if should not
     */
    @Override
    public void setMuted(boolean isMuted) {
        this.isMuted = isMuted;
        if (isMuted) {
            muteTts();
        }
    }

    /**
     * To be called during an off-route event, mutes TTS
     */
    @Override
    public void onOffRoute() {
        muteTts();
    }

    /**
     * Stops and shuts down TTS
     */
    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public MutableLiveData<Boolean> voiceAvailable() {
        return voiceAvailable;
    }

    private void muteTts() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }


    private void initializeWithLanguage(Locale language) {
        boolean isLanguageAvailableHelp;
        isLanguageAvailableHelp = textToSpeech.isLanguageAvailable(language) == TextToSpeech.LANG_AVAILABLE;

        boolean isAvailable = isLanguageAvailableHelp;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Set<Locale> locales = textToSpeech.getAvailableLanguages();
            isAvailable = locales.contains(language) || isLanguageAvailableHelp;
        }
//        else{
//            isLanguageAvailable = textToSpeech.isLanguageAvailable(language) == TextToSpeech.LANG_AVAILABLE;
//        }

        if (!isAvailable) {
            Timber.w("The specified language is not supported by TTS");
            voiceAvailable.postValue(false);
            return;
        }
        languageSupported = true;
        voiceAvailable.postValue(true);
        textToSpeech.setLanguage(language);
    }

    private void fireInstructionListenerIfApi14() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            speechListener.onStart();
        }
    }

    private void setSpeechListener(final SpeechListener speechListener) {
        this.speechListener = speechListener;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            textToSpeech.setOnUtteranceCompletedListener(new Api14UtteranceListener(speechListener));
        } else {
            textToSpeech.setOnUtteranceProgressListener(new UtteranceListener(speechListener));
        }
    }

    private Locale inferDeviceLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }
}
