package fds.hai811i.pathio;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();

    public interface OnSocialInteractionListener {
        void onCommentClicked(Post post);
        void onGoToMapClicked(Post post);
    }

    private OnSocialInteractionListener listener;

    public void setOnSocialInteractionListener(OnSocialInteractionListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> newPosts) {
        PostDiffCallback diffCallback = new PostDiffCallback(this.posts, newPosts);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.posts = new ArrayList<>(newPosts);
        diffResult.dispatchUpdatesTo(this);
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
        updateLikesUI(holder, post);
        holder.caption.setText(post.getCaption());
        holder.timestamp.setText(post.getTimestamp());

        String imageUrl = post.getImageUrl();
        Object loadTarget = null;

        if (imageUrl != null) {
            if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                loadTarget = android.net.Uri.parse(imageUrl);
            } else {
                loadTarget = "https://www.zerohour.fr/" + imageUrl;
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(loadTarget)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .fallback(R.drawable.pathio_logo)
                .error(R.drawable.pathio_logo)
                .into(holder.postImage);

        Glide.with(holder.itemView.getContext())
                .load("https://www.zerohour.fr/" + author.getAvatarUrl())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .fallback(R.drawable.outline_person_24)
                .error(R.drawable.outline_person_24)
                .into(holder.avatarImage);

        holder.btnLike.setOnClickListener(v -> handleLike(holder, post));
        holder.btnComment.setOnClickListener(v -> {
            if (listener != null) listener.onCommentClicked(post);
        });
        
        if (holder.btnGoToMap != null) {
            holder.btnGoToMap.setOnClickListener(v -> {
                if (listener != null) listener.onGoToMapClicked(post);
            });
        }
    }

    private void updateLikesUI(PostViewHolder holder, Post post) {
        holder.likesCount.setText(String.format(Locale.getDefault(), "%d %s", post.getLikesCount(), "J'aime"));
        if (post.isLikedByMe()) {
            holder.btnLike.setImageResource(R.drawable.outline_favorite_24);
            holder.btnLike.setColorFilter(Color.RED);
        } else {
            holder.btnLike.setImageResource(R.drawable.outline_favorite_24);
            holder.btnLike.setColorFilter(Color.GRAY);
        }
    }

    private void handleLike(PostViewHolder holder, Post post) {
        boolean wasLiked = post.isLikedByMe();
        post.setLikedByMe(!wasLiked);
        updateLikesUI(holder, post);

        Callback<ResponseBody> callback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    post.setLikedByMe(wasLiked);
                    updateLikesUI(holder, post);
                    Toast.makeText(holder.itemView.getContext(), "Erreur lors du like", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                post.setLikedByMe(wasLiked);
                updateLikesUI(holder, post);
            }
        };

        if (post.isLikedByMe()) {
            RetrofitClient.getApi(holder.itemView.getContext()).likePost(post.getId()).enqueue(callback);
        } else {
            RetrofitClient.getApi(holder.itemView.getContext()).unlikePost(post.getId()).enqueue(callback);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage, postImage, btnLike, btnComment;
        TextView username, location, likesCount, caption, timestamp;
        View btnGoToMap;

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
        }
    }

    private static class PostDiffCallback extends DiffUtil.Callback {
        private final List<Post> oldList;
        private final List<Post> newList;
        public PostDiffCallback(List<Post> oldList, List<Post> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }
        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }
        @Override public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).getId() == newList.get(newPos).getId();
        }
        @Override public boolean areContentsTheSame(int oldPos, int newPos) {
            Post oldPost = oldList.get(oldPos);
            Post newPost = newList.get(newPos);
            return oldPost.getLikesCount() == newPost.getLikesCount() && oldPost.isLikedByMe() == newPost.isLikedByMe();
        }
    }
}
