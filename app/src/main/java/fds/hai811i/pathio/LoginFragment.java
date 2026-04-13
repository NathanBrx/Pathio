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

import fds.hai811i.pathio.databinding.FragmentLoginBinding;
import fds.hai811i.pathio.model.LoginRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new ProfileFragment(), 4)
        );

        binding.btnLogin.setOnClickListener(v -> {
            String identifier = Objects.requireNonNull(binding.inputUsername.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.inputPassword.getText()).toString().trim();

            if (identifier.isEmpty() || password.isEmpty()) {
                binding.errorText.setVisibility(View.VISIBLE);
                binding.errorText.setText(String.format("%s", "Veuillez remplir tous les champs."));
                return;
            }

            binding.errorText.setVisibility(View.GONE);

            RetrofitClient.getApi().login(new LoginRequest(identifier, password))
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Connecté avec succès !", Toast.LENGTH_SHORT).show();
                                    ((MainActivity) requireActivity()).navigateTo(new ProfileFragment(), 4);
                                });
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Erreur lors de la connexion", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            System.err.println(t.getMessage());
                        }
                    });
        });

        binding.linkRegister.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new RegisterFragment(), 4)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}