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
}