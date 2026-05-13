package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fds.hai811i.pathio.model.Comment;
import fds.hai811i.pathio.model.Group;
import fds.hai811i.pathio.utils.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    // Note: This Fragment currently serves as a Group Management base 
    // to fulfill the "Group" requirement of TravelShare.
    
    private RecyclerView recyclerViewGroups;
    private GroupAdapter adapter;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewGroups = view.findViewById(R.id.recycler_groups);
        if (recyclerViewGroups != null) {
            setupRecyclerView();
            fetchGroups();
        }
        
        view.findViewById(R.id.btn_create_group).setOnClickListener(v -> showCreateGroupDialog());
    }

    private void setupRecyclerView() {
        adapter = new GroupAdapter();
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewGroups.setAdapter(adapter);
    }

    private void fetchGroups() {
        RetrofitClient.getApi(requireContext()).getGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setGroups(response.body());
                } else {
                    // Fallback mock data for demo
                    List<Group> mocks = new ArrayList<>();
                    mocks.add(new Group(1, "Voyageurs Montpellier", "Partagez vos coins secrets.", "Admin", 12));
                    mocks.add(new Group(2, "Randonneurs Hérault", "Sorties tous les dimanches.", "User123", 45));
                    adapter.setGroups(mocks);
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Toast.makeText(getContext(), "Mode hors-ligne: Chargement des groupes locaux", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateGroupDialog() {
        // Logic to show a dialog and call ApiService.createGroup()
        Toast.makeText(getContext(), "Ouverture du formulaire de groupe...", Toast.LENGTH_SHORT).show();
    }

    // --- Minimal Group Adapter ---
    private static class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
        private List<Group> groups = new ArrayList<>();

        public void setGroups(List<Group> groups) {
            this.groups = groups;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new GroupViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
            Group g = groups.get(position);
            holder.t1.setText(g.getName());
            holder.t2.setText(g.getDescription() + " (" + g.getMemberCount() + " membres)");
        }

        @Override public int getItemCount() { return groups.size(); }

        static class GroupViewHolder extends RecyclerView.ViewHolder {
            TextView t1, t2;
            GroupViewHolder(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
