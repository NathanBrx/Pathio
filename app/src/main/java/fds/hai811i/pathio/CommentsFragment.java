package fds.hai811i.pathio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fds.hai811i.pathio.databinding.FragmentCommentsBinding;
import fds.hai811i.pathio.model.Comment;
import fds.hai811i.pathio.model.requests.CommentRequest;
import fds.hai811i.pathio.model.responses.CommentsResponse;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsFragment extends Fragment {
    private FragmentCommentsBinding binding;
    private CommentAdapter adapter;
    private int currentPostId = -1;

    public CommentsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentPostId = getArguments().getInt("postId", -1);
        }

        setupRecyclerView();

        if (currentPostId != -1) {
            loadComments();
        } else {
            Toast.makeText(getContext(), "Erreur : Post introuvable", Toast.LENGTH_SHORT).show();
        }

        MainActivity mainActivity = (MainActivity) requireActivity();
        Fragment originalGallery = mainActivity.getExistingFragment(GalleryFragment.class);

        binding.btnBack.setOnClickListener(v -> {
            if (originalGallery != null) {
                mainActivity.navigateTo(originalGallery, 3);
            }
        });

        binding.btnSendComment.setOnClickListener(v -> {
            String content = binding.inputComment.getText() != null ? binding.inputComment.getText().toString().trim() : "";
            if (!content.isEmpty() && currentPostId != -1) {
                postComment(content);
            } else {
                Toast.makeText(getContext(), "Le commentaire est vide", Toast.LENGTH_SHORT).show();
            }
        });

        loadCurrentUserProfile();
    }

    private void setupRecyclerView() {
        List<Comment> commentList = new ArrayList<>();
        adapter = new CommentAdapter(commentList);
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerComments.setAdapter(adapter);
    }

    private void loadCurrentUserProfile() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String savedAvatarUrl = sharedPreferences.getString("user_avatar", null);

        if (savedAvatarUrl != null && !savedAvatarUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load("https://www.zerohour.fr" + savedAvatarUrl)
                    .centerCrop()
                    .placeholder(R.drawable.outline_person_24)
                    .error(R.drawable.outline_person_24)
                    .into(binding.currentUserAvatar);
        }
    }

    private void loadComments() {
        RetrofitClient.getApi(requireContext()).getComments(currentPostId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CommentsResponse> call, @NonNull Response<CommentsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setComments(response.body().getComments());

                    if (!response.body().getComments().isEmpty()) {
                        binding.recyclerComments.smoothScrollToPosition(0);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommentsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Erreur de chargement des commentaires", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment(String content) {
        binding.btnSendComment.setEnabled(false); // évite le double-clic

        CommentRequest request = new CommentRequest(content);

        RetrofitClient.getApi(requireContext()).addComment(currentPostId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                binding.btnSendComment.setEnabled(true);

                if (response.isSuccessful()) {
                    binding.inputComment.setText(""); // vider input texte
                    loadComments(); // Reload liste de commentaires
                } else {
                    Toast.makeText(getContext(), "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                binding.btnSendComment.setEnabled(true);
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