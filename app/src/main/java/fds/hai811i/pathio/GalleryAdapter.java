package fds.hai811i.pathio;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;
import fds.hai811i.pathio.utils.RetrofitClient;
import fds.hai811i.pathio.utils.TimeUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();

    public interface OnAudioPlayClickListener {
        void onAudioPlayClick(Post post, ImageView btnPlay, SeekBar seekbar, TextView txtTime);
    }
    private OnAudioPlayClickListener audioPlayClickListener;
    public void setOnAudioPlayClickListener(OnAudioPlayClickListener listener) {
        this.audioPlayClickListener = listener;
    }

    public interface OnLikeClickListener {
        void onLikeClick(Post post, int position);
    }
    private OnLikeClickListener likeClickListener;
    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.likeClickListener = listener;
    }

    public interface OnCommentClickListener {
        void onCommentClick(int postId);
    }
    private OnCommentClickListener commentClickListener;
    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.commentClickListener = listener;
    }

    public interface OnMapClickListener {
        void onMapClick(String locationName);
    }
    private OnMapClickListener mapClickListener;

    public void setOnMapClickListener(OnMapClickListener listener) {
        this.mapClickListener = listener;
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
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains("LIKE_UPDATE")) {
            Post post = posts.get(position);

            holder.likesCount.setText(String.format(Locale.getDefault(), "%d J'aime", post.getLikesCount()));

            if (post.isLikedByMe()) {
                holder.btnLike.setImageResource(R.drawable.baseline_favorite_24);
                holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#E91E63"));
            } else {
                holder.btnLike.setImageResource(R.drawable.outline_favorite_24);
                holder.btnLike.clearColorFilter();
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
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

        // --- Main picture ---
        Glide.with(holder.itemView.getContext())
                .load("https://www.zerohour.fr/" + post.getImageUrl())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .fallback(R.drawable.pathio_logo) // si photo est null ou url cassée
                .error(R.drawable.pathio_logo)
                .into(holder.postImage);

        // --- Profile picture ---
        holder.username.setText(author.getUsername());

        Glide.with(holder.itemView.getContext())
                .load("https://www.zerohour.fr/" + author.getAvatarUrl())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .fallback(R.drawable.outline_person_24) // si pp est null ou url cassée
                .error(R.drawable.outline_person_24)
                .into(holder.avatarImage);

        // --- Like button ---
        holder.btnLike.setOnClickListener(v -> {
            if (likeClickListener != null) {
                int safePosition = holder.getBindingAdapterPosition();
                if (safePosition != RecyclerView.NO_POSITION) {
                    likeClickListener.onLikeClick(post, safePosition);
                }
            }
        });

        // --- Comment button ---
        holder.btnComment.setOnClickListener(v -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(post.getId());
            }
        });

        // --- Audio playback ---
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

        // --- Location button ---
        if (post.getLocation() == null || post.getLocation().trim().isEmpty()) {
            holder.btnGoToMap.setVisibility(View.GONE);
        } else {
            holder.btnGoToMap.setVisibility(View.VISIBLE);

            holder.btnGoToMap.setOnClickListener(v -> {
                if (mapClickListener != null) {
                    mapClickListener.onMapClick(post.getLocation());
                }
            });
        }

        // --- More options button ---
        holder.btnOptions.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), v);

            popup.getMenu().add("Signaler ce post");

            popup.setOnMenuItemClickListener(item -> {
                new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                        .setTitle("Signaler le post")
                        .setMessage("Êtes-vous sûr de vouloir signaler ce contenu aux administrateurs ?")
                        .setPositiveButton("Oui, signaler", (dialog, which) -> {

                            // Appel API Retrofit
                            RetrofitClient.getApi(v.getContext()).reportPost(post.getId()).enqueue(new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(v.getContext(), "Merci, le post a été signalé.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(v.getContext(), response.message(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                    Toast.makeText(v.getContext(), "Erreur réseau.", Toast.LENGTH_SHORT).show();
                                }
                            });

                        })
                        .setNegativeButton("Annuler", null)
                        .show();
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage, postImage, btnLike, btnComment, btnPlayAudio, btnOptions;
        TextView username, location, likesCount, caption, timestamp, audioTime;
        View layoutAudioPlayer;
        SeekBar seekbarAudio;
        MaterialButton btnGoToMap;

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
            btnGoToMap = itemView.findViewById(R.id.btn_go_to_map);
            btnOptions = itemView.findViewById(R.id.btn_more_options);

            layoutAudioPlayer = itemView.findViewById(R.id.layout_audio_player);
            btnPlayAudio = itemView.findViewById(R.id.btn_play_audio);
            seekbarAudio = itemView.findViewById(R.id.seekbar_audio);
            audioTime = itemView.findViewById(R.id.txt_audio_time);
        }
    }
}