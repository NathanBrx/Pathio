package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

import fds.hai811i.pathio.databinding.FragmentItineraryListBinding;
import fds.hai811i.pathio.model.Itinerary;

import fds.hai811i.pathio.model.UserPreferences;
import fds.hai811i.pathio.utils.ItineraryGenerator;

import android.widget.Toast;
import fds.hai811i.pathio.model.responses.OverpassResponse;
import fds.hai811i.pathio.model.POI;
import fds.hai811i.pathio.utils.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItineraryListFragment extends Fragment {
    private FragmentItineraryListBinding binding;
    private UserPreferences prefs;

    public ItineraryListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItineraryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            Fragment originalNewPath = ((MainActivity) requireActivity()).getExistingFragment(NewPathFragment.class);
            if (originalNewPath != null) {
                ((MainActivity) requireActivity()).navigateTo(originalNewPath, 0);
            }
        });

        if (getArguments() != null) {
            prefs = (UserPreferences) getArguments().getSerializable("preferences");
        }

        if (prefs != null) {
            fetchRealPOIsAndGenerate();
        } else {
            showDemoItineraries();
        }
    }

    private void fetchRealPOIsAndGenerate() {
        binding.loadingLayout.setVisibility(View.VISIBLE);
        binding.recyclerViewItineraries.setVisibility(View.GONE);

        // For now, use Montpellier center as default location if no GPS
        double lat = 43.6107;
        double lon = 3.8767;

        String query = ItineraryGenerator.buildOverpassQuery(lat, lon, 3000, prefs.categories);
        android.util.Log.d("OverpassQuery", query);
        
        RetrofitClient.getOverpassApi().getPOIs(query).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (getContext() == null) return;
                binding.loadingLayout.setVisibility(View.GONE);
                binding.recyclerViewItineraries.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("OverpassResponse", "Found " + response.body().elements.size() + " elements");
                    List<POI> database = new ArrayList<>();
                    for (OverpassResponse.Element element : response.body().elements) {
                        database.add(ItineraryGenerator.fromOverpass(element));
                    }
                    displayItineraries(ItineraryGenerator.generate(prefs, database));
                } else {
                    String errorMsg = "Erreur API: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    android.util.Log.e("OverpassError", errorMsg);
                    Toast.makeText(getContext(), "Erreur API Overpass (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    showDemoItineraries();
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                if (getContext() == null) return;
                android.util.Log.e("OverpassFailure", "Network error", t);
                binding.loadingLayout.setVisibility(View.GONE);
                binding.recyclerViewItineraries.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showDemoItineraries();
            }
        });
    }

    private void displayItineraries(List<Itinerary> itineraries) {
        binding.recyclerViewItineraries.setLayoutManager(new LinearLayoutManager(getContext()));
        ItineraryAdapter adapter = new ItineraryAdapter(itineraries, itinerary -> {
            MapFragment mapFragment = new MapFragment();
            Bundle mapArgs = new Bundle();
            mapArgs.putSerializable("itinerary", itinerary);
            mapFragment.setArguments(mapArgs);
            ((MainActivity) requireActivity()).navigateTo(mapFragment, 1);
        });
        binding.recyclerViewItineraries.setAdapter(adapter);
    }

    private void showDemoItineraries() {
        List<Itinerary> myItineraries = new ArrayList<>();
        myItineraries.add(new Itinerary("Économique (Démo)", "45MIN", "3.2KM", "GRATUIT"));
        myItineraries.add(new Itinerary("Équilibré (Démo)", "1.5HR", "5.8KM", "30.00"));
        myItineraries.add(new Itinerary("Confort (Démo)", "2.5HR", "8.1KM", "75.00"));
        displayItineraries(myItineraries);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}