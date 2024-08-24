package database;

import java.time.LocalDateTime;

//Holds the shared variables between Timeline.java and Event.java, mostly used by the Editor abstract class
public abstract class TimelineObject<T> implements DBObject<T> {
    LocalDateTime startDate = LocalDateTime.of(0, 1, 1, 0, 0, 0, 0);
    LocalDateTime endDate = LocalDateTime.of(0, 1, 1, 0, 0, 0, 0);
    LocalDateTime creationDate;
    String imagePath;

    public abstract int getID();

    public abstract int getOwnerID();

    public abstract void setOwnerID(int ownerID);

    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setImage(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getDescription();

    public abstract void setDescription(String name);
}
