package refdiff.parsers.universal.common;

import org.treesitter.TSNode;

import refdiff.core.cst.Location;
import refdiff.core.cst.Parameter;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class NodeUtils {
    public static String getNodeText(TSNode node, byte[] sourceBytes) {
        if (node == null || node.isNull()) {
            return "";
        }
        return new String(sourceBytes, node.getStartByte(), node.getEndByte() - node.getStartByte(), StandardCharsets.UTF_8);
    }
    
    public static Location generateLocation(TSNode decl, TSNode body, String path) {
        return new Location(
            path,
            decl.getStartByte(),
            decl.getEndByte(),
            decl.getStartPoint().getRow() + 1,
            body != null ? body.getStartPoint().getRow() + 1 : decl.getEndPoint().getRow() + 1,
            body != null ? body.getEndPoint().getRow() + 1 : decl.getEndPoint().getRow() + 1,
            body != null ? body.getStartByte() : decl.getEndByte(),
            body != null ? body.getEndByte() : decl.getEndByte()
        );
    }

    public static String generateParams(List<Parameter> params) {
        StringBuilder paramsStr = new StringBuilder();
        paramsStr.append("(");
        for (int i = 0; i < params.size(); i++) {
            paramsStr.append(params.get(i).getName());
            if (i < params.size() - 1) {
                paramsStr.append(", ");
            }
        }
        paramsStr.append(")");
        return paramsStr.toString();
    }
}