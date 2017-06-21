package io.jschneider.gradlesummit.rewrite;

import java.util.*;

public class A {
    public void foo() {
        List<String> l = Arrays.asList("",
                "a",
                    "b",
                        "c"
        );
    }
}