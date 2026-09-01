package dev.gaphunter.integeroverflowwideningcompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import dev.gaphunter.integeroverflowwideningcompanion.detect.JavaOverflowFinder
import dev.gaphunter.integeroverflowwideningcompanion.review.ReviewPrompt

/**
 * Flags an `int * int`/`int + int` expression assigned/passed directly
 * to a `long`-typed destination with no widening cast on either
 * operand -- CWE-190, Integer Overflow or Wraparound. The operation
 * happens in 32-bit `int` arithmetic first, silently truncating the
 * result (often to a negative value) before it's widened to `long`.
 *
 * Runs via `checkFile` (same shape as every other inspection in this
 * catalog); [JavaOverflowFinder] does the real PSI walk.
 */
class OverflowInspection : LocalInspectionTool() {

    companion object {
        const val MAX_FILE_LENGTH = 500_000
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file.text.length > MAX_FILE_LENGTH) return null
        if (file !is PsiJavaFile) return null

        val hits = JavaOverflowFinder.findAll(file)
        if (hits.isEmpty()) return null

        val problems = hits.map { hit ->
            manager.createProblemDescriptor(
                hit.anchor,
                "int arithmetic result widened to long -- this computes in 32-bit int first, silently " +
                    "truncating (often to a negative value) before the already-wrong result is widened " +
                    "(CWE-190, Integer Overflow or Wraparound); cast at least one operand to long BEFORE the operation",
                isOnTheFly,
                emptyArray(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
        }

        val path = file.virtualFile?.path
        if (path != null) {
            for (hit in hits) {
                val lineNumber = file.viewProvider.document?.getLineNumber(hit.anchor.textRange.startOffset) ?: -1
                ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
            }
        }

        return problems.toTypedArray()
    }
}
