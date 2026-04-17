package fds.hai811i.pathio;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import fds.hai811i.pathio.model.ApiService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit instance;
    private static final String BASE_URL = "https://www.zerohour.fr/";

    public static ApiService getApi(Context context) {
        if (instance == null) {

            Interceptor authInterceptor = new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request originalRequest = chain.request();

                    SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

                    String token = sharedPreferences.getString("jwt_token", null);

                    // if a token exists, clone the request and attach the Authorization header
                    if (token != null) {
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    }

                    // no token, normal behaviour
                    return chain.proceed(originalRequest);
                }
            };

            // add interceptor to custom client
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            // retrofit client building
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance.create(ApiService.class);
    }
}