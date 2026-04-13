package fds.hai811i.pathio;

import android.Manifest;
import android.content.Context;
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import fds.hai811i.pathio.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {
    private FragmentMapBinding binding;
    private MyLocationNewOverlay myLocationOverlay;
    private IMapController mapController;

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

        boolean hasPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if (hasPermission) {
            enableUserLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
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

        Toast.makeText(getContext(), "Recherche du signal satellite...", Toast.LENGTH_SHORT).show();
        initLocationOverlay();
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(() -> requireActivity().runOnUiThread(() -> {
            GeoPoint loc = myLocationOverlay.getMyLocation();
            if (loc != null) {
                mapController.animateTo(loc);
                mapController.setZoom(18.0);
            }
        }));
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