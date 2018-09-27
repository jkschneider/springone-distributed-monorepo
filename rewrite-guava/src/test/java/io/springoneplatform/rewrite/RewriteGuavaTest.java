package io.springoneplatform.rewrite;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RewriteGuavaTest {

    @Test
    public void findAndFixIssues() {
        Tr.CompilationUnit cu = new OracleJdkParser().parse("" +
                "import com.google.common.base.*;\n" +
                "public class A {\n" +
                "   void foo() {\n" +
                "       Objects.firstNonNull(null,\n" +
                "           1);\n" +
                "   }\n" +
                "}");

        assertThat(RewriteGuava.listIssues(cu))
                .contains("Objects.firstNonNull");

        assertThat(RewriteGuava.refactor(cu, "src/main/java/A.java")).isEqualTo("" +
                "diff --git a/src/main/java/A.java\n" +
                "index d123f1c..b9543ef 100644\n" +
                "--- a/src/main/java/A.java\n" +
                "+++ b/src/main/java/A.java\n" +
                "@@ -1,7 +1,7 @@\n" +
                "-import com.google.common.base.*;\n" +
                "+import com.google.common.base.MoreObjects;\n" +
                " public class A {\n" +
                "    void foo() {\n" +
                "-       Objects.firstNonNull(null,\n" +
                "+       MoreObjects.firstNonNull(null,\n" +
                "            1);\n" +
                "    }\n" +
                " }\n" +
                "\\ No newline at end of file\n");
    }
}
