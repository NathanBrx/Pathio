package fds.hai811i.pathio.model;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private int id;
    private String name;
    private String description;
    private int creatorId;
    private int memberCount;
    private List<User> members;

    public Group(int id, String name, String description, int creatorId, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.memberCount = memberCount;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public int getCreatorId() {
        return creatorId;
    }
    public int getMemberCount() {
        return memberCount;
    }
}
