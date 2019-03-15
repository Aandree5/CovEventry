package g3.coveventry.lint;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Register the issues for all lint check classes
 * <p>
 * Using fully qualified class names to avoid the unstable api warning
 */
@SuppressWarnings("unused,UnstableApiUsage")
public class UserIssueRegistry extends com.android.tools.lint.client.api.IssueRegistry {

    /**
     * Constructor
     */
    public UserIssueRegistry() {
    }

    @Override
    public boolean cacheable() {
        // In the IDE, cache across incremental runs; here, lint is never run in parallel
        // Outside of the IDE, typically in Gradle, we don't want this caching since
        // lint can run in parallel and this caching can be incorrect;
        // see for example issue 77891711
        return com.android.tools.lint.client.api.LintClient.Companion.isStudio();
    }

    @NotNull
    @Override
    public List<com.android.tools.lint.detector.api.Issue> getIssues() {
        // List available issues
        return Collections.singletonList(UserDetector.ISSUE);
    }
}
