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

import fds.hai811i.pathio.databinding.FragmentForgotPasswordBinding;
import fds.hai811i.pathio.model.requests.ForgotPasswordRequest;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;

    public ForgotPasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new LoginFragment(), 4)
        );

        binding.btnSendEmail.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.inputEmail.getText()).toString().trim();

            if (email.isEmpty()) {
                binding.errorText.setVisibility(View.VISIBLE);
                binding.errorText.setText(String.format("%s", "Veuillez entrer votre adresse e-mail."));
                return;
            }

            binding.errorText.setVisibility(View.GONE);
            binding.btnSendEmail.setEnabled(false);

            RetrofitClient.getApi(requireContext()).forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    binding.btnSendEmail.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Si ce compte existe, un e-mail a été envoyé.", Toast.LENGTH_LONG).show();
                        ((MainActivity) requireActivity()).navigateTo(new LoginFragment(), 4);
                    } else {
                        Toast.makeText(getContext(), "Erreur de serveur", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    binding.btnSendEmail.setEnabled(true);
                    Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}