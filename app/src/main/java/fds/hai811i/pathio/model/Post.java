package fds.hai811i.pathio.model;
public class Post {
    private int id;
    private User author;
    private String location;
    private String imageUrl;
    private String caption;
    private String timestamp;

    // infos utiles, pour éviter de transférer trop de données
    private int likesCount;
    private boolean isLikedByMe;

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

    public String getCaption() {
        return caption;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getLikesCount() {
        return likesCount;
    }
}