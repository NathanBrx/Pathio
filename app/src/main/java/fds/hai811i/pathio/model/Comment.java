package fds.hai811i.pathio.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private int id;
    private String authorName;
    private String text;
    private String timestamp;

    public Comment(int id, String authorName, String text, String timestamp) {
        this.id = id;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getAuthorName() { return authorName; }
    public String getText() { return text; }
    public String getTimestamp() { return timestamp; }
}
