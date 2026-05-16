package fds.hai811i.pathio.model.services;

import java.util.List;

import fds.hai811i.pathio.model.*;
import fds.hai811i.pathio.model.requests.*;
import fds.hai811i.pathio.model.responses.*;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    Call<ResponseBody> uploadAvatar(@Part MultipartBody.Part avatar);

    @POST("api/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest body);

    @POST("api/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest body);

    @Multipart
    @POST("api/posts/post")
    Call<ResponseBody> createPost(
            @Part("location") RequestBody location,
            @Part("caption") RequestBody caption,
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part audio
    );

    @POST("api/posts/{postId}/newComment")
    Call<ResponseBody> addComment(
            @Path("postId") int postId,
            @Body CommentRequest body
    );

    @GET("api/posts/{postId}/comments")
    Call<CommentsResponse> getComments(
            @Path("postId") int postId
    );

    @POST("api/posts/{postId}/like")
    Call<ResponseBody> toggleLike(
            @Path("postId") int postId
    );

    @POST("api/posts/{postId}/report")
    Call<ResponseBody> reportPost(
            @Path("postId") int postId
    );

    @GET("api/groups")
    Call<List<Group>> getGroups();

    @POST("api/groups/newGroup")
    Call<ResponseBody> createGroup(
            @Body GroupRequest request
    );

    @POST("api/groups/{groupId}/joinLeave")
    Call<ResponseBody> joinLeaveGroup(
            @Path("groupId") int groupId
    );

    @GET("api/groups/{groupId}/members")
    Call<List<User>> getGroupMembers(
            @Path("groupId") int groupId
    );
}