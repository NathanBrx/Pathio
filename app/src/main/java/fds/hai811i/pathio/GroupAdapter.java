package fds.hai811i.pathio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import fds.hai811i.pathio.model.Group;

public class GroupAdapter extends ListAdapter<Group, GroupAdapter.GroupViewHolder> {

    public GroupAdapter() {
        super(new GroupDiffCallback());
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = getItem(position);

        holder.groupName.setText(group.getName());

        String membersText = group.getMemberCount() <= 1
                ? group.getMemberCount() + " membre"
                : group.getMemberCount() + " membres";
        holder.groupMembers.setText(membersText);

        holder.itemView.setOnClickListener(v -> {
            GroupDetailsFragment detailsFragment = new GroupDetailsFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("group_id", group.getId());
            bundle.putString("group_name", group.getName());
            bundle.putString("group_desc", group.getDescription());
            bundle.putInt("group_member_count", group.getMemberCount());
            bundle.putBoolean("is_member_me", group.amIMember());
            detailsFragment.setArguments(bundle);

            ((MainActivity) v.getContext()).navigateTo(detailsFragment, 4);
        });
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, groupMembers;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.txtGroupName);
            groupMembers = itemView.findViewById(R.id.txtGroupMembers);
        }
    }

    static class GroupDiffCallback extends DiffUtil.ItemCallback<Group> {
        @Override
        public boolean areItemsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getMemberCount() == newItem.getMemberCount();
        }
    }
}