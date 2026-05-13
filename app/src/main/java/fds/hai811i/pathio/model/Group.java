package fds.hai811i.pathio.model;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private int id;
    private String name;
    private String description;
    private String creatorName;
    private int memberCount;
    private List<User> members;

    public Group(int id, String name, String description, String creatorName, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creatorName = creatorName;
        this.memberCount = memberCount;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatorName() { return creatorName; }
    public int getMemberCount() { return memberCount; }
}
