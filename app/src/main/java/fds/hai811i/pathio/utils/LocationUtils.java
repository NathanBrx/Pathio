package fds.hai811i.pathio.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public interface LocationCallback {
        void onLocationResult(Location location);
    }

    @SuppressLint("MissingPermission")
    public static void getLastKnownLocation(Context context, LocationCallback callback) {
        if (!hasLocationPermission(context)) {
            callback.onLocationResult(null);
            return;
        }

        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);
        fusedClient.getLastLocation().addOnSuccessListener(callback::onLocationResult);
    }

    public static String getReadableAddress(Context context, Location location) {
        if (location == null) return "Unknown Location";

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String city = address.getLocality();
                if (city == null) city = address.getAdminArea();

                String area = address.getSubLocality();
                if (area == null) area = address.getThoroughfare();

                return (city != null ? city : "") + (area != null ? " - " + area : "");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return "Location Error";
    }
}