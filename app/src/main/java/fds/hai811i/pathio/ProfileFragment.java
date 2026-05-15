package fds.hai811i.pathio;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fds.hai811i.pathio.databinding.FragmentProfileBinding;
import fds.hai811i.pathio.model.Group;
import fds.hai811i.pathio.model.responses.ProfileResponse;
import fds.hai811i.pathio.utils.*;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private GroupAdapter groupAdapter;
    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uploadAvatarToServer(uri);
                } else {
                    Toast.makeText(getContext(), "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
                }
            });

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnLogout.setOnClickListener(v -> logout());
        binding.btnEditAvatar.setOnClickListener(v ->
                photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build())
        );
        binding.btnLogin.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateTo(new LoginFragment(), 4));
        binding.btnRegister.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateTo(new RegisterFragment(), 4));

        binding.btnCreateGroup.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateTo(new NewGroupFragment(), 4));

        setupGroupsRecyclerView();

        refreshProfileState();
    }

    private void setupGroupsRecyclerView() {
        groupAdapter = new GroupAdapter();
        binding.recyclerGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerGroups.setAdapter(groupAdapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshProfileState();
        }
    }

    private void refreshProfileState() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);

        if (token != null) {
            binding.loggedState.setText(String.format("%s", "Vous êtes connecté"));
            binding.btnLogin.setVisibility(View.GONE);
            binding.btnRegister.setVisibility(View.GONE);
            binding.btnLogout.setVisibility(View.VISIBLE);
            binding.btnEditAvatar.setVisibility(View.VISIBLE);

            binding.titleGroups.setVisibility(View.VISIBLE);
            binding.btnCreateGroup.setVisibility(View.VISIBLE);
            binding.recyclerGroups.setVisibility(View.VISIBLE);

            // api call for user info
            RetrofitClient.getApi(requireContext()).profile().enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        binding.userId.setText(response.body().getUser().getUsername());
                        String avatarUrl = response.body().getUser().getAvatarUrl();

                        sharedPreferences.edit().putString("user_avatar", avatarUrl).apply();

                        if (avatarUrl != null) {
                            Glide.with(requireContext())
                                    .load("https://www.zerohour.fr" + avatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.outline_person_24)
                                    .error(R.drawable.outline_person_24)
                                    .into(binding.profilePic);
                        }
                    } else {
                        Toast.makeText(getContext(), "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();
                        logoutSilent();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                    System.err.println("Profile fetch failed: " + t.getMessage());
                    Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            });

            fetchGroups();
        } else {
            binding.userId.setText(String.format("%s", "Anonymous"));
            binding.loggedState.setText(String.format("%s", "Vous n'êtes pas connecté"));
            binding.btnLogin.setVisibility(View.VISIBLE);
            binding.btnRegister.setVisibility(View.VISIBLE);
            binding.btnLogout.setVisibility(View.GONE);
            binding.btnEditAvatar.setVisibility(View.GONE);

            binding.titleGroups.setVisibility(View.GONE);
            binding.btnCreateGroup.setVisibility(View.GONE);
            binding.recyclerGroups.setVisibility(View.GONE);
            if (groupAdapter != null) groupAdapter.submitList(new ArrayList<>());

            Glide.with(requireContext()).clear(binding.profilePic);
            binding.profilePic.setImageResource(R.drawable.outline_person_24);
        }
    }

    private void fetchGroups() {
        RetrofitClient.getApi(requireContext()).getGroups().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Group>> call, @NonNull Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupAdapter.submitList(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Group>> call, @NonNull Throwable t) {
                System.err.println("Failed to fetch groups: " + t.getMessage());
            }
        });
    }

    private void uploadAvatarToServer(Uri imageUri) {
        File imageFile = FileUtils.getFileFromUri(requireContext(), imageUri);
        if (imageFile == null) {
            Toast.makeText(getContext(), "Erreur lors de la lecture de l'image", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part requestObject = ImageUploader.createAvatarRequest(imageFile);

        Toast.makeText(getContext(), "Envoi en cours...", Toast.LENGTH_SHORT).show();

        // appel api pour upload l'avatar
        RetrofitClient.getApi(requireContext()).uploadAvatar(requestObject).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Avatar mis à jour avec succès !", Toast.LENGTH_SHORT).show();

                    refreshProfileState();
                } else {
                    Toast.makeText(getContext(), "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                System.err.println("Upload failed: " + t.getMessage());
                Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void logout() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                logoutSilent();
                Toast.makeText(getContext(), "Déconnecté avec succès", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Non", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    private void logoutSilent() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("jwt_token").apply();
        sharedPreferences.edit().remove("user_avatar").apply();
        refreshProfileState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}