package com.mapbox.services.android.navigation.ui.v5.voice;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import java.util.Locale;

/**
 * Given to the constructor of {@link NavigationSpeechPlayer}, this class decides which
 * {@link SpeechPlayer} should be used based on voice language compatibility.
 * <p>
 * If the given {@link DirectionsRoute#voiceLanguage()} is not <tt>null</tt>, this means the language is
 * supported by the Mapbox Voice API, which can parse SSML.  The boolean <tt>voiceLanguageSupported</tt> should
 * be try in this case.
 * <p>
 * If false, an instance of {@link MapboxSpeechPlayer} will never be provided to the {@link NavigationSpeechPlayer}.
 * The SDK will default to the {@link AndroidSpeechPlayer} powered by {@link android.speech.tts.TextToSpeech}.
 *
 * @since 0.16.0
 */
public class SpeechPlayerProvider {

  private SpeechPlayer speechPlayer;

  /**
   * Constructed when creating an instance of {@link NavigationSpeechPlayer}.
   *
   * @param context                for the initialization of the speech players
   * @since 0.16.0
   */
  public SpeechPlayerProvider(@NonNull Context context) {
    initialize(context);
  }

  SpeechPlayer retrieveSpeechPlayer() {
    return speechPlayer;
  }

  void setMuted(boolean isMuted) {
    speechPlayer.setMuted(isMuted);
  }

  void onOffRoute() {
    speechPlayer.onOffRoute();
  }

  void onDestroy() {
    speechPlayer.onDestroy();
  }

  private void initialize(@NonNull Context context) {
    AudioFocusDelegateProvider provider = buildAudioFocusDelegateProvider(context);
    SpeechAudioFocusManager audioFocusManager = new SpeechAudioFocusManager(provider);
    SpeechListener speechListener = new NavigationSpeechListener(this, audioFocusManager);
    initSpeechPlayer(context, speechListener);
  }

  private AudioFocusDelegateProvider buildAudioFocusDelegateProvider(Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    return new AudioFocusDelegateProvider(audioManager);
  }

  private void initSpeechPlayer(Context context,
                                SpeechListener listener) {
    speechPlayer = new AndroidSpeechPlayer(context, listener);
  }
}
