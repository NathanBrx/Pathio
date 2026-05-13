package fds.hai811i.pathio.model;

import java.util.List;

import fds.hai811i.pathio.model.requests.*;
import fds.hai811i.pathio.model.responses.*;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/register")
    Call<RegisterResponse> register(@Body RegisterRequest body);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("api/profile")
    Call<ProfileResponse> profile();

    @GET("api/posts")
    Call<List<Post>> getPosts();

    @Multipart
    @PUT("api/profile/avatar/upload")
    Call<ResponseBody> uploadAvatar(@Part MultipartBody.Part avatarFile);

    @POST("api/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest body);

    @POST("api/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest body);

    // --- TravelShare Extensions ---

    // Groups
    @GET("api/groups")
    Call<List<Group>> getGroups();

    @POST("api/groups")
    Call<Group> createGroup(@Body Group group);

    @POST("api/groups/{id}/join")
    Call<ResponseBody> joinGroup(@Path("id") int groupId);

    // Interactions
    @POST("api/posts/{id}/like")
    Call<ResponseBody> likePost(@Path("id") int postId);

    @DELETE("api/posts/{id}/like")
    Call<ResponseBody> unlikePost(@Path("id") int postId);

    @POST("api/posts/{id}/comments")
    Call<Comment> addComment(@Path("id") int postId, @Query("text") String text);
    }