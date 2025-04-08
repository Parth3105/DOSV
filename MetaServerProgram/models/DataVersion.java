package MetaServerProgram.models;
import java.sql.Timestamp;
public class DataVersion {
    

    private int id;
    private String name;
    private String email;
    private int version;
    private Timestamp validFrom;
    private Timestamp validTo;
    private boolean isCurrent;

    // 🧱 Constructor
    public DataVersion(int id, String name, String email, int version,
                             Timestamp validFrom, Timestamp validTo, boolean isCurrent) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.version = version;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.isCurrent = isCurrent;
    }

    // // 🛠 Getters & Setters
    // public int getId() { return id; }
    // public void setId(int id) { this.id = id; }

    // public String getName() { return name; }
    // public void setName(String name) { this.name = name; }

    // public String getEmail() { return email; }
    // public void setEmail(String email) { this.email = email; }

    // public int getVersion() { return version; }
    // public void setVersion(int version) { this.version = version; }

    // public Timestamp getValidFrom() { return validFrom; }
    // public void setValidFrom(Timestamp validFrom) { this.validFrom = validFrom; }

    // public Timestamp getValidTo() { return validTo; }
    // public void setValidTo(Timestamp validTo) { this.validTo = validTo; }

    // public boolean isCurrent() { return isCurrent; }
    // public void setCurrent(boolean current) { isCurrent = current; }

    // 📦 Optional: toString for easy printing
    @Override
    public String toString() {
        return "CustomerVersioned{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", version=" + version +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
