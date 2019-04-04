package dst.courseproject.models.service;

import dst.courseproject.entities.Category;
import dst.courseproject.entities.Comment;
import dst.courseproject.entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class VideoServiceModel {
    private String id;

    private String title;

    private String description;

    private String videoIdentifier;

    private User author;

    private Category category;

    private Set<Comment> comments;

    private Long views;

    private LocalDateTime uploadedOn;

    private Map<String, User> usersLiked;

    public VideoServiceModel() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoIdentifier() {
        return this.videoIdentifier;
    }

    public void setVideoIdentifier(String videoIdentifier) {
        this.videoIdentifier = videoIdentifier;
    }

    public User getAuthor() {
        return this.author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Comment> getComments() {
        return this.comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Long getViews() {
        return this.views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public LocalDateTime getUploadedOn() {
        return this.uploadedOn;
    }

    public void setUploadedOn(LocalDateTime uploadedOn) {
        this.uploadedOn = uploadedOn;
    }

    public Map<String, User> getUsersLiked() {
        return this.usersLiked;
    }

    public void setUsersLiked(Map<String, User> usersLiked) {
        this.usersLiked = usersLiked;
    }
}
