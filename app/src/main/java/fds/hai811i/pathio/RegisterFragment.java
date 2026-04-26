package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import fds.hai811i.pathio.databinding.FragmentRegisterBinding;
import fds.hai811i.pathio.model.requests.RegisterRequest;
import fds.hai811i.pathio.model.responses.RegisterResponse;
import fds.hai811i.pathio.utils.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;

    public RegisterFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Fragment originalProfile = ((MainActivity) requireActivity()).getExistingFragment(ProfileFragment.class);

        binding.btnBack.setOnClickListener(v -> {
            if (originalProfile != null) {
                ((MainActivity) requireActivity()).navigateTo(originalProfile, 4);
            }
        });

        binding.btnRegister.setOnClickListener(v -> {
            String username = Objects.requireNonNull(binding.inputUsername.getText()).toString().trim();
            String email = Objects.requireNonNull(binding.inputEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.inputPassword.getText()).toString().trim();
            String confirm = Objects.requireNonNull(binding.inputConfirmPassword.getText()).toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                showError("Veuillez remplir tous les champs.");
                return;
            }
            if (!password.equals(confirm)) {
                showError("Les mots de passe ne correspondent pas.");
                return;
            }

            binding.errorText.setVisibility(View.GONE);

            RetrofitClient.getApi(requireContext()).register(new RegisterRequest(email, username, password)).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        Toast.makeText(getContext(), "Inscrit avec succès !", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).navigateTo(originalProfile, 4);

                    } else {
                        Toast.makeText(getContext(), "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                    System.err.println("Registration failed: " + t.getMessage());
                    Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.linkLogin.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new LoginFragment(), 4)
        );
    }

    private void showError(String message) {
        binding.errorText.setVisibility(View.VISIBLE);
        binding.errorText.setText(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}