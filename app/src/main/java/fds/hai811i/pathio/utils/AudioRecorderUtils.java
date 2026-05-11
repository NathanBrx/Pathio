package fds.hai811i.pathio.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorderUtils {
    private MediaRecorder recorder;
    private String currentAudioPath;
    public AudioRecorderUtils() {}
    public String startRecording(Context context) {
        File cacheDir = context.getCacheDir();
        File audioFile = new File(cacheDir, "voice_narrative_" + System.currentTimeMillis() + ".m4a");
        currentAudioPath = audioFile.getAbsolutePath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = new MediaRecorder(context);
        } else {
            recorder = new MediaRecorder();
        }

        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(currentAudioPath);

            recorder.prepare();
            recorder.start();
            return currentAudioPath;

        } catch (IOException e) {
            Log.e("AudioRecorder", "Recording failed: " + e.getMessage());
            return null;
        }
    }

    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (RuntimeException e) {
                Log.e("AudioRecorder", "Failed to stop properly");
            }
            recorder = null;
        }
    }

    public String getCurrentAudioPath() {
        return currentAudioPath;
    }
}