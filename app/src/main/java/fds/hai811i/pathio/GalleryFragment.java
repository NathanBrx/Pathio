package fds.hai811i.pathio;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.Locale;

import fds.hai811i.pathio.databinding.FragmentGalleryBinding;
import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.utils.AudioPlayerUtils;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    private GalleryAdapter adapter;
    private AudioPlayerUtils audioPlayerUtils;
    private String currentPlayingUrl = null;
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;

    public GalleryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        audioPlayerUtils = new AudioPlayerUtils();

        binding.btnNewPost.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateTo(new NewPostFragment(), 3));

        setupRecyclerView();

        fetchPosts();
    }
    private void setupRecyclerView() {
        adapter = new GalleryAdapter();

        adapter.setOnLikeClickListener((post, position) -> {
            boolean isCurrentlyLiked = post.isLikedByMe();
            int currentLikes = post.getLikesCount();

            post.setLikedByMe(!isCurrentlyLiked);
            post.setLikesCount(isCurrentlyLiked ? currentLikes - 1 : currentLikes + 1);

            adapter.notifyItemChanged(position, "LIKE_UPDATE");

            // --- APPEL API ---
            RetrofitClient.getApi(requireContext()).toggleLike(post.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        revertLikeState(post, position, isCurrentlyLiked, currentLikes);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    revertLikeState(post, position, isCurrentlyLiked, currentLikes);
                    Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            });
        });

        adapter.setOnCommentClickListener(postId -> {
            CommentsFragment commentsFragment = new CommentsFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("postId", postId);
            commentsFragment.setArguments(bundle);

            ((MainActivity) requireActivity()).navigateTo(commentsFragment, 3);
        });

        adapter.setOnAudioPlayClickListener((post, btnPlay, seekBar, txtTime) -> {
            String fullUrl = "https://www.zerohour.fr/" + post.getAudioUrl();

            // PAUSE
            if (fullUrl.equals(currentPlayingUrl) && audioPlayerUtils.isPlaying()) {
                audioPlayerUtils.pausePlaying();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            }
            // RESUME
            else if (fullUrl.equals(currentPlayingUrl) && !audioPlayerUtils.isPlaying()) {
                audioPlayerUtils.resumePlaying();
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
            }
            // START
            else {
                audioPlayerUtils.stopPlaying();
                if (progressRunnable != null) progressHandler.removeCallbacks(progressRunnable);

                currentPlayingUrl = fullUrl;
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);

                audioPlayerUtils.startPlaying(fullUrl, () -> {
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                    seekBar.setProgress(0);
                    txtTime.setText(String.format("%s","0:00"));
                    currentPlayingUrl = null;
                    progressHandler.removeCallbacks(progressRunnable);
                });

                // Seekbar et temps écoulé
                progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (audioPlayerUtils.isPlaying()) {
                            int currentMs = audioPlayerUtils.getCurrentPosition();
                            int totalMs = audioPlayerUtils.getDuration();

                            seekBar.setMax(totalMs);
                            seekBar.setProgress(currentMs);

                            // millisecondes -> M:SS
                            int seconds = (currentMs / 1000) % 60;
                            int minutes = (currentMs / 1000) / 60;
                            txtTime.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));

                            // check each 100 milliseconds
                            progressHandler.postDelayed(this, 100);
                        }
                    }
                };
                progressHandler.postDelayed(progressRunnable, 100);

                // user can seek a time on the seekbar
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            audioPlayerUtils.seekTo(progress);
                        }
                    }
                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }
        });

        adapter.setOnMapClickListener(locationName -> {
            MapFragment mapFragment = new MapFragment();

            Bundle bundle = new Bundle();
            bundle.putString("target_location", locationName);
            mapFragment.setArguments(bundle);

            ((MainActivity) requireActivity()).navigateTo(mapFragment, 1);
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

    private void revertLikeState(Post post, int position, boolean originalLikeState, int originalLikesCount) {
        post.setLikedByMe(originalLikeState);
        post.setLikesCount(originalLikesCount);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            fetchPosts();
        } else {
            if (audioPlayerUtils != null) {
                audioPlayerUtils.stopPlaying();
                currentPlayingUrl = null;
            }
            if (progressHandler != null) {
                progressHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (audioPlayerUtils != null) {
            audioPlayerUtils.stopPlaying();
        }
        if (progressHandler != null) {
            progressHandler.removeCallbacksAndMessages(null);
        }
        binding = null;
    }
}