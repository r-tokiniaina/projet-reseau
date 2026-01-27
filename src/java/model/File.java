package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import logging.Log;
import utils.FileUtils;

public class File {

    public enum Type {FILE, DIRECTORY}
    public enum Permission {READ, WRITE, DELETE}

    private String name;
    private String path;
    private File parent;
    private Type type;
    private Set<Permission> permissions;

    public File() {
        this.name = "";
        this.path = "/";
        this.parent = null;
        this.type = Type.DIRECTORY;
        this.permissions = new HashSet<>();
        addPermission(Permission.READ);
        addPermission(Permission.WRITE);
        addPermission(Permission.DELETE);
    }

    public File(String path) {
        this();
        int lastSlash = path.lastIndexOf("/");
        this.name = path.substring(lastSlash + 1);
        this.path = path.substring(0, lastSlash + 1);
        this.type = getRealFile().isDirectory() ? Type.DIRECTORY : Type.FILE;
    }

    public File(String path, Type type) {
        this();
        int lastSlash = path.lastIndexOf("/");
        this.name = path.substring(lastSlash + 1);
        this.path = path.substring(0, lastSlash + 1);
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public File getParent() {
        return parent;
    }

    public void setParent(File parent) {
        this.parent = parent;
        this.path = parent.getPath() + parent.getName() + "/";
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

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }


    public File[] list() {
        java.io.File[] realSubFiles = getRealFile().listFiles();
        File[] subFiles = new File[realSubFiles.length];
        for (int i = 0; i < realSubFiles.length; i++) {
            File f = new File();
            f.setName(realSubFiles[i].getName());
            f.setParent(this);
            f.setType(realSubFiles[i].isDirectory() ? Type.DIRECTORY : Type.FILE);
            subFiles[i] = f;
        }
        return subFiles;
    }

    public void create() {
        if (type == Type.DIRECTORY) {
            getRealFile().mkdir();
        }
        else {
            try {
                getRealFile().createNewFile();
            }
            catch (IOException e) {
                Log.error("Unable to create a new file: " + e.getMessage());
            }
        }
    }

    public void delete() {
        if (type == Type.DIRECTORY) {
            for (File f : list()) {
                f.delete();
            }
        }
        getRealFile().delete();
    }


    public String computeChecksum() throws IOException {
        return FileUtils.computeChecksum(getRealFile());
    }


    public long length() {
        return getRealFile().length();
    }


    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(getRealFile()));
    }

    public OutputStream getOutputStream() throws IOException {
        return new BufferedOutputStream(new FileOutputStream(getRealFile()));
    }


    private java.io.File getRealFile() {
        return new java.io.File(Settings.getInstance().getUploadDir(), path.substring(1) + name);
    }
}
