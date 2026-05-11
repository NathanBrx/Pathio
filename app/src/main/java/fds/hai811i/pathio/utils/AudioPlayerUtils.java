package fds.hai811i.pathio.utils;

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
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlaying();
                if (onCompletion != null) {
                    onCompletion.run();
                }
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

    public boolean isPlaying() {
        return isPlaying;
    }
}