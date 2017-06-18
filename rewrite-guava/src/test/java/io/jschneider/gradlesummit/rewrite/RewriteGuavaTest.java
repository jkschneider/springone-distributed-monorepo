package io.jschneider.gradlesummit.rewrite;

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

        assertThat(RewriteGuava.refactor(cu).fix().printTrimmed()).isEqualTo("" +
                "import com.google.common.base.MoreObjects;\n" +
                "public class A {\n" +
                "   void foo() {\n" +
                "       MoreObjects.firstNonNull(null,\n" +
                "           1);\n" +
                "   }\n" +
                "}");
    }
}
