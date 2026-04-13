package fds.hai811i.pathio.model;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/register")
    Call<ResponseBody> register(@Body RegisterRequest body);

    @POST("api/login")
    Call<ResponseBody> login(@Body LoginRequest body);
}