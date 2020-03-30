package com.aoher.domain;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.aoher.util.Utilities.cleanUp;

@Entity
@NamedQueries({
        @NamedQuery(name = "List.findByOwner", query = "SELECT l FROM List l WHERE l.owner = :owner"),
        @NamedQuery(name = "List.findSize", query = "SELECT COUNT(r) FROM Reminder r WHERE r.list = :list")
})
public class List {

    @Id
    @GeneratedValue(generator = "LIST_ID")
    @TableGenerator(name = "LIST_ID", table = "ID_GEN", allocationSize = 1)
    @Min(value = 0, message = "a list's id must be greater than 0")
    private long id;

    @NotNull(message = "LIST_TITLE")
    private String title;

    @ManyToOne
    @NotNull(message = "a list must be assigned to a user")
    private User owner;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = cleanUp(title);
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        List list = (List) o;
        return id == list.id &&
                Objects.equals(title, list.title) &&
                Objects.equals(owner, list.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, owner);
    }
}