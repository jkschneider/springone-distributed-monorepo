package io.jschneider.gradlesummit.rewrite;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.ast.Tree;
import com.netflix.rewrite.ast.visitor.AstVisitor;
import com.netflix.rewrite.parse.OracleJdkParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * Generates a dot file that can be used to generate a hierarchical graph
 * diagramming a Rewrite AST.
 *
 * Run `dot -Tpng graph.gv > graph.png` from the root of this project to generate
 * the image.
 */
public class RewriteToGraphviz {
    public static void main(String[] args) throws IOException {
        String javaSource = "" +
                "import com.google.common.base.*;\n" +
                "public class A {\n" +
                "   void foo() {\n" +
                "       Objects.firstNonNull(null,\n" +
                "           1);\n" +
                "   }\n" +
                "}";

        final Tr.CompilationUnit cu = new OracleJdkParser(emptyList() /* classpath to be used in parsing */)
                .parse(javaSource);

        List<Link> links = cu.accept(new AstVisitor<List<Link>>(emptyList()) {
            private Node node(Tree t) {
                if (t instanceof Tr.CompilationUnit) {
                    return new Node(t, "cu");
                } else if (t instanceof Tr.Import) {
                    return new Node(t, "Import");
                } else if (t instanceof Tr.FieldAccess) {
                    return new Node(t, "FieldAccess{" + ((Tr.FieldAccess) t).getSimpleName() + "}");
                } else if (t instanceof Tr.Ident) {
                    return new Node(t, "Ident{" + ((Tr.Ident) t).getSimpleName() + "}");
                } else if (t instanceof Tr.ClassDecl) {
                    return new Node(t, "ClassDecl{" + ((Tr.ClassDecl) t).getSimpleName() + "}");
                } else if (t instanceof Tr.Literal) {
                    return new Node(t, "Literal{" + ((Tr.Literal) t).getValue() + "}");
                } else if (t instanceof Tr.MethodDecl) {
                    return new Node(t, "MethodDecl{" + ((Tr.MethodDecl) t).getSimpleName() + "}");
                } else if (t instanceof Tr.Block) {
                    return new Node(t, "Block");
                } else if (t instanceof Tr.Empty) {
                    return new Node(t, "Empty");
                } else if (t instanceof Tr.MethodInvocation) {
                    return new Node(t, "MethodInvocation{" + ((Tr.MethodInvocation) t).getSimpleName() + "}");
                } else if (t instanceof Tr.Modifier) {
                    return new Node(t, "Modifier{" + t.getClass().getSimpleName().toLowerCase() + "}");
                } else if (t instanceof Tr.Primitive) {
                    return new Node(t, "Primitive{" + ((Tr.Primitive) t).getType().getKeyword() + "}");
                }
                System.out.println("Encountered unknown node: " + t.getClass().getSimpleName());
                return new Node(t, "UNKNOWN");
            }

            @Override
            public List<Link> visitTree(Tree t) {
                List<Tree> path = cu.cursor(t).getPath();
                List<Link> links = new ArrayList<>();
                for (int i = 0; i < path.size() - 1; i++) {
                    links.add(new Link(node(path.get(i)), node(path.get(i + 1))));
                }
                return links;
            }
        });

        Set<Node> nodes = links.stream().flatMap(l -> Stream.of(l.from, l.to)).collect(Collectors.toSet());

        Files.write(new File("graph.gv").toPath(), ("" +
                "digraph AbstractSyntaxTree {\n" +
                "node [shape=box]; " +
                nodes.stream().map(n -> n.name).collect(Collectors.joining(";")) + "\n" +
                nodes.stream().map(n -> n.name + " [label=\"" + n.label + "\"]").collect(Collectors.joining(";\n")) + "\n" +
                links.stream().distinct().map(l -> l.from.name + "->" + l.to.name).collect(Collectors.joining(";\n")) +
                "\noverlap=false\n" +
                "fontsize=12;\n" +
                "}").getBytes());
    }

    static class Node {
        String name;
        String label;

        Node(Tree t, String label) {
            this.name = t.getClass().getSimpleName() + Math.abs(t.getId());
            this.label = label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return name != null ? name.equals(node.name) : node.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    static class Link {
        Node from;
        Node to;

        Link(Node from, Node to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Link link = (Link) o;
            return (from != null ? from.equals(link.from) : link.from == null) && (to != null ? to.equals(link.to) : link.to == null);
        }

        @Override
        public int hashCode() {
            int result = from != null ? from.hashCode() : 0;
            result = 31 * result + (to != null ? to.hashCode() : 0);
            return result;
        }
    }
}
