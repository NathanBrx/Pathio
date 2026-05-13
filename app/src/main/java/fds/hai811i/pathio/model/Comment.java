package fds.hai811i.pathio.model;

public class Comment {
    private int id;
    private String username;
    private String text;
    private String timestamp;
    private String avatarUrl;

    public Comment() {}

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}