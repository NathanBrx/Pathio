package fds.hai811i.pathio;

import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fds.hai811i.pathio.databinding.FragmentNewPostBinding;
import fds.hai811i.pathio.utils.LocationUtils;

public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    binding.textTapUpload.setText("Image selected!");
                } else {
                    System.out.println("No media selected");
                }
            });

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if ((fineLocationGranted != null && fineLocationGranted) ||
                        (coarseLocationGranted != null && coarseLocationGranted)) {
                    fetchCurrentLocation();
                } else {
                    binding.location.setText(String.format("%s","Permission denied"));
                }
            });

    public NewPostFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) requireActivity();
        Fragment originalGallery = mainActivity.getExistingFragment(GalleryFragment.class);

        binding.uploadArea.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.btnCloseContainer.setOnClickListener(v -> {
            if (originalGallery != null) {
                mainActivity.navigateTo(originalGallery, 3);
            }
        });

        fetchCurrentLocation();
        binding.locationIconBg.setOnClickListener(v -> fetchCurrentLocation());
        binding.btnChangeLocation.setOnClickListener(v -> showLocationSearchDialog());
    }

    private void fetchCurrentLocation() {
        if (LocationUtils.hasLocationPermission(requireContext())) {
            binding.location.setText(String.format("%s","Locating..."));

            LocationUtils.getLastKnownLocation(requireContext(), location -> {
                if (location != null) {
                    String readableAddress = LocationUtils.getReadableAddress(requireContext(), location);
                    binding.location.setText(readableAddress);
                } else {
                    binding.location.setText(String.format("%s","Location not found, ensure GPS is enabled"));
                }
            });
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void showLocationSearchDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search_location, null);
        EditText inputSearch = dialogView.findViewById(R.id.inputSearchLocation);
        ProgressBar progressSearch = dialogView.findViewById(R.id.progressSearch);
        ListView listResults = dialogView.findViewById(R.id.listLocationResults);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Find Location")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        // Set up the list adapter
        List<Address> addressResults = new ArrayList<>();
        List<String> displayStrings = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayStrings);
        listResults.setAdapter(adapter);

        // Tools for background searching
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        // Listen to typing
        inputSearch.addTextChangedListener(new TextWatcher() {
            Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel the previous search if the user is still typing fast
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() < 3) return;

                searchRunnable = () -> {
                    handler.post(() -> progressSearch.setVisibility(View.VISIBLE));

                    // Do the heavy network search in the background using OSM Nominatim
                    executor.execute(() -> {
                        try {
                            // 1. Format the search URL for OpenStreetMap's API
                            String encodedQuery = URLEncoder.encode(query, "UTF-8");
                            String urlString = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&addressdetails=1&limit=5";

                            URL url = new URL(urlString);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            // OSM requires a User-Agent to avoid blocking your requests
                            conn.setRequestProperty("User-Agent", "PathioApp/1.0");

                            // 2. Read the JSON response
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            reader.close();

                            // 3. Parse the JSON
                            JSONArray jsonArray = new JSONArray(response.toString());

                            // 4. Post results back to the UI thread
                            handler.post(() -> {
                                progressSearch.setVisibility(View.GONE);
                                displayStrings.clear();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        JSONObject obj = jsonArray.getJSONObject(i);

                                        // The specific name of the POI (e.g., "Tour Eiffel")
                                        String name = obj.optString("name", "");

                                        // Dig into the address object for city and country
                                        JSONObject address = obj.getJSONObject("address");
                                        String city = address.optString("city", address.optString("town", address.optString("village", "")));
                                        String country = address.optString("country", "");

                                        // Build a clean string: "Tour Eiffel, Paris, France"
                                        StringBuilder sb = new StringBuilder();
                                        if (!name.isEmpty()) sb.append(name);
                                        if (!city.isEmpty() && !city.equals(name)) sb.append(name.isEmpty() ? city : ", " + city);
                                        if (!country.isEmpty()) sb.append(", ").append(country);

                                        // Fallback to the full display name if parsing fails
                                        String finalString = sb.toString().trim();
                                        if (finalString.isEmpty() || finalString.equals(",")) {
                                            finalString = obj.optString("display_name", "Unknown Location");
                                        }

                                        displayStrings.add(finalString);
                                    } catch (Exception e) {
                                        System.err.println(e.getMessage());
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            });

                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            handler.post(() -> progressSearch.setVisibility(View.GONE));
                        }
                    });
                };
                // 500ms delay (Debounce): Only search if user stops typing for half a second
                handler.postDelayed(searchRunnable, 500);
            }
        });

        // Handle clicks on the results
        listResults.setOnItemClickListener((parent, view, position, id) -> {
            // Address selectedAddress = addressResults.get(position); // Access lat/long here if you need to save it!
            String selectedText = displayStrings.get(position);

            binding.location.setText(selectedText);
            dialog.dismiss();
        });

        // Clean up the background thread when dialog closes
        dialog.setOnDismissListener(d -> executor.shutdown());

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}