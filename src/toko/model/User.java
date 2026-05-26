package toko.model;

public class User {
    private String username;
    private String password;
    private String status; // admin, kasir, manajer

    public User(String username, String password, String status) {
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public String getUsername() { 
        return username; 
    }

    public String getPassword() { 
        return password; 
    }
    public String getStatus() { 
        return status; 
    }

    public void setUsername(String username) { 
        this.username = username; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }

    @Override
    public String toString() {
        return username + "," + password + "," + status;
    }

    public static User fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3) return null;
        return new User(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }
}
