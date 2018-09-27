import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.MoreExecutors;

public class B {
    void foo() {
        MoreObjects.firstNonNull(
                null,
                "hi"
        );

        MoreExecutors.directExecutor();
    }
}

