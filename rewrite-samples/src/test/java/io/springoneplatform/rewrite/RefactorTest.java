package io.springoneplatform.rewrite;

import com.google.common.io.CharStreams;
import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import com.netflix.rewrite.refactor.Refactor;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

public class RefactorTest {
    @Test
    public void refactorGuavaCall() throws IOException {
        String bSource = CharStreams.toString(new InputStreamReader(
                FormattingTest.class.getResourceAsStream("/B.java")));

        Tr.CompilationUnit cu = new OracleJdkParser().parse(bSource);

        Refactor refactor = cu.refactor();
        refactor.changeMethodTargetToStatic(
                cu.findMethodCalls("com.google.common.base.Objects firstNonNull(..)"),
                "com.google.common.base.MoreObjects"
        );

        refactor.changeMethodName(
                cu.findMethodCalls("com.google..MoreExecutors sameThreadExecutor()"),
                "directExecutor"
        );

        System.out.println(refactor.diff());
    }
}
