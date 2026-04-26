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

public class ItineraryListFragment extends Fragment {
    private FragmentItineraryListBinding binding;

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

        List<Itinerary> myItineraries = new ArrayList<>();
        myItineraries.add(new Itinerary("Économique", "45MIN", "3.2KM", "GRATUIT"));
        myItineraries.add(new Itinerary("Équilibré", "1.5HR", "5.8KM", "30.00"));
        myItineraries.add(new Itinerary("Confort", "2.5HR", "8.1KM", "75.00"));

        binding.recyclerViewItineraries.setLayoutManager(new LinearLayoutManager(getContext()));

        ItineraryAdapter adapter = new ItineraryAdapter(myItineraries);
        binding.recyclerViewItineraries.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}