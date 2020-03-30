package com.aoher.domain;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Objects;

import static com.aoher.util.Utilities.cleanUp;

@Entity
@NamedQueries({
        @NamedQuery(name = "Reminder.findByList", query = "SELECT r FROM Reminder r WHERE r.list = :list")
})
public class Reminder {

    @Id
    @GeneratedValue(generator = "REMINDER_ID")
    @TableGenerator(name = "REMINDER_ID", table = "ID_GEN", allocationSize = 1)
    @Min(value = 0, message = "a reminder's id must be greater than 0")
    private long id;

    @ManyToOne
    @NotNull(message = "a reminder must belong to a list")
    private List list;

    @NotNull(message = "REMINDER_TITLE")
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "REMINDER_DATE")
    private Calendar date;

    @Embedded
    private Location location;

    private String image;

    public long getId() {
        return id;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = cleanUp(title);
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = cleanUp(image);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return id == reminder.id &&
                Objects.equals(list, reminder.list) &&
                Objects.equals(title, reminder.title) &&
                Objects.equals(date, reminder.date) &&
                Objects.equals(location, reminder.location) &&
                Objects.equals(image, reminder.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, list, title, date, location, image);
    }
}