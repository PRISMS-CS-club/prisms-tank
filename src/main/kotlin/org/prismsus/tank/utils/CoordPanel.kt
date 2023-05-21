package org.prismsus.tank.utils

import org.prismsus.tank.utils.collidable.DPos2
import org.prismsus.tank.utils.collidable.Collidable
import org.prismsus.tank.utils.collidable.ColRect
import java.awt.*
import java.text.DecimalFormat
import javax.swing.JPanel
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.math.*
import kotlin.math.roundToInt

class CoordPanel(
    val gridInterv : IDim2,
    val factor : IDim2,
    val originPos : IPos2 = IPos2(0, 0)
) : JPanel() {

    val toDraw : ArrayList<Shape> = ArrayList<Shape>()

    fun drawCollidable(vararg Collidables : Collidable) {
        for (collidable in Collidables) {
            val curShape = collidable.toShape(coordTransformer, shapeModifier)
            toDraw.add(curShape)
            gmodifiers.add(graphicsModifier)
        }
    }

    override fun paintComponent(g: Graphics) {
        val xGridInterval = gridInterv.x
        val yGridInterval = gridInterv.y
        val xScaleFactor = factor.x
        val yScaleFactor = factor.y

        super.paintComponent(g)

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val width = size.width
        val height = size.height

        g2d.background = Color.WHITE
        g2d.clearRect(0, 0, width, height)

        // Adjust coordinate system to match math coordinate system
        val centerX = (width.toDouble() / 2).roundToInt()
        val centerY = (height.toDouble() / 2).roundToInt() // used for translating the coordinate system



        val transatedCentX = originPos.x
        val transatedCentY = originPos.y
        val scaledTranX = transatedCentX * xScaleFactor
        val scaledTranY = transatedCentY * yScaleFactor
        val maxAbsX = centerX + abs(scaledTranX)
        val minX = - maxAbsX
        val maxX = maxAbsX
        val maxAbsY = centerY + abs(scaledTranY)
        val minY = - maxAbsY
        val maxY = maxAbsY

        g2d.translate(centerX, centerY)
        g2d.color = Color.BLACK
        g2d.stroke = BasicStroke(2f)
        // Draw x-axis
        g2d.color = Color.BLACK
        g2d.drawLine(minX, -transatedCentY * yScaleFactor, maxX, -transatedCentY * yScaleFactor)

        // Draw y-axis
        g2d.drawLine(transatedCentX * xScaleFactor, minY, transatedCentX * xScaleFactor, maxY)


        g2d.font = Font("Arial", Font.PLAIN, 11)
        g2d.color = Color.BLACK
        // Draw grid lines
        g2d.color = Color.LIGHT_GRAY
        g2d.stroke = BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, floatArrayOf(5f), 0f)
        val format = DecimalFormat("#.##")
        for (xOff in (xGridInterval * xScaleFactor) until max(abs(maxX), abs(minX)) step (xGridInterval * xScaleFactor)) {
            // xOff is the offset from the center vertical line
            val posX = xOff + scaledTranX
            val negX = -xOff + scaledTranX
            g2d.drawLine(posX, minY, posX, maxY)
            g2d.drawLine(negX, minY, negX, maxY)
            val posStr = (transatedCentX + xOff / xScaleFactor).toString()
            val negStr = (transatedCentX - xOff / xScaleFactor).toString()
            val lableWidth = g2d.fontMetrics.stringWidth(posStr)
            val posLabelX = posX - lableWidth / 2
            val negLabelX = negX - lableWidth / 2
            val labelHeight = g2d.fontMetrics.height
            g2d.drawString(posStr, posLabelX, -labelHeight - scaledTranY)
            g2d.drawString(negStr, negLabelX, -labelHeight - scaledTranY)
        }

        for (yOff in (yGridInterval * yScaleFactor) until max(abs(maxY), abs(minY)) step (yGridInterval * yScaleFactor)) {
            // yOff is the offset from the center horizontal line
            val posY = -(yOff + scaledTranY) // negate because y axis is flipped
            val negY = -(-yOff + scaledTranY)
            g2d.drawLine(minX, posY, maxX, posY)
            g2d.drawLine(minX, negY, maxX, negY)
            val posStr = (transatedCentY + yOff / yScaleFactor).toString()
            val negStr = (transatedCentY - yOff / yScaleFactor).toString()
            val labelWidth = g2d.fontMetrics.stringWidth(posStr)
            val labelHeight = g2d.fontMetrics.height
            val posLabelY = posY + labelHeight / 2
            val negLabelY = negY + labelHeight / 2
            g2d.drawString(posStr, -labelWidth - 5 + scaledTranX , posLabelY )
            g2d.drawString(negStr, -labelWidth - 5 + scaledTranX, negLabelY)
            // y axis is flipped
        }
        // draw all the shapes
        g2d.color = Color.BLACK
        g2d.stroke = BasicStroke(2f)
        for (i in 0 until toDraw.size) {
            val shape = toDraw[i]
            val gmodifier = gmodifiers[i]
            gmodifier(g2d)
            g2d.draw(shape)
        }
    }

    val coordTransformer
        get() = {pos : DPos2 -> DPos2(pos.x * factor.x + originPos.x, -(pos.y * factor.y + originPos.y))}
    var shapeModifier : (Shape) -> Unit = {it}
    var graphicsModifier : (Graphics2D) -> Unit = {it}
    var gmodifiers : ArrayList<(Graphics2D) -> Unit> = ArrayList()

    companion object {
        const val DEFAULT_WIDTH = 800
        const val DEFAULT_HEIGHT = 800
    }

    fun showFrame(framModifier : (JFrame) -> JFrame = {it}){
        SwingUtilities.invokeLater{
            val frame = JFrame("Coordinate System")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(CoordPanel.DEFAULT_WIDTH, CoordPanel.DEFAULT_HEIGHT)
            frame.contentPane.add(this)
            framModifier(frame)
            frame.isVisible = true
        }
    }
}


    fun main() {
        val panel : CoordPanel = CoordPanel(IDim2(1, 1), IDim2(15, 15))
        val box = ColRect(DPos2(0.0, 0.0), DDim2(10, 10))
        val pt = DPos2(4.0, 4.0)
        panel.drawCollidable(box, pt)
        panel.showFrame()
    }

