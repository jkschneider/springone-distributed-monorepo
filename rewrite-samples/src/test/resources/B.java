package io.springoneplatform.rewrite;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.MoreExecutors;

public class B {
    void foo() {
        Objects.firstNonNull(
                null,
                "hi"
        );

        MoreExecutors.sameThreadExecutor();
    }
}


