package fds.hai811i.pathio;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fds.hai811i.pathio.databinding.FragmentMapBinding;
import fds.hai811i.pathio.model.Itinerary;
import fds.hai811i.pathio.model.POI;
import fds.hai811i.pathio.model.responses.OSRMResponse;
import fds.hai811i.pathio.utils.LocationUtils;
import fds.hai811i.pathio.utils.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment {
    private FragmentMapBinding binding;
    private MyLocationNewOverlay myLocationOverlay;
    private IMapController mapController;
    private Itinerary displayedItinerary;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableUserLocation();
                } else {
                    Toast.makeText(getContext(), "Permission GPS refusée.", Toast.LENGTH_SHORT).show();
                }
            });

    public MapFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapController = binding.mapView.getController();
        mapController.setZoom(16.0);

        initLocationOverlay();

        binding.btnZoomIn.setOnClickListener(v -> mapController.zoomIn());
        binding.btnZoomOut.setOnClickListener(v -> mapController.zoomOut());
        binding.btnLocate.setOnClickListener(v -> onLocateClicked());

        if (LocationUtils.hasLocationPermission(requireContext())) {
            enableUserLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        handleIncomingData();
    }

    private void handleIncomingData() {
        if (getArguments() != null) {
            // Priority 1: Full Itinerary from NewPathFragment
            displayedItinerary = (Itinerary) getArguments().getSerializable("itinerary");
            if (displayedItinerary != null) {
                displayItinerary(displayedItinerary);
                return;
            }

            // Priority 2: Single Location from Gallery Bridge
            String targetLocation = getArguments().getString("target_location");
            if (targetLocation != null) {
                generateItineraryToLocation(targetLocation);
            }
        }
    }

    private void generateItineraryToLocation(String locationName) {
        if (locationName == null || locationName.isEmpty()) return;

        binding.tvLoadingMessage.setText("Recherche de l'adresse...");
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    POI targetPoi = new POI(locationName, address.getLatitude(), address.getLongitude(), 
                                            "discover", 0, 1, "Destination trouvée via recherche");

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> startRoutingWithTarget(targetPoi));
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Localisation introuvable sur la carte", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> binding.loadingOverlay.setVisibility(View.GONE));
                }
            }
        }).start();
    }

    private void startRoutingWithTarget(POI targetPoi) {
        binding.tvLoadingMessage.setText("Attente du signal GPS...");
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        
        myLocationOverlay.runOnFirstFix(() -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                GeoPoint myPos = myLocationOverlay.getMyLocation();
                Itinerary bridgeItinerary = new Itinerary("Itinéraire direct", "---", "---", "---");
                
                if (myPos != null) {
                    bridgeItinerary.steps.add(new POI("Ma position", myPos.getLatitude(), myPos.getLongitude(), "start", 0, 0, "Départ"));
                } else {
                    bridgeItinerary.steps.add(new POI("Départ (Ma position)", 43.6107, 3.8767, "start", 0, 0, "Départ"));
                }
                
                bridgeItinerary.steps.add(targetPoi);
                displayItinerary(bridgeItinerary);
            });
        });

        GeoPoint currentPos = myLocationOverlay.getMyLocation();
        if (currentPos != null) {
            Itinerary bridgeItinerary = new Itinerary("Itinéraire direct", "---", "---", "---");
            bridgeItinerary.steps.add(new POI("Ma position", currentPos.getLatitude(), currentPos.getLongitude(), "start", 0, 0, "Départ"));
            bridgeItinerary.steps.add(targetPoi);
            displayItinerary(bridgeItinerary);
        }
    }

    private void displayItinerary(Itinerary itinerary) {
        if (itinerary.steps == null || itinerary.steps.isEmpty()) return;

        binding.tvLoadingMessage.setText("Calcul de l'itinéraire...");
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker || overlay instanceof Polyline);

        StringBuilder coordsBuilder = new StringBuilder();
        for (int i = 0; i < itinerary.steps.size(); i++) {
            POI poi = itinerary.steps.get(i);
            Marker marker = new Marker(binding.mapView);
            marker.setPosition(new GeoPoint(poi.getLatitude(), poi.getLongitude()));
            marker.setIcon(createCustomMarker(i + 1));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(poi.getName());
            marker.setSnippet(poi.getDescription());
            
            marker.setOnMarkerClickListener((m, mapView) -> {
                m.showInfoWindow();
                mapView.getController().animateTo(m.getPosition());
                return true;
            });

            binding.mapView.getOverlays().add(marker);

            coordsBuilder.append(poi.getLongitude()).append(",").append(poi.getLatitude());
            if (i < itinerary.steps.size() - 1) coordsBuilder.append(";");
        }

        fetchRoute(coordsBuilder.toString());

        POI first = itinerary.steps.get(0);
        mapController.setCenter(new GeoPoint(first.getLatitude(), first.getLongitude()));
        mapController.setZoom(15.0);
        binding.mapView.invalidate();
    }

    private Drawable createCustomMarker(int index) {
        int size = 110;
        Bitmap bitmap = Bitmap.createBitmap(size, size + 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        paint.setColor(Color.parseColor("#D45D3B"));
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(size / 2f - 15, size - 10);
        path.lineTo(size / 2f + 15, size - 10);
        path.lineTo(size / 2f, size + 10);
        path.close();
        canvas.drawPath(path, paint);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 5, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 12, paint);

        paint.setColor(Color.parseColor("#D45D3B"));
        paint.setTextSize(40f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        
        float textY = (size / 2f) - ((paint.descent() + paint.ascent()) / 2f);
        canvas.drawText(String.valueOf(index), size / 2f, textY, paint);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private void fetchRoute(String coordinates) {
        RetrofitClient.getRoutingApi().getRoute(coordinates, "full", "geojson").enqueue(new Callback<OSRMResponse>() {
            @Override
            public void onResponse(Call<OSRMResponse> call, Response<OSRMResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().routes.isEmpty()) {
                    drawRoute(response.body().routes.get(0));
                } else {
                    binding.loadingOverlay.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<OSRMResponse> call, Throwable t) {
                binding.loadingOverlay.setVisibility(View.GONE);
                if (getContext() != null)
                    Toast.makeText(getContext(), "Erreur de chargement de l'itinéraire", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(OSRMResponse.Route route) {
        Polyline line = new Polyline();
        line.getOutlinePaint().setColor(Color.parseColor("#D45D3B"));
        line.getOutlinePaint().setStrokeWidth(12f);

        List<GeoPoint> points = new ArrayList<>();
        for (List<Double> coord : route.geometry.coordinates) {
            points.add(new GeoPoint(coord.get(1), coord.get(0)));
        }
        line.setPoints(points);
        binding.mapView.getOverlays().add(0, line);
        binding.mapView.invalidate();
        binding.loadingOverlay.setVisibility(View.GONE);
    }

    private void initLocationOverlay() {
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            binding.mapView.getOverlays().remove(myLocationOverlay);
        }
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), binding.mapView);
        binding.mapView.getOverlays().add(myLocationOverlay);
    }

    private void enableUserLocation() {
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        binding.mapView.postInvalidate();
    }

    private void onLocateClicked() {
        GeoPoint myLocation = myLocationOverlay.getMyLocation();
        if (myLocation != null) {
            mapController.animateTo(myLocation);
            mapController.setZoom(18.0);
            return;
        }

        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled) {
            Toast.makeText(getContext(), "Veuillez activer la localisation de votre téléphone.", Toast.LENGTH_LONG).show();
            return;
        }

        binding.tvLoadingMessage.setText("Recherche du signal satellite...");
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        
        initLocationOverlay();
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    GeoPoint loc = myLocationOverlay.getMyLocation();
                    if (loc != null) {
                        mapController.animateTo(loc);
                        mapController.setZoom(18.0);
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.mapView.onResume();
        if (myLocationOverlay != null) myLocationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) binding.mapView.onPause();
        if (myLocationOverlay != null) myLocationOverlay.disableMyLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.mapView.onDetach();
        binding = null;
    }
}
