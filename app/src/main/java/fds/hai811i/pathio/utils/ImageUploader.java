package fds.hai811i.pathio.utils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImageUploader {
    public static MultipartBody.Part createAvatarRequest(File imageFile) {
        RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse("image/*"));
        return MultipartBody.Part.createFormData("avatarFile", imageFile.getName(), requestFile);
    }
}