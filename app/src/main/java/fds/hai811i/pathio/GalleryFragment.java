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

import java.util.ArrayList;
import java.util.List;

import fds.hai811i.pathio.databinding.FragmentGalleryBinding;
import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;
import fds.hai811i.pathio.utils.LocalRepository;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchPosts(); // Refresh list for standard lifecycle
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            fetchPosts(); // Refresh list when shown via hide/show (MainActivity logic)
        }
    }

    private void setupRecyclerView() {
        adapter = new GalleryAdapter();
        adapter.setOnSocialInteractionListener(new GalleryAdapter.OnSocialInteractionListener() {
            @Override
            public void onCommentClicked(Post post) {
                Toast.makeText(getContext(), "Affichage des commentaires pour: " + post.getCaption(), Toast.LENGTH_SHORT).show();
            }
@Override
public void onGoToMapClicked(Post post) {
    // Bridge: From Post to Map
    Toast.makeText(getContext(), "Calcul de l'itinéraire vers " + post.getLocation(), Toast.LENGTH_SHORT).show();

    // We go to the Map Tab
    MainActivity mainActivity = (MainActivity) requireActivity();

    // We create a new MapFragment (or clear arguments of existing one)
    MapFragment mapFragment = new MapFragment();
    Bundle args = new Bundle();
    args.putString("target_location", post.getLocation());
    mapFragment.setArguments(args);

    mainActivity.navigateTo(mapFragment, 1);
}

        });
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
                binding.recyclerGallery.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    adapter.setPosts(response.body());
                } else {
                    // Fallback to mock data if API is empty or fails
                    loadMockPosts();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerGallery.setVisibility(View.VISIBLE);
                loadMockPosts();
                Toast.makeText(getContext(), "Mode hors-ligne : Affichage des posts locaux", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMockPosts() {
        List<Post> posts = LocalRepository.getInstance().getPosts();
        adapter.setPosts(posts);
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
