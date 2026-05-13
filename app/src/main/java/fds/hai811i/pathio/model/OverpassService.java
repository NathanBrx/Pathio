package fds.hai811i.pathio.model;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OverpassService {
    @FormUrlEncoded
    @POST("interpreter")
    Call<OverpassResponse> getPOIs(@Field("data") String query);
}
