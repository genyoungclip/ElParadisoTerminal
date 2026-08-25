package net.elparadisogonzalo.terminal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

/** Row of extra keys under the terminal: ESC TAB CTRL ALT ↑ ↓ ← → */
class ExtraKeysView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var listener: ((String) -> Unit)? = null

    fun setOnKeyPress(l: (String) -> Unit) { listener = l }

    init {
        orientation = HORIZONTAL
        gravity     = Gravity.CENTER_VERTICAL
        val keys = listOf(
            "ESC" to "\u001B",
            "TAB" to "\t",
            "CTRL" to "",
            "ALT"  to "",
            "↑"   to "\u001B[A",
            "↓"   to "\u001B[B",
            "←"   to "\u001B[D",
            "→"   to "\u001B[C",
            "/"   to "/",
            "|"   to "|"
        )
        for ((label, seq) in keys) {
            val b = Button(context).apply {
                text = label
                setOnClickListener { listener?.invoke(seq) }
            }
            addView(b)
        }
    }
}
