package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fds.hai811i.pathio.databinding.FragmentProfileBinding;

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

        binding.userId.setText(String.format("%s", "Anonymous"));
        binding.loggedState.setText(String.format("%s", "Vous n'êtes pas connecté"));

        binding.btnLogin.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new LoginFragment(), 4)
        );

        binding.btnRegister.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new RegisterFragment(), 4)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}