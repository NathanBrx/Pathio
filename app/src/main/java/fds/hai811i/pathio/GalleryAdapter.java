package fds.hai811i.pathio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;
import fds.hai811i.pathio.utils.TimeUtils;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();

    public interface OnAudioPlayClickListener {
        void onAudioPlayClick(Post post, ImageView btnPlay, SeekBar seekbar, TextView txtTime);
    }
    private OnAudioPlayClickListener audioPlayClickListener;

    public void setOnAudioPlayClickListener(OnAudioPlayClickListener listener) {
        this.audioPlayClickListener = listener;
    }

    public interface OnCommentClickListener {
        void onCommentClick(int postId);
    }
    private OnCommentClickListener commentClickListener;
    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.commentClickListener = listener;
    }
    public void setPosts(List<Post> newPosts) {
        PostDiffCallback diffCallback = new PostDiffCallback(this.posts, newPosts);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.posts = new ArrayList<>(newPosts);

        diffResult.dispatchUpdatesTo(this);
    }

    private static class PostDiffCallback extends DiffUtil.Callback {
        private final List<Post> oldList;
        private final List<Post> newList;

        public PostDiffCallback(List<Post> oldList, List<Post> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return (oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Post oldPost = oldList.get(oldItemPosition);
            Post newPost = newList.get(newItemPosition);

            return oldPost.getLikesCount() == newPost.getLikesCount() && oldPost.getCaption().equals(newPost.getCaption());
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        User author = post.getAuthor();

        holder.username.setText(author.getUsername());
        holder.location.setText(post.getLocation());
        holder.likesCount.setText(String.format(Locale.getDefault(), "%d %s",post.getLikesCount(), "J'aime"));
        holder.caption.setText(post.getCaption());
        String localTime = TimeUtils.getLocalTime(post.getTimestamp());
        holder.timestamp.setText(localTime);

        // charge l'image principale du post
        Glide.with(holder.itemView.getContext())
                .load("https://www.zerohour.fr/" + post.getImageUrl())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .fallback(R.drawable.pathio_logo) // si photo est null ou url cassée
                .error(R.drawable.pathio_logo)
                .into(holder.postImage);

        // charge la pp de l'auteur
        holder.username.setText(author.getUsername());

        Glide.with(holder.itemView.getContext())
                .load("https://www.zerohour.fr/" + author.getAvatarUrl())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .fallback(R.drawable.outline_person_24) // si pp est null ou url cassée
                .error(R.drawable.outline_person_24)
                .into(holder.avatarImage);

        holder.btnComment.setOnClickListener(v -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(post.getId());
            }
        });

        if (post.getAudioUrl() != null && !post.getAudioUrl().isEmpty()) {
            holder.layoutAudioPlayer.setVisibility(View.VISIBLE);

            holder.btnPlayAudio.setOnClickListener(v -> {
                if (audioPlayClickListener != null) {
                    audioPlayClickListener.onAudioPlayClick(
                            post,
                            holder.btnPlayAudio,
                            holder.seekbarAudio,
                            holder.audioTime
                    );
                }
            });
        } else {
            holder.layoutAudioPlayer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage, postImage, btnLike, btnComment, btnPlayAudio;
        TextView username, location, likesCount, caption, timestamp, audioTime;
        View layoutAudioPlayer;
        SeekBar seekbarAudio;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.txt_post_username);
            location = itemView.findViewById(R.id.txt_post_location);
            postImage = itemView.findViewById(R.id.img_post_content);
            likesCount = itemView.findViewById(R.id.txt_likes_count);
            caption = itemView.findViewById(R.id.txt_post_caption);
            timestamp = itemView.findViewById(R.id.txt_post_time);

            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);

            layoutAudioPlayer = itemView.findViewById(R.id.layout_audio_player);
            btnPlayAudio = itemView.findViewById(R.id.btn_play_audio);
            seekbarAudio = itemView.findViewById(R.id.seekbar_audio);
            audioTime = itemView.findViewById(R.id.txt_audio_time);
        }
    }
}