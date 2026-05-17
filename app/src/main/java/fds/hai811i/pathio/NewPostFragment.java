package fds.hai811i.pathio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fds.hai811i.pathio.databinding.FragmentNewPostBinding;
import fds.hai811i.pathio.model.Group;
import fds.hai811i.pathio.model.repositories.GroupRepository;
import fds.hai811i.pathio.model.repositories.PostRepository;
import fds.hai811i.pathio.utils.*;

public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private AudioRecorderUtils audioRecorder;
    private AudioPlayerUtils audioPlayer;
    private boolean isRecording = false;
    Uri selectedImageUri;
    private List<Group> myGroups = new ArrayList<>();
    private Integer selectedGroupId = null;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.textTapUpload.setText(uri.toString());
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
                    binding.location.setText(String.format("%s","Permission refusée"));
                }
            });

    private final ActivityResultLauncher<String> audioPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showRecordingDialog();
                } else {
                    binding.switchVoice.setChecked(false);
                    Toast.makeText(getContext(), "Permission micro requise", Toast.LENGTH_SHORT).show();
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

        audioRecorder = new AudioRecorderUtils();
        audioPlayer = new AudioPlayerUtils();

        binding.uploadArea.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.btnCloseContainer.setOnClickListener(v -> {
            if (originalGallery != null) {
                mainActivity.navigateTo(originalGallery, 3);
            }
        });

        loadUserGroups();
        binding.btnChangeDestination.setOnClickListener(v -> showDestinationDialog());

        fetchCurrentLocation();
        binding.locationIconBg.setOnClickListener(v -> fetchCurrentLocation());
        binding.btnChangeLocation.setOnClickListener(v -> showLocationSearchDialog());

        // Voice Switch Logic
        binding.switchVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // They turned it ON -> Check permissions and show dialog
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    showRecordingDialog();
                } else {
                    audioPermissionRequest.launch(Manifest.permission.RECORD_AUDIO);
                }
            } else {
                // They turned it OFF -> Clean up audio, hide play button
                if (isRecording) {
                    audioRecorder.stopRecording();
                    isRecording = false;
                }
                if (audioPlayer.isPlaying()) audioPlayer.stopPlaying();

                binding.subtitleVoice.setText(R.string.addAudio);
                binding.subtitleVoice.setTextColor(Color.parseColor("#718096"));
                binding.btnPreviewAudio.setVisibility(View.GONE);
            }
        });

        binding.btnPreviewAudio.setOnClickListener(v -> {
            if (audioPlayer.isPlaying()) {
                audioPlayer.stopPlaying();
                resetPlayButtonUI();
            } else {
                String path = audioRecorder.getCurrentAudioPath();
                if (path != null) {
                    binding.btnPreviewAudio.setText(String.format("%s","Stop"));
                    binding.btnPreviewAudio.setIconResource(android.R.drawable.ic_media_pause);
                    audioPlayer.startPlaying(path, this::resetPlayButtonUI);
                }
            }
        });

        binding.btnPublish.setOnClickListener(v -> {
            String caption = binding.inputCaption.getText() != null ? binding.inputCaption.getText().toString().trim() : "";
            String location = binding.location.getText() != null ? binding.location.getText().toString().trim() : "";
            String audioPath = binding.switchVoice.isChecked() ? audioRecorder.getCurrentAudioPath() : null;

            // Validation
            if (selectedImageUri == null && caption.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez ajouter une image ou une description.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button and show feedback
            binding.btnPublish.setEnabled(false);
            Toast.makeText(getContext(), "Publication en cours...", Toast.LENGTH_SHORT).show();

            // Prepare Files, converted by repository
            File imageFile = selectedImageUri != null ? FileUtils.getFileFromUri(requireContext(), selectedImageUri) : null;
            File audioFile = audioPath != null ? new File(audioPath) : null;

            // Repository call
            PostRepository.createPost(requireContext(), location, caption, imageFile, audioFile, selectedGroupId, new PostRepository.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    binding.btnPublish.setEnabled(true);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                    if (originalGallery != null) {
                        mainActivity.navigateTo(originalGallery, 3);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    binding.btnPublish.setEnabled(true);
                    Toast.makeText(getContext(), "Erreur : " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadUserGroups() {
        GroupRepository.fetchGroups(requireContext(), new GroupRepository.GroupCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                myGroups.clear();
                for (Group group : groups) {
                    if (group.amIMember()) {
                        myGroups.add(group);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("Erreur de chargement des groupes : " + errorMessage);
            }
        });
    }

    private void showDestinationDialog() {
        String[] options = new String[myGroups.size() + 1];
        options[0] = "Mode Public";

        for (int i = 0; i < myGroups.size(); i++) {
            options[i + 1] = myGroups.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
            .setTitle("Publier dans...")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    selectedGroupId = null;
                    binding.textDestination.setText(String.format("%s", "Mode Public"));
                    binding.iconDestination.setImageResource(R.drawable.outline_person_24);
                } else {
                    Group selectedGroup = myGroups.get(which - 1);
                    selectedGroupId = selectedGroup.getId();
                    binding.textDestination.setText(selectedGroup.getName());
                    binding.iconDestination.setImageResource(R.drawable.outline_group_24);
                }
            })
            .show();
    }

    private void fetchCurrentLocation() {
        if (LocationUtils.hasLocationPermission(requireContext())) {
            binding.location.setText(String.format("%s","Recherche de localisation..."));

            LocationUtils.getLastKnownLocation(requireContext(), location -> {
                if (location != null) {
                    String readableAddress = LocationUtils.getReadableAddress(requireContext(), location);
                    binding.location.setText(readableAddress);
                } else {
                    binding.location.setText(String.format("%s","Localisation inconnue, activez votre GPS"));
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

    private void showRecordingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_audio_recorder, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false) // Force them to use Save or Cancel buttons
                .create();

        // Start recording immediately when dialog opens
        String path = audioRecorder.startRecording(requireContext());
        if (path != null) {
            isRecording = true;
        } else {
            Toast.makeText(getContext(), "Failed to start recorder", Toast.LENGTH_SHORT).show();
            binding.switchVoice.setChecked(false);
            return;
        }

        // Cancel Button
        dialogView.findViewById(R.id.btnCancelRecord).setOnClickListener(v -> {
            audioRecorder.stopRecording();
            isRecording = false;
            // Setting this to false automatically triggers the else block in our checked listener to clean up the UI
            binding.switchVoice.setChecked(false);
            dialog.dismiss();
        });

        // Save Button
        dialogView.findViewById(R.id.btnSaveRecord).setOnClickListener(v -> {
            audioRecorder.stopRecording();
            isRecording = false;

            // Update the main UI
            binding.subtitleVoice.setText(String.format("%s","✅ Note vocale sauvegardée"));
            binding.subtitleVoice.setTextColor(Color.parseColor("#6B5B49"));
            binding.btnPreviewAudio.setVisibility(View.VISIBLE);
            resetPlayButtonUI();

            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void resetPlayButtonUI() {
        if (binding != null) {
            binding.btnPreviewAudio.setText(R.string.playVoice);
            binding.btnPreviewAudio.setIconResource(android.R.drawable.ic_media_play);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (audioPlayer != null) audioPlayer.stopPlaying();
        if (audioRecorder != null && isRecording) audioRecorder.stopRecording();
        binding = null;
    }
}