package fds.hai811i.pathio.model.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface PostsCallback {
        void onSuccess(List<Post> posts);
        void onError(String errorMessage);
    }

    public static void createPost(
            Context context,
            String location,
            String caption,
            File imageFile,
            File audioFile,
            Integer groupId,
            ActionCallback callback) {

        // Strings -> RequestBodies
        RequestBody captionBody = RequestBody.create(caption, MultipartBody.FORM);
        RequestBody locationBody = RequestBody.create(location, MultipartBody.FORM);

        // Group ID (null if Public)
        RequestBody groupIdBody = null;
        if (groupId != null) {
            groupIdBody = RequestBody.create(String.valueOf(groupId), MultipartBody.FORM);
        }

        // Image -> MultipartBody.Part
        MultipartBody.Part imagePart = null;
        if (imageFile != null && imageFile.exists()) {
            RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse("image/*"));
            imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
        }

        // Audio -> MultipartBody.Part
        MultipartBody.Part audioPart = null;
        if (audioFile != null && audioFile.exists()) {
            RequestBody requestFile = RequestBody.create(audioFile, MediaType.parse("audio/mp4"));
            audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), requestFile);
        }

        // API Call
        RetrofitClient.getApi(context).createPost(locationBody, captionBody, imagePart, audioPart, groupIdBody).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Post publié avec succès !");
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onError("Erreur réseau: " + t.getMessage());
            }
        });
    }

    public static void getPosts(Context context, PostsCallback callback) {
        RetrofitClient.getApi(context).getPosts().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erreur de chargement du flux");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void toggleLike(Context context, int postId, ActionCallback callback) {
        RetrofitClient.getApi(context).toggleLike(postId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // L'API a bien enregistré le like/unlike
                    callback.onSuccess("Succès");
                } else {
                    callback.onError("Impossible de modifier le like");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}