package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import fds.hai811i.pathio.databinding.FragmentGalleryBinding;
import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.utils.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private GalleryAdapter adapter;

    public GalleryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnNewPost.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateTo(new NewPostFragment(), 3));

        setupRecyclerView();

        fetchPosts();
    }

    private void setupRecyclerView() {
        adapter = new GalleryAdapter();
        binding.recyclerGallery.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerGallery.setAdapter(adapter);
    }

    private void fetchPosts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerGallery.setVisibility(View.GONE);

        // appel api pour récupérer les posts
        RetrofitClient.getApi(requireContext()).getPosts().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    binding.recyclerGallery.setVisibility(View.VISIBLE);
                    adapter.setPosts(response.body());
                } else {
                    Toast.makeText(getContext(), "Erreur de chargement du flux", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                System.err.println("Failed to fetch posts: " + t.getMessage());
                Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}