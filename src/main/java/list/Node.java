package list;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class Node {
    private List<Node> children = new ArrayList<>();
    private List<Node> parent = null;
    private List<String> data = null;
    private String methodScope;

    private int nodeC = 0;

    public Node(List<String> data, String methodScope) {
        this.data = new ArrayList<>();
        this.data.addAll(data);
        this.methodScope = methodScope;
    }

    public String getMethodScope() {
        return methodScope;
    }

    public void incNodec() {
        ++this.nodeC;
    }
    public int getNodeC() {
        return this.nodeC;
    }


    public List<Node> getChildren() {
        return children;
    }

    public List<Node> getParent() {
        return parent;
    }



    public void setParent(Node parent) {
        //parent.addChild(this);
        this.parent.add(parent);
    }


    public void addChild(Node child) {
        //child.setParent(this);
        this.children.add(child);
    }

    public List<String> getData() {
        return this.data;
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public void removeParent() {
        this.parent = null;
    }
}