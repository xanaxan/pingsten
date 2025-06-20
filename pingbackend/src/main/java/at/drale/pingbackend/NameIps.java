package at.drale.pingbackend;

public class NameIps {

    public String name;
    public String ipWork;
    public String ipVpn;

    // Default constructor for JSON deserialization
    public NameIps() {
    }

    public NameIps(String name, String ipWork, String ipVpn) {
        this.name = name;
        this.ipWork = ipWork;
        this.ipVpn = ipVpn;
    }
}