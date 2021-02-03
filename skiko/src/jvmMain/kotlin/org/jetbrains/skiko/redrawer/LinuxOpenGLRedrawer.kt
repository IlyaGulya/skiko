package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.OpenGLApi
import org.jetbrains.skiko.SkikoProperties

internal class LinuxOpenGLRedrawer(
    private val layer: HardwareLayer
) : Redrawer {
    private val context = layer.lockDrawingSurface {
        it.createContext()
    }
    private var isDisposed = false

    override fun dispose() {
        check(!isDisposed)
        layer.lockDrawingSurface {
            it.destroyContext(context)
        }
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed)
        toRedraw.add(this)
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() = layer.lockDrawingSurface {
        check(!isDisposed)
        update(System.nanoTime())
        it.makeCurrent(context)
        draw()
        it.setSwapInterval(0)
        it.swapBuffers()
        OpenGLApi.instance.glFinish()
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        layer.draw()
    }

    companion object {
        private val toRedraw = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawCopy = mutableSetOf<LinuxOpenGLRedrawer>()
        private val toRedrawAlive = toRedrawCopy.asSequence().filterNot(LinuxOpenGLRedrawer::isDisposed)

        private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
            toRedrawCopy.clear()
            toRedrawCopy.addAll(toRedraw)
            toRedraw.clear()

            val nanoTime = System.nanoTime()

            for (redrawer in toRedrawAlive) {
                try {
                    redrawer.update(nanoTime)
                } catch (e: CancellationException) {
                    // continue
                }
            }

            val drawingSurfaces = toRedrawAlive.map { lockDrawingSurface(it.layer) }.toList()
            try {
                toRedrawAlive.forEachIndexed { index, redrawer ->
                    drawingSurfaces[index].makeCurrent(redrawer.context)
                    redrawer.draw()
                }

                toRedrawAlive.forEachIndexed { index, _ ->
                    // it is ok to set swap interval every frame, there is no performance overhead
                    drawingSurfaces[index].setSwapInterval(if (SkikoProperties.vsyncEnabled) 1 else 0)
                    drawingSurfaces[index].swapBuffers()
                }

                toRedrawAlive.forEachIndexed { index, redrawer ->
                    drawingSurfaces[index].makeCurrent(redrawer.context)
                    OpenGLApi.instance.glFinish()
                }
            } finally {
                drawingSurfaces.forEach(::unlockDrawingSurface)
            }
        }
    }
}

private inline fun <T> HardwareLayer.lockDrawingSurface(action: (DrawingSurface) -> T): T {
    val drawingSurface = lockDrawingSurface(this)
    try {
        return action(drawingSurface)
    } finally {
        unlockDrawingSurface(drawingSurface)
    }
}

private fun lockDrawingSurface(layer: HardwareLayer): DrawingSurface {
    val ptr = lockDrawingSurfaceNative(layer)
    return DrawingSurface(ptr, getDisplay(ptr), getWindow(ptr))
}

private fun unlockDrawingSurface(drawingSurface: DrawingSurface) {
    unlockDrawingSurfaceNative(drawingSurface.ptr)
}

private class DrawingSurface(
    val ptr: Long,
    val display: Long,
    val window: Long
) {
    fun createContext() = createContext(display)
    fun destroyContext(context: Long) = destroyContext(display, context)
    fun makeCurrent(context: Long) = makeCurrent(display, window, context)
    fun swapBuffers() = swapBuffers(display, window)
    fun setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)
}

private external fun lockDrawingSurfaceNative(layer: HardwareLayer): Long
private external fun unlockDrawingSurfaceNative(drawingSurface: Long)
private external fun getDisplay(drawingSurface: Long): Long
private external fun getWindow(drawingSurface: Long): Long

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun createContext(display: Long): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)