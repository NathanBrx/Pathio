package fds.hai811i.pathio;

import fds.hai811i.pathio.model.ApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit instance;
    private static final String BASE_URL = "https://www.zerohour.fr/";

    public static ApiService getApi() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance.create(ApiService.class);
    }
}