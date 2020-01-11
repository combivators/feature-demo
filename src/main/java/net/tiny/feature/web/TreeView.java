package net.tiny.feature.web;

import java.util.Set;
import java.util.TreeSet;

import net.tiny.config.JsonParser;

public class TreeView {

    private Set<Node> nodes = new TreeSet<>();
    public TreeView() {}
    private TreeView(Set<Node> nodes) {
        setNodes(nodes);
    }
    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public String json() {
        return JsonParser.marshal(nodes);
    }

    public static String json(Set<String> names) {
        return JsonParser.marshal(parse(names));
    }

    public static TreeView of(Set<String> names) {
        return new TreeView(parse(names));
    }

    public static Set<Node> parse(Set<String> names) {
        Set<Node> nodes = new TreeSet<>();
        names.forEach(n -> append(n, null, nodes));
        //Collections.sort(nodes);
        return nodes;
    }


    private static void append(String name, String pname, Set<Node> nodes) {
        int pos = name.indexOf(".");
        if (pos == -1) {//is simple name
            Node n = new Node();
            n.text = name;
            n.href = (null != pname) ? pname+"."+name : name;
            nodes.add(n);
            return;
        }
        // Append a child node
        String parent = name.substring(0, pos);
        Node p = find(parent, nodes);
        if (null == p) {
            p = new Node();
            p.text = parent;
            p.href = (null != pname) ? pname+"."+parent : parent;
            p.nodes = new TreeSet<>();
            nodes.add(p);
        }
        append(name.substring(pos+1), p.href, p.nodes);
    }


    private static Node find(String name, Set<Node> nodes) {
        return nodes.stream()
             .filter(n -> name.equals(n.text))
             .findFirst()
             .orElse(null);
    }

    public static int compareNatural(String a, String b) {
        int la = a.length();
        int lb = b.length();
        int ka = 0;
        int kb = 0;
        while (true) {
            if (ka == la)
                return kb == lb ? 0 : -1;
            if (kb == lb)
                return 1;
            if (a.charAt(ka) >= '0' && a.charAt(ka) <= '9' && b.charAt(kb) >= '0' && b.charAt(kb) <= '9') {
                int na = 0;
                int nb = 0;
                while (ka < la && a.charAt(ka) == '0')
                    ka++;
                while (ka + na < la && a.charAt(ka + na) >= '0' && a.charAt(ka + na) <= '9')
                    na++;
                while (kb < lb && b.charAt(kb) == '0')
                    kb++;
                while (kb + nb < lb && b.charAt(kb + nb) >= '0' && b.charAt(kb + nb) <= '9')
                    nb++;
                if (na > nb)
                    return 1;
                if (nb > na)
                    return -1;
                if (ka == la)
                    return kb == lb ? 0 : -1;
                if (kb == lb)
                    return 1;

            }
            if (a.charAt(ka) != b.charAt(kb))
                return a.charAt(ka) - b.charAt(kb);
            ka++;
            kb++;
        }
    }

    public static class Node implements Comparable<Node> {
        String text;
        String href;
        String[] tags;
        Set<Node> nodes;

        public String getText() {
            return text;
        }
        public String getHref() {
            return href;
        }

        @Override
        public int compareTo(Node that) {
            return compareNatural(getHref(), that.getHref());
        }
    }

    public static class KeyValue {
        String lang;
        String key;
        String value;
    }

}
