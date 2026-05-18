package fds.hai811i.pathio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fds.hai811i.pathio.model.Comment;
import fds.hai811i.pathio.utils.TimeUtils;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setComments(List<Comment> newComments) {
        CommentDiffCallback diffCallback = new CommentDiffCallback(this.comments, newComments);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.comments = new ArrayList<>(newComments);

        diffResult.dispatchUpdatesTo(this);
    }

    private static class CommentDiffCallback extends DiffUtil.Callback {
        private final List<Comment> oldList;
        private final List<Comment> newList;

        public CommentDiffCallback(List<Comment> oldList, List<Comment> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Comment oldComment = oldList.get(oldItemPosition);
            Comment newComment = newList.get(newItemPosition);
            return oldComment.getText().equals(newComment.getText());
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.username.setText(comment.getUsername());
        holder.text.setText(comment.getText());
        String localTime = TimeUtils.getLocalTime(comment.getTimestamp());
        holder.time.setText(localTime);

        Glide.with(holder.itemView.getContext())
                .load("https://www.zerohour.fr" + comment.getAvatarUrl())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .fallback(R.drawable.outline_person_24) // si pp est null ou url cassée
                .error(R.drawable.outline_person_24)
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username, text, time;
        ImageView avatar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.commentUsername);
            text = itemView.findViewById(R.id.commentText);
            time = itemView.findViewById(R.id.commentTime);
            avatar = itemView.findViewById(R.id.commentAvatar);
        }
    }
}