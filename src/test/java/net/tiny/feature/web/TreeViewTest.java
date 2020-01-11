package net.tiny.feature.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import net.tiny.config.JsonParser;

public class TreeViewTest {

    @Test
    public void testTreeView() throws Exception {
        HashSet<String> names = new HashSet<>();
        names.add("abc");
        names.add("app.user");
        names.add("app.sample.date");
        names.add("app.sample.time");
        names.add("app.sample.1");
        names.add("app.sample.10");
        names.add("app.sample.20");
        names.add("app.sample.2");
        String json = TreeView.json(names);
        System.out.println(json);

        Set<TreeView.Node> nodes = TreeView.parse(names);
        assertEquals(2, nodes.size());
    }

    @Test
    public void testNodeJson() throws Exception {
        TreeView.Node parent = new TreeView.Node();
        parent.text = "parent-1";
        parent.href = "#parent1";
        parent.tags = new String[] {"p1"};

        TreeView.Node child1 = new TreeView.Node();
        child1.text = "child-1";
        child1.href = "#child1";
        TreeView.Node child2 = new TreeView.Node();
        child2.text = "child-2";
        child2.href = "#child2";
        parent.nodes = new TreeSet<>();
        parent.nodes.add(child1);
        parent.nodes.add(child2);

        TreeView.Node parent2 = new TreeView.Node();
        parent2.text = "parent-2";
        parent2.href = "#parent2";

        List<TreeView.Node> nodes = new ArrayList<>();
        nodes.add(parent);
        nodes.add(parent2);
        String json = JsonParser.marshal(nodes);
        System.out.println(json);

    }
}
