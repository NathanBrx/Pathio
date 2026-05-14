package fds.hai811i.pathio.model;

public class Post {
    private int id;
    private User author;
    private String location;
    private String imageUrl;
    private String caption;
    private String timestamp;
    private String audioUrl;

    // infos utiles, pour éviter de transférer trop de données
    private int likesCount;
    private boolean isLikedByMe;

    public Post(int id, User author, String location, String imageUrl, String caption, String timestamp, int likesCount) {
        this.id = id;
        this.author = author;
        this.location = location;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.timestamp = timestamp;
        this.likesCount = likesCount;
    }

    // Getters
    public int getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getCaption() {
        return caption;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public boolean isLikedByMe() {
        return isLikedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        this.isLikedByMe = likedByMe;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }
}