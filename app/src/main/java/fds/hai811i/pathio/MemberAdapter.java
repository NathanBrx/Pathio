package fds.hai811i.pathio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import fds.hai811i.pathio.model.User;

public class MemberAdapter extends ListAdapter<User, MemberAdapter.MemberViewHolder> {
    public MemberAdapter() {
        super(new UserDiffCallback());
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = getItem(position);
        holder.memberName.setText(member.getUsername());

        Glide.with(holder.itemView.getContext())
                .load("https://www.zerohour.fr" + member.getAvatarUrl())
                .circleCrop()
                .placeholder(R.drawable.outline_person_24)
                .error(R.drawable.outline_person_24)
                .into(holder.memberAvatar);
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView memberAvatar;
        TextView memberName;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberAvatar = itemView.findViewById(R.id.imgMemberAvatar);
            memberName = itemView.findViewById(R.id.txtMemberName);
        }
    }

    static class UserDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUsername().equals(newItem.getUsername());
        }
    }
}