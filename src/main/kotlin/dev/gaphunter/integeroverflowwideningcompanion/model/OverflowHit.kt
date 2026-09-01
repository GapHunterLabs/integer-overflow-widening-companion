package dev.gaphunter.integeroverflowwideningcompanion.model

import com.intellij.psi.PsiElement

/** One `int OP int` expression (`*` or `+`) assigned/passed directly to a `long`-typed destination with no widening cast on either operand. */
data class OverflowHit(val anchor: PsiElement)
