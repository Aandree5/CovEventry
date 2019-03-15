package g3.coveventry.lint;

import com.android.tools.lint.detector.api.JavaContext;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UBlockExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.ULambdaExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UReturnExpression;
import org.jetbrains.uast.UastUtils;
import org.jetbrains.uast.visitor.AbstractUastVisitor;

import java.util.Collections;
import java.util.List;

import static com.android.tools.lint.detector.api.Lint.getMethodName;

/**
 * Detect usage error on the user class
 * - Not calling `apply()` after `updateProfile()`
 * <p>
 * Using fully qualified class names to avoid the unstable api warning
 */
@SuppressWarnings("unused,UnstableApiUsage")
public class UserDetector extends com.android.tools.lint.detector.api.Detector
        implements com.android.tools.lint.detector.api.Detector.UastScanner {
    /**
     * Issue describing the problem and pointing to the detector implementation
     */
    @SuppressWarnings("WeakerAccess")
    public static final com.android.tools.lint.detector.api.Issue ISSUE = com.android.tools.lint.detector.api.Issue.create(
            // ID: used in @SuppressLint warnings etc
            "UserUpdateProfileNoApply",

            // Title -- shown in the IDE's preference dialog, as category headers in the
            // Analysis results window, etc
            "User profile updated but not saved",

            // Full explanation of the issue
            // Can use some markdown markup such as `monospace`, *italic*, and **bold**.
            "`User.updateProfile()` changes the user data but does **not** save it. You must call "
                    + "`apply()` on the resulting object to actually make the `User` save the data.",
            com.android.tools.lint.detector.api.Category.CORRECTNESS,
            6,
            com.android.tools.lint.detector.api.Severity.WARNING,
            new com.android.tools.lint.detector.api.Implementation(
                    UserDetector.class, com.android.tools.lint.detector.api.Scope.JAVA_FILE_SCOPE))
            .setAndroidSpecific(true);

    /**
     * Constructor
     */
    public UserDetector() {
    }

    /**
     * Get a list of method names to look for, for this detector
     *
     * @return A list of the method names to look for
     */
    @Override
    public List<String> getApplicableMethodNames() {
        // Look for method updateProfile()
        return Collections.singletonList("updateProfile");
    }


    /**
     * Visit each method found, from getApplicableMethodNames()
     *
     * @param context The java context (?File)
     * @param call    The expression for the method call
     * @param method  The actual method
     */
    @Override
    public void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression call, @NotNull PsiMethod method) {
        // If the found method is not a member of the class User, don't check anything else
        if (!context.getEvaluator().isMemberInClass(method, "g3.coveventry.user.User"))
            return;


        // Get the entire declaration where the method was found
        @SuppressWarnings("unchecked")
        UElement surroundingDeclaration = UastUtils.getParentOfType(call, true, UMethod.class, UBlockExpression.class, ULambdaExpression.class);
        if (surroundingDeclaration == null)
            return;

        // Create finder to look for the needed method
        ShowFinder finder = new ShowFinder(call);
        surroundingDeclaration.accept(finder);

        // If method was not found show warning to user
        if (!finder.isNeededMethodCalled()) {
            context.report(ISSUE, call, context.getCallLocation(call, true, false),
                    "User will be updated but **not** saved: did you forget to call `apply()` ?");
        }
    }

    private static class ShowFinder extends AbstractUastVisitor {
        // The call expression of the method found in visitMethodCall()
        private final UCallExpression target;
        // Check if the needed method was found
        private boolean found;
        // Check if the target has been seen already
        private boolean seenTarget;

        /**
         * Constructor
         *
         * @param target Call expression for the method
         */
        private ShowFinder(UCallExpression target) {
            this.target = target;
            found = false;
            seenTarget = false;
        }

        @Override
        public boolean visitCallExpression(@NotNull UCallExpression node) {
            // Check if target has been seen
            if (node == target || node.getJavaPsi() != null && node.getJavaPsi() == target.getJavaPsi()) {
                seenTarget = true;

            } else {
                // If target was seen and the needed method called, mark variable so the warning is not shown
                if ((seenTarget || target.equals(node.getReceiver())) && "apply".equals(getMethodName(node))) {
                    //  Do more flow analysis to see whether we're really calling show
                    // on the right type of object?
                    found = true;
                }
            }

            return super.visitCallExpression(node);
        }


        @Override
        public boolean visitReturnExpression(@NotNull UReturnExpression node) {
            // If using "return User.updateProfile(...)" don't show warning
            if (UastUtils.isChildOf(target, node.getReturnExpression(), true))
                found = true;

            return super.visitReturnExpression(node);
        }

        /**
         * Check if needed method was found
         *
         * @return True if the needed method was found and false otherwise
         */
        boolean isNeededMethodCalled() {
            return found;
        }
    }
}
