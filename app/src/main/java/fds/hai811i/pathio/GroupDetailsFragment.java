package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.Locale;

import fds.hai811i.pathio.databinding.FragmentGroupDetailsBinding;
import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;
import fds.hai811i.pathio.model.repositories.GroupRepository;
import fds.hai811i.pathio.utils.ViewUtils;

public class GroupDetailsFragment extends Fragment {
    private FragmentGroupDetailsBinding binding;
    private MainActivity mainActivity;
    private ProfileFragment originalProfile;
    private int groupId;
    private MemberAdapter memberAdapter;
    private GalleryAdapter galleryAdapter;
    private boolean isMembersTabActive = true;

    public GroupDetailsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
        originalProfile = mainActivity.getExistingFragment(ProfileFragment.class);

        if (getArguments() != null) {
            groupId = getArguments().getInt("group_id", -1);
            String groupName = getArguments().getString("group_name", "Détails du groupe");
            String groupDesc = getArguments().getString("group_desc", "");
            int memberCount = getArguments().getInt("group_member_count", 0);
            boolean isMemberMe = getArguments().getBoolean("is_member_me", false);

            binding.txtGroupName.setText(groupName);
            binding.txtGroupDescription.setText(groupDesc);
            binding.txtGroupMembersCount.setText(String.format(Locale.getDefault(), "%d%s", memberCount, (memberCount > 1 ? " membres" : " membre")));
            updateUI(isMemberMe);
        }

        if (groupId == -1) {
            Toast.makeText(getContext(), "Erreur de chargement du groupe", Toast.LENGTH_SHORT).show();
            returnToProfile();
            return;
        }

        binding.btnBack.setOnClickListener(v -> {
            returnToProfile();
        });

        binding.btnJoinLeaveGroup.setOnClickListener(v -> toggleGroupMembership());

        binding.btnTabMembers.setOnClickListener(v -> showMembersTab());
        binding.btnTabPosts.setOnClickListener(v -> showPhotosTab());

        setupRecyclerView();

        showMembersTab();
    }

    private void setupRecyclerView() {
        binding.recyclerGroupContent.setLayoutManager(new LinearLayoutManager(requireContext()));
        memberAdapter = new MemberAdapter();
        galleryAdapter = new GalleryAdapter();
    }

    private void showPhotosTab() {
        isMembersTabActive = false;
        ViewUtils.switchTabs(binding.btnTabPosts, binding.btnTabMembers);

        binding.recyclerGroupContent.setAdapter(galleryAdapter);
        fetchPosts();
    }

    private void showMembersTab() {
        isMembersTabActive = true;
        ViewUtils.switchTabs(binding.btnTabMembers, binding.btnTabPosts);

        binding.recyclerGroupContent.setAdapter(memberAdapter);
        fetchGroupMembers();
    }

    private void updateUI(boolean amIMember) {
        if (amIMember) {
            binding.btnJoinLeaveGroup.setText(String.format("%s", "Quitter le groupe"));
            binding.btnJoinLeaveGroup.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E2E8F0")));
            binding.btnJoinLeaveGroup.setTextColor(android.graphics.Color.parseColor("#4A5568"));
        } else {
            binding.btnJoinLeaveGroup.setText(String.format("%s", "Rejoindre le groupe"));
            binding.btnJoinLeaveGroup.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#B84B25")));
            binding.btnJoinLeaveGroup.setTextColor(android.graphics.Color.WHITE);
        }
    }

    private void toggleGroupMembership() {
        binding.btnJoinLeaveGroup.setEnabled(false);

        boolean isCurrentlyMember = binding.btnJoinLeaveGroup.getText().toString().toLowerCase().contains("quitter");

        GroupRepository.joinLeaveGroup(requireContext(), groupId, new GroupRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                binding.btnJoinLeaveGroup.setEnabled(true);

                boolean newMembershipState = !isCurrentlyMember;
                updateUI(newMembershipState);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                fetchGroupMembers();
            }

            @Override
            public void onError(String errorMessage) {
                binding.btnJoinLeaveGroup.setEnabled(true);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToProfile() {
        if (originalProfile != null) {
            mainActivity.navigateTo(originalProfile, 4);
        }
    }

    private void fetchGroupMembers() {
        GroupRepository.getGroupMembers(requireContext(), groupId, new GroupRepository.MembersCallback() {
            @Override
            public void onSuccess(List<User> members) {
                memberAdapter.submitList(members);

                int newCount = members.size();
                binding.txtGroupMembersCount.setText(String.format(Locale.getDefault(), "%d%s", newCount, (newCount > 1 ? " membres" : " membre")));
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void fetchPosts() {
        binding.recyclerGroupContent.setVisibility(View.GONE);

        GroupRepository.getGroupPosts(requireContext(), groupId, new GroupRepository.GroupPostsCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                binding.recyclerGroupContent.setVisibility(View.VISIBLE);
                galleryAdapter.setPosts(posts);
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("Failed to fetch posts: " + errorMessage);
                Toast.makeText(getContext(), "Erreur : " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}