package refdiff.rast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RastNode {
    public int id;
    public String type;
    public Location location;
    public String logicalName;
    public List<RastNode> nodes = new ArrayList<>();
    public Set<Stereotype> stereotypes = new HashSet<>();
    
    @Override
    public String toString() {
        return String.format("%s %s %s", location.toString(), type, logicalName);
    }
}
