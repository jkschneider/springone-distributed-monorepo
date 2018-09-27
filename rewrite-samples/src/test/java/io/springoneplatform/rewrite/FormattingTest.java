package io.springoneplatform.rewrite;

import com.google.common.io.CharStreams;
import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;

public class FormattingTest {
    @Test
    public void rewriteAstPreservesFormatting() throws IOException {
        String aSource = CharStreams.toString(new InputStreamReader(
                FormattingTest.class.getResourceAsStream("/A.java")));

        Tr.CompilationUnit cu = new OracleJdkParser().parse(aSource);
        assertThat(cu.print()).isEqualTo(aSource);

        cu.firstClass().methods().get(0).getBody().getStatements()
                .forEach(t -> System.out.println(t.printTrimmed()));
    }
}
