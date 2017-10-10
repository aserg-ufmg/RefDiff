package refdiff.rast;

import java.util.List;
import java.util.Set;

public class RastNode {
    public int id;
    public String type;
    public String logicalName;
    public Location file;
    public List<RastNode> children;
    public Set<Stereotype> stereotypes;
}
