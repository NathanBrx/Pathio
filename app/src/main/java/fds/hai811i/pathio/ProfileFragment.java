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

import fds.hai811i.pathio.databinding.FragmentProfileBinding;
import fds.hai811i.pathio.model.responses.ProfileResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

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

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);

        if (token != null) {
            binding.loggedState.setText(String.format("%s", "Vous êtes connecté"));

            binding.btnLogin.setVisibility(View.GONE);
            binding.btnRegister.setVisibility(View.GONE);

            binding.btnLogout.setVisibility(View.VISIBLE);
            binding.btnLogout.setOnClickListener(v -> logout());

            // Api call to retrieve user info
            RetrofitClient.getApi(requireContext()).profile().enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        String username = response.body().getUser().getUsername();

                        binding.userId.setText(username);

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
        }

        binding.btnLogin.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new LoginFragment(), 4)
        );

        binding.btnRegister.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new RegisterFragment(), 4)
        );
    }

    private void logout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("jwt_token").apply();

        Toast.makeText(getContext(), "Déconnecté avec succès", Toast.LENGTH_SHORT).show();

        ((MainActivity) requireActivity()).navigateTo(new ProfileFragment(), 4);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}