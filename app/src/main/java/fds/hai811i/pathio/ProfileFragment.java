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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import fds.hai811i.pathio.databinding.FragmentProfileBinding;
import fds.hai811i.pathio.model.responses.ProfileResponse;
import fds.hai811i.pathio.utils.ImageUploader;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadAvatarToServer(uri);
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
        binding.btnEditAvatar.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        binding.btnLogin.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateTo(new LoginFragment(), 4));
        binding.btnRegister.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateTo(new RegisterFragment(), 4));

        refreshProfileState();
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

            // api call for user info
            RetrofitClient.getApi(requireContext()).profile().enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        binding.userId.setText(response.body().getUser().getUsername());
                        String avatarUrl = response.body().getUser().getAvatarUrl();

                        if (avatarUrl != null) {
                            Glide.with(requireContext())
                                    .load("https://www.zerohour.fr" + avatarUrl)
                                    .circleCrop()
                                    .into(binding.profilePic);
                        }
                    } else {
                        Toast.makeText(getContext(), "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();
                        logout();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                    System.err.println("Profile fetch failed: " + t.getMessage());
                    Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            binding.userId.setText(String.format("%s", "Anonymous"));
            binding.loggedState.setText(String.format("%s", "Vous n'êtes pas connecté"));
            binding.btnLogin.setVisibility(View.VISIBLE);
            binding.btnRegister.setVisibility(View.VISIBLE);
            binding.btnLogout.setVisibility(View.GONE);
            binding.btnEditAvatar.setVisibility(View.GONE);

            Glide.with(requireContext()).clear(binding.profilePic);
            binding.profilePic.setImageResource(R.drawable.outline_person_24);
        }
    }

    private void uploadAvatarToServer(Uri imageUri) {
        File imageFile = getFileFromUri(imageUri);
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

    /**
     * Méthode d'aide pour transformer une uri android (quand on sélectionne une photo) en vrai fichier
     * @param uri L'uri android
     * @return L'image si réussite, null sinon
     */
    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload_avatar", ".jpg", requireContext().getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while (true) {
                assert inputStream != null;
                if (!((length = inputStream.read(buffer)) > 0)) break;
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    private void logout() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("jwt_token").apply();

                Toast.makeText(getContext(), "Déconnecté avec succès", Toast.LENGTH_SHORT).show();

                refreshProfileState();
            })
            .setNegativeButton("Non", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}