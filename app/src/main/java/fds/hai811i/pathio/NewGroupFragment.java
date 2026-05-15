package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fds.hai811i.pathio.databinding.FragmentNewGroupBinding;
import fds.hai811i.pathio.model.requests.GroupRequest;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewGroupFragment extends Fragment {
    private FragmentNewGroupBinding binding;
    MainActivity mainActivity;
    Fragment originalProfile;

    public NewGroupFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
        originalProfile = mainActivity.getExistingFragment(ProfileFragment.class);

        binding.btnBack.setOnClickListener(v -> {
            if (originalProfile != null) {
                mainActivity.navigateTo(originalProfile, 4);
            }
        });

        binding.btnSubmitGroup.setOnClickListener(v -> attemptCreateGroup());
    }

    private void attemptCreateGroup() {
        String name = binding.inputGroupName.getText() != null ? binding.inputGroupName.getText().toString().trim() : "";
        String description = binding.inputGroupDesc.getText() != null ? binding.inputGroupDesc.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.inputGroupName.setError("Le nom du groupe est requis");
            return;
        } else if (description.isEmpty()) {
            binding.inputGroupDesc.setError("La description est requise");
            return;
        }

        binding.btnSubmitGroup.setEnabled(false);
        binding.btnSubmitGroup.setText(String.format("%s","Création en cours..."));

        GroupRequest request = new GroupRequest(name, description);

        RetrofitClient.getApi(requireContext()).createGroup(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                binding.btnSubmitGroup.setEnabled(true);
                binding.btnSubmitGroup.setText(R.string.createGroup);

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Groupe créé avec succès !", Toast.LENGTH_SHORT).show();
                    if (originalProfile != null) {
                        mainActivity.navigateTo(originalProfile, 4);
                    }
                } else {
                    Toast.makeText(getContext(), response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                binding.btnSubmitGroup.setEnabled(true);
                binding.btnSubmitGroup.setText(R.string.createGroup);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}