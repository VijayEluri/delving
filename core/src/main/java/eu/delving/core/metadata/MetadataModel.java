package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("metadata-model")
public class MetadataModel {

    @XStreamAsAttribute
    private String name;

    @XStreamImplicit
    private List<MetadataNode> nodes;

    public List<MetadataNode> getNodes() {
        return nodes;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(name).append(" {\n");
        for (MetadataNode node : nodes) {
            for (String line : node.toString().split("\n")) {
                out.append("   ").append(line).append('\n');
            }
        }
        out.append("}");
        return out.toString();
    }
}
