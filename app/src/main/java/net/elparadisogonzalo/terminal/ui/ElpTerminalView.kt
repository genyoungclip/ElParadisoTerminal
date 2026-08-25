package net.elparadisogonzalo.terminal.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import net.elparadisogonzalo.terminal.core.TerminalSession
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

/**
 * Minimal in-tree terminal view. In the real build this file is replaced by
 * the imported `terminal-view` module (Termux, GPL-3.0) which supports full
 * VT100 emulation, colors, mouse and touch. Kept here so the project compiles
 * standalone.
 */
class ElpTerminalView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val lines = CopyOnWriteArrayList<String>()
    private val paint = Paint().apply {
        color = Color.parseColor("#E6E6E6")
        typeface = Typeface.MONOSPACE
        textSize = 32f
        isAntiAlias = true
    }

    private var session: TerminalSession? = null
    private val banner  = StringBuilder()

    fun appendBanner(text: String) {
        banner.append(text); text.split("\n").forEach { if (it.isNotEmpty()) lines += it }
        postInvalidate()
    }

    fun attach(s: TerminalSession) {
        session = s
        thread(isDaemon = true) {
            val buf = ByteArray(4096)
            val ins = s.output ?: return@thread
            while (true) {
                val n = try { ins.read(buf) } catch (_: Throwable) { -1 }
                if (n <= 0) break
                val text = String(buf, 0, n)
                text.split("\n").forEach { if (it.isNotEmpty()) lines += it }
                postInvalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0F1015"))
        val visible = lines.takeLast((height / 40).coerceAtLeast(1))
        var y = paint.textSize
        for (l in visible) { canvas.drawText(l, 16f, y, paint); y += 40f }
    }
}
