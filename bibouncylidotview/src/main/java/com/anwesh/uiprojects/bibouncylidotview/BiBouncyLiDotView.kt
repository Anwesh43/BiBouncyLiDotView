package com.anwesh.uiprojects.bibouncylidotview

/**
 * Created by anweshmishra on 30/11/19.
 */

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.MotionEvent

val nodes : Int = 5
val parts : Int = 2
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#4CAF50")
val scGap : Float = 0.02f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val rFactor : Float = 3.5f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBiBouncyLiDot(scale : Float, w : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sc : Float = scale.divideScale(1, 2)
    for (j in 0..(parts - 1)) {
        save()
        scale(1f - 2 * j, 1f)
        drawLine(0f, 0f, size * sf, 0f, paint)
        save()
        translate(size, 0f)
        drawCircle((w - size) * sc, 0f, size / rFactor, paint)
        restore()
        restore()
    }
}

fun Canvas.drawBBLDNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, gap * (i + 1))
    drawBiBouncyLiDot(scale, w / 2, size, paint)
    restore()
}

class BiBouncyLiDotView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb :(Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            cb()
            try {
                Thread.sleep(delay)
                view.invalidate()
            } catch(ex : Exception) {

            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BBLDNode(var i : Int, val state : State = State()) {

        private var next : BBLDNode? = null
        private var prev : BBLDNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BBLDNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBBLDNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BBLDNode {
            var curr : BBLDNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BiBouncyLiDot(var i : Int) {

        private val root : BBLDNode = BBLDNode(0)
        private var curr : BBLDNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BiBouncyLiDotView) {

        private val animator : Animator = Animator(view)
        private val bbld : BiBouncyLiDot = BiBouncyLiDot(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bbld.draw(canvas, paint)
            animator.animate {
                bbld.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bbld.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity: Activity) : BiBouncyLiDotView {
            val view : BiBouncyLiDotView = BiBouncyLiDotView(activity)
            activity.setContentView(view)
            return view
        }
    }
}