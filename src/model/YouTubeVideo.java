package model;

public class YouTubeVideo {
    private String channel;
    private String date;
    private String title;
    private String description;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private String videoId;
    private String channelId;
    private String thumbnailUrl;
    private String categoryId;
    private String liveBroadcastContent;

    // Constructor
    public YouTubeVideo(String channel, String date, String title, String description, int viewCount,
                        int likeCount, int commentCount, String videoId, String channelId, String thumbnailUrl,
                        String categoryId, String liveBroadcastContent) {
        this.channel = channel;
        this.date = date;
        this.title = title;
        this.description = description;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.videoId = videoId;
        this.channelId = channelId;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryId = categoryId;
        this.liveBroadcastContent = liveBroadcastContent;
    }

    // Getters for all fields
    public String getChannel() {
        return channel;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getLiveBroadcastContent() {
        return liveBroadcastContent;
    }

    @Override
    public String toString() {
        return title;
    }
}
