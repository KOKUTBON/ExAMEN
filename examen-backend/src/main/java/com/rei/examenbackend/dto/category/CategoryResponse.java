package com.rei.examenbackend.dto.category;

public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private boolean custom;

    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, String description) {
        this(id, name, description, false);
    }

    public CategoryResponse(Long id, String name, String description, boolean custom) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.custom = custom;
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

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private boolean custom;

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

        public Builder custom(boolean custom) {
            this.custom = custom;
            return this;
        }

        public CategoryResponse build() {
            return new CategoryResponse(id, name, description, custom);
        }
    }
}
