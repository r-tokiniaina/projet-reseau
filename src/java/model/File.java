package model;

import java.util.Set;

public class File {

    public enum Type {FILE, DIRECTORY}
    public enum Permission {READ, WRITE, DELETE}

    private String name;
    private File parent;
    private Type type;
    private Set<Permission> permissions;

    public File() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getParent() {
        return parent;
    }

    public void setParent(File parent) {
        this.parent = parent;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission p) {
        return permissions.contains(p);
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
