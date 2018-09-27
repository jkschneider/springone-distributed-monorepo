package io.springoneplatform.rewrite;

import com.google.common.io.CharStreams;
import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class FindingCodeTest {
    @Test
    public void rewriteAstFindCode() throws IOException {
        String aSource = CharStreams.toString(new InputStreamReader(
                FormattingTest.class.getResourceAsStream("/A.java")));

        Tr.CompilationUnit cu = new OracleJdkParser().parse(aSource);

        assertThat(cu.findMethodCalls("java.util.Arrays asList(..)")).hasSize(1);

        assertThat(cu.hasType("java.util.Arrays")).isTrue();
        assertThat(cu.hasType(Arrays.class)).isTrue();

        assertThat(cu.findType(Arrays.class))
                .hasSize(1)
                .hasOnlyElementsOfType(Tr.Ident.class);

        assertThat(cu.hasImport(Arrays.class)).isTrue();

        assertThat(cu.firstClass().findFields("java.util.Arrays")).isEmpty();
    }
}
