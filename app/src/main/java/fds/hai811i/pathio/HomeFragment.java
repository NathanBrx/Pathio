package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fds.hai811i.pathio.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) requireActivity();

        binding.travelPathCard.setOnClickListener(v -> {
            Fragment originalNewPath = mainActivity.getExistingFragment(NewPathFragment.class);
            if (originalNewPath != null) {
                mainActivity.navigateTo(originalNewPath, 0);
            }
        });

        binding.travelShareCard.setOnClickListener(v -> {
            Fragment originalGallery = mainActivity.getExistingFragment(GalleryFragment.class);
            if (originalGallery != null) {
                mainActivity.navigateTo(originalGallery, 3);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}