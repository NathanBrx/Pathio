package fds.hai811i.pathio;

import android.content.Context;
import android.content.SharedPreferences;
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
import fds.hai811i.pathio.model.Group;
import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.repositories.GroupRepository;
import fds.hai811i.pathio.model.repositories.PostRepository;
import fds.hai811i.pathio.utils.AudioPlayerUtils;
import fds.hai811i.pathio.utils.ViewUtils;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    private GalleryAdapter galleryAdapter;
    private GroupAdapter groupAdapter;
    private AudioPlayerUtils audioPlayerUtils;
    private String currentPlayingUrl = null;
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private MainActivity mainActivity;
    private boolean isPhotosTabActive = true;

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

        mainActivity = (MainActivity) requireActivity();

        binding.btnNewPost.setOnClickListener(v -> mainActivity.navigateTo(new NewPostFragment(), 3));
        binding.btnFilterPhotos.setOnClickListener(v -> showPhotosTab());
        binding.btnFilterGroups.setOnClickListener(v -> showGroupsTab());

        setupRecyclerView();

        showPhotosTab();
    }
    private void setupRecyclerView() {
        galleryAdapter = new GalleryAdapter();
        groupAdapter = new GroupAdapter();

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        Fragment originalProfile = mainActivity.getExistingFragment(ProfileFragment.class);

        galleryAdapter.setOnLikeClickListener((post, position) -> {
            if (token == null) {
                Toast.makeText(getContext(), "Connectez-vous pour aimer un post !", Toast.LENGTH_SHORT).show();
                mainActivity.navigateTo(originalProfile, 4);
                return;
            }

            boolean isCurrentlyLiked = post.isLikedByMe();
            int currentLikes = post.getLikesCount();

            post.setLikedByMe(!isCurrentlyLiked);
            post.setLikesCount(isCurrentlyLiked ? currentLikes - 1 : currentLikes + 1);
            galleryAdapter.notifyItemChanged(position, "LIKE_UPDATE");

            PostRepository.toggleLike(requireContext(), post.getId(), new PostRepository.ActionCallback() {
                @Override
                public void onSuccess(String message) {}

                @Override
                public void onError(String errorMessage) {
                    revertLikeState(post, position, isCurrentlyLiked, currentLikes);
                    Toast.makeText(getContext(), "Erreur réseau : " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        galleryAdapter.setOnCommentClickListener(postId -> {
            if (token == null) {
                Toast.makeText(getContext(), "Connectez-vous pour commenter un post !", Toast.LENGTH_SHORT).show();
                mainActivity.navigateTo(originalProfile, 4);
                return;
            }

            CommentsFragment commentsFragment = new CommentsFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("postId", postId);
            commentsFragment.setArguments(bundle);

            ((MainActivity) requireActivity()).navigateTo(commentsFragment, 3);
        });

        galleryAdapter.setOnAudioPlayClickListener((post, btnPlay, seekBar, txtTime) -> {
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

        galleryAdapter.setOnMapClickListener(locationName -> {
            MapFragment mapFragment = new MapFragment();

            Bundle bundle = new Bundle();
            bundle.putString("target_location", locationName);
            mapFragment.setArguments(bundle);

            ((MainActivity) requireActivity()).navigateTo(mapFragment, 1);
        });

        binding.recyclerGallery.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void showPhotosTab() {
        isPhotosTabActive = true;
        ViewUtils.switchTabs(binding.btnFilterPhotos, binding.btnFilterGroups);

        binding.recyclerGallery.setAdapter(galleryAdapter);
        fetchPosts();
    }

    private void showGroupsTab() {
        isPhotosTabActive = false;
        ViewUtils.switchTabs(binding.btnFilterGroups, binding.btnFilterPhotos);

        binding.recyclerGallery.setAdapter(groupAdapter);
        fetchGroups();
    }

    private void fetchPosts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerGallery.setVisibility(View.GONE);

        PostRepository.getPosts(requireContext(), new PostRepository.PostsCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerGallery.setVisibility(View.VISIBLE);

                galleryAdapter.setPosts(posts);
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);

                System.err.println("Failed to fetch posts: " + errorMessage);
                Toast.makeText(getContext(), "Erreur : " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGroups() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerGallery.setVisibility(View.GONE);

        GroupRepository.fetchGroups(requireContext(), new GroupRepository.GroupCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerGallery.setVisibility(View.VISIBLE);

                groupAdapter.submitList(groups);
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);
                System.err.println("Failed to fetch groups: " + errorMessage);
                Toast.makeText(getContext(), "Erreur réseau: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revertLikeState(Post post, int position, boolean originalLikeState, int originalLikesCount) {
        post.setLikedByMe(originalLikeState);
        post.setLikesCount(originalLikesCount);
        galleryAdapter.notifyItemChanged(position);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (isPhotosTabActive) {
                fetchPosts();
            } else {
                fetchGroups();
            }
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