package fds.hai811i.pathio.model;

import fds.hai811i.pathio.model.requests.*;
import fds.hai811i.pathio.model.responses.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/register")
    Call<RegisterResponse> register(@Body RegisterRequest body);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("api/profile")
    Call<ProfileResponse> profile();
}