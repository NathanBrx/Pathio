package fds.hai811i.pathio.model;

import fds.hai811i.pathio.model.responses.OSRMResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RoutingService {
    @GET("route/v1/foot/{coordinates}")
    Call<OSRMResponse> getRoute(
            @Path("coordinates") String coordinates,
            @Query("overview") String overview,
            @Query("geometries") String geometries
    );
}
