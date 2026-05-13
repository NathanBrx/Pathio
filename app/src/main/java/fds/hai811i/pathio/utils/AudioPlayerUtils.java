package fds.hai811i.pathio.utils;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioPlayerUtils {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    public AudioPlayerUtils() {}

    public void startPlaying(String filePath, Runnable onCompletion) {
        if (isPlaying) stopPlaying();

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            Log.d("AudioPlayerUtils", "Tentative de lecture de l'URL : " + filePath);

            mediaPlayer.setDataSource(filePath);

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
            });

            mediaPlayer.prepareAsync();

            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlaying();
                if (onCompletion != null) {
                    onCompletion.run();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("AudioPlayerUtils", "Erreur réseau/lecture: what=" + what + ", extra=" + extra);
                stopPlaying();
                return true;
            });

        } catch (IOException e) {
            Log.e("AudioPlayerUtils", "Playback failed: " + e.getMessage());
        }
    }

    public void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    public void pausePlaying() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void resumePlaying() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}