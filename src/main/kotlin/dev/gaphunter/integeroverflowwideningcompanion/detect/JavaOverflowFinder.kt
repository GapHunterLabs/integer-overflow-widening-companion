package dev.gaphunter.integeroverflowwideningcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiAssignmentExpression
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiExpressionList
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReturnStatement
import com.intellij.psi.PsiTypeCastExpression
import com.intellij.psi.PsiTypes
import com.intellij.psi.PsiVariable
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.integeroverflowwideningcompanion.model.OverflowHit

/**
 * Finds an `int * int` or `int + int` expression assigned/passed
 * directly to a `long`-typed destination (a `long` local variable's
 * initializer, an assignment to a `long` variable/field, a `long`
 * method parameter, or a `long` method return) with no explicit
 * widening cast on either operand -- CWE-190, Integer Overflow or
 * Wraparound. The multiplication/addition happens in 32-bit `int`
 * arithmetic FIRST, silently truncating (often producing a negative
 * result) before the already-wrong value is widened to `long`.
 *
 * **v0.1 scope, stated honestly:** only the exact shape `int OP int`
 * assigned directly to a `long` destination -- never analyzes longer
 * expression chains, and never multiplication/addition of more than
 * two operands. General integer-range analysis is a research-level
 * problem; this stays honest by only publishing the syntactically
 * unambiguous form (a direct widening assignment with no intermediate
 * cast), resisting the temptation to generalize.
 */
object JavaOverflowFinder {

    fun findAll(file: PsiFile): List<OverflowHit> {
        val hits = mutableListOf<OverflowHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitBinaryExpression(expression: PsiBinaryExpression) {
                super.visitBinaryExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(expr: PsiBinaryExpression): OverflowHit? {
        val opText = expr.operationSign.text
        if (opText != "*" && opText != "+") return null

        val leftOperand = expr.lOperand
        val rightOperand = expr.rOperand ?: return null
        if (leftOperand.type != PsiTypes.intType() || rightOperand.type != PsiTypes.intType()) return null

        // A widening cast on either operand (`(long) a * b`) makes the whole
        // operation happen in 64-bit arithmetic -- safe, never flagged.
        if (leftOperand is PsiTypeCastExpression || rightOperand is PsiTypeCastExpression) return null

        if (!isDirectlyWidenedToLong(expr)) return null

        return OverflowHit(expr)
    }

    /** True when [expr] is used directly (no intermediate expression) somewhere its resulting value is widened to `long`. */
    private fun isDirectlyWidenedToLong(expr: PsiExpression): Boolean {
        return when (val parent = expr.parent) {
            is PsiVariable -> parent.type == PsiTypes.longType() && parent.initializer === expr
            is PsiAssignmentExpression -> parent.rExpression === expr && parent.lExpression.type == PsiTypes.longType()
            is PsiExpressionList -> isPassedToLongParameter(expr, parent)
            is PsiReturnStatement -> {
                val method = PsiTreeUtil.getParentOfType(parent, PsiMethod::class.java) ?: return false
                method.returnType == PsiTypes.longType()
            }
            else -> false
        }
    }

    private fun isPassedToLongParameter(expr: PsiExpression, argumentList: PsiExpressionList): Boolean {
        val call = argumentList.parent as? PsiCallExpression ?: return false
        val index = argumentList.expressions.indexOf(expr)
        if (index < 0) return false
        val method = call.resolveMethod() ?: return false
        val parameter = method.parameterList.parameters.getOrNull(index) ?: return false
        return parameter.type == PsiTypes.longType()
    }
}
