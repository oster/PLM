package plm.universe.lander


import plm.universe.{Entity, SvgGenerator, World, WorldView}
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.awt.geom.Line2D
import java.awt.BasicStroke
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import Math.PI
import java.awt.geom.QuadCurve2D

import scala.util.Random
import java.awt.Font
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseAdapter
import java.awt.geom.Point2D
import java.io.{OutputStreamWriter, StringWriter, Writer}

import org.apache.batik.svggen.{SVGGraphics2D, SVGGraphics2DIOException}
import plm.universe.lander.LanderWorld.State.{CRASHED, FLYING, LANDED, OUT}


object LanderWorldView {
  private val LANDER_SHAPE = List(
    List(Point(-2, 3), Point(-3, 4), Point(-3, 7), Point(-2, 8), Point(2, 8), Point(3, 7),
      Point(3, 4), Point(2, 3), Point(-2, 3)),
    List(Point(-6, 0), Point(-4, 0)),
    List(Point(6, 0), Point(4, 0)),
    List(Point(-5, 0), Point(-4, 2.8), Point(-2, 3)),
    List(Point(5, 0), Point(4, 2.8), Point(2, 3)))

  private val EXPLOSION_SHAPE = List(
    Point(-4, 3), Point(-1, 2), Point(0, 4), Point(1, 2), Point(4, 2), Point(2, 0),
    Point(3, -2), Point(0, -1), Point(-3, -3), Point(-2, 0), Point(-4, 3))



  private var mouseIn: Boolean = false
  private var mousePos: Point = Point(0, 0)


  def draw(delegLandWorld: DelegatingLanderWorld, width: Int, height: Int): String = { // Ask the test to render into the SVG Graphics2D implementation.

    val svgGenerator: SVGGraphics2D = new SVGGraphics2D(SvgGenerator.document)
    paintComponent(svgGenerator, delegLandWorld, width, height)

    //***********AFFICHAGE SUR LA CONSOLE******************

    // Finally, stream out SVG to the standard output using
    // UTF-8 encoding.
    val useCSS: Boolean = true
    // we want to use CSS style attributes
    val out: Writer = new OutputStreamWriter(System.out, "UTF-8")
    try {
      svgGenerator.stream(out, useCSS)
    } catch {
      case e: SVGGraphics2DIOException =>
        e.printStackTrace()
    }
    //************Fiiiin AFFICHAGE SUR LA CONSOLE***************

    val writer = new StringWriter
    try
      svgGenerator.stream(writer)
    catch {
      case e: SVGGraphics2DIOException =>
        e.printStackTrace()
    }
    val str = writer.getBuffer.toString
    str
  }

  def paintComponent(g: Graphics, delegLandWorld: DelegatingLanderWorld, width: Int, height: Int) = {
    //super.paintComponent(g)
    val g2 = g.asInstanceOf[Graphics2D]
    new Painter(g2, delegLandWorld, width, height).paint()
  }


  private class Painter(g2: Graphics2D, delegLandWorld: DelegatingLanderWorld, width: Int, height: Int) {
    def paint() {
      val initialTransform = g2.getTransform()
      setupRendering()
      clearWidgetBackground()
      setupGlobalTransform()
      clearWorldBackground()
      drawGround()
      drawStats(initialTransform)
      delegLandWorld.realWorld.state match {
        case FLYING => drawLander(drawFlame = true)
        case LANDED => drawLander(drawFlame = false)
        case OUT => drawQuestionMarks()
        case CRASHED => drawExplosion()
      }
    }

    def setupRendering() {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    def clearWidgetBackground() = {
      g2.setColor(Color.gray)
      g2.fill(new Rectangle2D.Double(0, 0, width, height))
    }

    def setupGlobalTransform() = {
      val scale = (width.toDouble / delegLandWorld.realWorld.width) min (height.toDouble / delegLandWorld.realWorld.height)
      val translateX = (width - delegLandWorld.realWorld.width * scale) / 2
      val translateY = (height + delegLandWorld.realWorld.height * scale) / 2
      g2.translate(translateX, translateY)
      g2.scale(scale, -scale)
    }

    def clearWorldBackground() = {
      g2.setColor(Color.black)
      g2.fill(new Rectangle2D.Double(0, 0, delegLandWorld.realWorld.width, delegLandWorld.realWorld.height))
    }

    /** Sets the line width to 1px no matter the transform, and the color to white. */
    def resetPen() {
      val transform = g2.getTransform()
      val scale = (transform.getScaleX() min transform.getScaleY()).abs
      g2.setStroke(new BasicStroke((1 / scale).toFloat))
      g2.setColor(Color.white)
    }

    def drawPath(path: List[Point], fill: Boolean) {
      if (!path.isEmpty) {
        val polyLine = new GeneralPath(Path2D.WIND_EVEN_ODD, path.length)
        polyLine.moveTo(path.head.x, path.head.y)
        for (point <- path.tail) {
          polyLine.lineTo(point.x, point.y)
        }
        if (fill) g2.fill(polyLine) else g2.draw(polyLine)
      }
    }

    def drawText(text: String, x: Double, y: Double) {
      val oldTransform = g2.getTransform()
      g2.translate(x, y)
      g2.scale(1, -1)
      g2.drawString(text, 0, 0)
      g2.setTransform(oldTransform)
    }

    def drawGround() = {
      resetPen()
      drawPath(delegLandWorld.realWorld.ground, fill = false)
    }

    def randomScaleFactor() = 1 + Random.nextDouble() * 0.2

    def drawLander(drawFlame: Boolean) = {
      val oldTransform = g2.getTransform()
      g2.translate(delegLandWorld.realWorld.position.x, delegLandWorld.realWorld.position.y)
      g2.scale(6, 6) // the lander shape is small
      resetPen()
      g2.rotate(delegLandWorld.realWorld.angleRadian - PI / 2)
      LanderWorldView.LANDER_SHAPE.foreach(drawPath(_, fill = false))
      val thrust = delegLandWorld.realWorld.thrust
      if (drawFlame && thrust > 0) {
        val controlX = (0.4 + thrust * 0.1) * randomScaleFactor()
        val endY = (-2 * thrust) * randomScaleFactor()
        val controlY = (endY + 3) / 2
        g2.draw(new QuadCurve2D.Double(-0.25, 3, -controlX, controlY, 0, endY))
        g2.draw(new QuadCurve2D.Double(0.25, 3, controlX, controlY, 0, endY))
      }
      g2.setTransform(oldTransform)
    }

    def drawExplosion() = {
      val oldTransform = g2.getTransform()
      g2.translate(delegLandWorld.realWorld.position.x, delegLandWorld.realWorld.position.y)
      g2.scale(15, 15) // the explosion shape is small
      resetPen()
      drawPath(LanderWorldView.EXPLOSION_SHAPE, fill = true)
      g2.setTransform(oldTransform)
    }

    def drawQuestionMarks() = {
      val x = delegLandWorld.realWorld.position.x
      val y = delegLandWorld.realWorld.position.y
      val textX = if (x >= delegLandWorld.realWorld.width) delegLandWorld.realWorld.width - 100 else if (x <= 0) 5 else x
      val textY = if (y >= delegLandWorld.realWorld.height) delegLandWorld.realWorld.height - 40 else if (y <= 0) 5 else y - 30
      g2.setColor(Color.WHITE)
      g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40))
      drawText("???", textX, textY)
    }

    def drawStats(initialTransform: AffineTransform) {
      g2.setColor(Color.LIGHT_GRAY)
      g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 30))
      drawText(f"x: ${delegLandWorld.realWorld.position.x}%.2f", 5, delegLandWorld.realWorld.height - 30)
      drawText(f"y: ${delegLandWorld.realWorld.position.y}%.2f", 5, delegLandWorld.realWorld.height - 2 * 30)
      drawText(f"speed x: ${delegLandWorld.realWorld.speed.x}%.2f", 5, delegLandWorld.realWorld.height - 3 * 30)
      drawText(f"speed y: ${delegLandWorld.realWorld.speed.y}%.2f", 5, delegLandWorld.realWorld.height - 4 * 30)
      drawText(f"angle: ${delegLandWorld.realWorld.angle}%.2f°", 5, delegLandWorld.realWorld.height - 5 * 30)
      drawText(f"thrust: ${delegLandWorld.realWorld.thrust}", 5, delegLandWorld.realWorld.height - 6 * 30)
      drawText(f"fuel: ${delegLandWorld.realWorld.fuel}", 5, delegLandWorld.realWorld.height - 7 * 30)

      if (mouseIn) {
        val deltaTransform = new AffineTransform(g2.getTransform())
        deltaTransform.invert()
        deltaTransform.concatenate(initialTransform)
        val coord = deltaTransform.transform(new Point2D.Double(mousePos.x, mousePos.y), null)
        drawText(f"x: ${coord.getX.round}", delegLandWorld.realWorld.width - 130, delegLandWorld.realWorld.height - 30)
        drawText(f"y: ${coord.getY.round}", delegLandWorld.realWorld.width - 130, delegLandWorld.realWorld.height - 2 * 30)
      }
    }

  }



  }


class LanderWorldView(delagatingWorld: DelegatingLanderWorld) extends WorldView(delagatingWorld) {

  import LanderWorld.State._



}



