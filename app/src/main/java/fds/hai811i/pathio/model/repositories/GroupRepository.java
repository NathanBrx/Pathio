package fds.hai811i.pathio.model.repositories;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;

import fds.hai811i.pathio.model.Group;
import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;
import fds.hai811i.pathio.model.requests.GroupRequest;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupRepository {
    public interface GroupCallback {
        void onSuccess(List<Group> groups);
        void onError(String errorMessage);
    }
    public interface MembersCallback {
        void onSuccess(List<User> members);
        void onError(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface GroupPostsCallback {
        void onSuccess(List<Post> posts);
        void onError(String errorMessage);
    }

    public static void fetchGroups(Context context, GroupCallback callback) {
        RetrofitClient.getApi(context).getGroups().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Group>> call, @NonNull Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erreur lors de la récupération des groupes");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Group>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void getGroupMembers(Context context, int groupId, MembersCallback callback) {
        RetrofitClient.getApi(context).getGroupMembers(groupId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erreur lors du chargement des membres");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void joinLeaveGroup(Context context, int groupId, ActionCallback callback) {
        RetrofitClient.getApi(context).joinLeaveGroup(groupId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Statut mis à jour avec succès");
                } else {
                    callback.onError("Impossible de modifier votre statut.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public static void getGroupPosts(Context context, int groupId, GroupPostsCallback callback) {
        RetrofitClient.getApi(context).getGroupPosts(groupId).enqueue(new Callback<>() {
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

    public static void createGroup(Context context, String name, String description, ActionCallback callback) {
        GroupRequest request = new GroupRequest(name, description);

        RetrofitClient.getApi(context).createGroup(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Groupe créé avec succès !");
                } else {
                    callback.onError("Erreur lors de la création du groupe.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}