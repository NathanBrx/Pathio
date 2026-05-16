package fds.hai811i.pathio.model;

import com.google.gson.annotations.SerializedName;

public class Group {
    private int id;
    private String name;
    private String description;
    @SerializedName("creator_id")
    private int creatorId;
    @SerializedName("member_count")
    private int memberCount;
    @SerializedName("is_member_me")
    private boolean isMemberMe;

    public Group(int id, String name, String description, int creatorId, int memberCount, boolean isMemberMe) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.memberCount = memberCount;
        this.isMemberMe = isMemberMe;
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
    public boolean amIMember() {
        return isMemberMe;
    }
}
