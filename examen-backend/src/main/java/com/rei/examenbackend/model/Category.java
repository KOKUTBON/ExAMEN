package com.rei.examenbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // Null for the shared default library; set for categories a user created.
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Category() {}

    public Category(Long id, String name, String description) {
        this(id, name, description, null);
    }

    public Category(Long id, String name, String description, User owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isOwnedBy(User user) {
        return owner != null && user != null && owner.getId().equals(user.getId());
    }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private User owner;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public Category build() {
            return new Category(id, name, description, owner);
        }
    }
}
