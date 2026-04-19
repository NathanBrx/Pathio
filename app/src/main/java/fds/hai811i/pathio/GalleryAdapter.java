package fds.hai811i.pathio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fds.hai811i.pathio.model.Post;
import fds.hai811i.pathio.model.User;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();
    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
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
        holder.timestamp.setText(post.getTimestamp());

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
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // ==========================================
    // THE VIEWHOLDER
    // ==========================================
    // This inner class acts as a cache for the views in your XML layout
    // so `findViewById` doesn't get called 1000 times as the user scrolls.
    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage, postImage, btnLike, btnComment;
        TextView username, location, likesCount, caption, timestamp;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // Link these to the exact IDs you used in item_gallery_post.xml
            avatarImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.txt_post_username);
            location = itemView.findViewById(R.id.txt_post_location);
            postImage = itemView.findViewById(R.id.img_post_content);
            likesCount = itemView.findViewById(R.id.txt_likes_count);
            caption = itemView.findViewById(R.id.txt_post_caption);
            timestamp = itemView.findViewById(R.id.txt_post_time);

            // Buttons
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
        }
    }
}