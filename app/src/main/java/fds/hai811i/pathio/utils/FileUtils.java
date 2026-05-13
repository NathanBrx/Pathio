package fds.hai811i.pathio.utils;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    /**
     * Méthode d'aide pour transformer une uri android (quand on sélectionne une photo) en vrai fichier
     * @param context Fragment context
     * @param uri L'uri android
     * @return L'image si réussite, null sinon
     */
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            // We pass the context in as a parameter!
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload_media", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while (true) {
                assert inputStream != null;
                if (!((length = inputStream.read(buffer)) > 0)) break;
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}