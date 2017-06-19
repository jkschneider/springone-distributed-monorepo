package io.jschneider.gradlesummit.rewrite;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.refactor.Refactor;

import java.util.ArrayList;
import java.util.List;

public class RewriteGuava {
    public static List<String> listIssues(Tr.CompilationUnit cu) {
        List<String> problems = new ArrayList<>();

        if (cu.hasType("com.google.common.util.concurrent.FutureFallback"))
           problems.add("FutureFallback");
        if (cu.hasType("com.google.common.io.InputSupplier"))
           problems.add("InputSupplier");
        if (cu.hasType("com.google.common.io.OutputSupplier"))
           problems.add("OutputSupplier");
        if (cu.hasType("com.google.common.collect.MapConstraints"))
           problems.add("MapConstraints");
        if (!cu.findMethodCalls("com.google.common.base.Objects firstNonNull(..)").isEmpty())
           problems.add("Objects.firstNonNull");
        if (!cu.findMethodCalls("com.google.common.base.Objects toStringHelper(..)").isEmpty())
           problems.add("Objects.toStringHelper");
        if (!cu.findMethodCalls("com.google.common.collect.Iterators emptyIterator()").isEmpty())
           problems.add("Iterators.emptyIterator");
        if (!cu.findMethodCalls("com.google.common.util.concurrent.Futures get(..)").isEmpty())
           problems.add("Futures.get");
        if (!cu.findMethodCalls("com.google.common.util.concurrent.Futures withFallback(..)").isEmpty())
           problems.add("Futures.withFallback");
        if (!cu.findMethodCalls("com.google.common.util.concurrent.Futures transform(com.google.common.util.concurrent.ListenableFuture, com.google.common.util.concurrent.AsyncFunction, ..)").isEmpty())
           problems.add("Futures.transform");
        if (!cu.findMethodCalls("com.google.common.reflect.TypeToken isAssignableFrom(..)").isEmpty())
           problems.add("TypeToken.isAssignableFrom");
        if (!cu.findMethodCalls("com.google.common.util.concurrent.MoreExecutors sameThreadExecutor()").isEmpty())
           problems.add("MoreExecutors.sameThreadExecutor");

        return problems;
    }

    public static String refactor(Tr.CompilationUnit cu, String path) {
        Refactor refactor = cu.refactor();

        refactor.changeMethodTargetToStatic(
                cu.findMethodCalls("com.google.common.base.Objects toStringHelper(..)"),
                "com.google.common.base.MoreObjects"
        );

        refactor.changeMethodTargetToStatic(
                cu.findMethodCalls("com.google.common.base.Objects firstNonNull(..)"),
                "com.google.common.base.MoreObjects"
        );

        refactor.changeMethodTargetToStatic(
                cu.findMethodCalls("com.google.common.collect.Iterators emptyIterator(..)"),
                "java.util.Collections"
        );

        refactor.changeMethodName(
                cu.findMethodCalls("com.google.common.util.concurrent.MoreExecutors sameThreadExecutor()"),
                "directExecutor"
        );

        refactor.changeMethodName(
                cu.findMethodCalls("com.google.common.util.concurrent.Futures get(java.util.concurrent.Future, Class)"),
                "getChecked"
        );

        refactor.changeMethodName(
                cu.findMethodCalls("com.google.common.util.concurrent.Futures transform(com.google.common.util.concurrent.ListenableFuture, com.google.common.util.concurrent.AsyncFunction, ..)"),
                "transformAsync"
        );

        refactor.changeType(
                "com.google.common.base.Objects.ToStringHelper",
                "com.google.common.base.MoreObjects.ToStringHelper");

        return refactor.diff().replaceAll("([ab])/.*/\\w+\\.java", "$1/" + path);
    }
}
