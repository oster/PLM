package plm.universe.dutchflag;



import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import plm.core.log.Logger;
import plm.core.utils.FileUtils;
import plm.universe.baseball.BaseballWorld;
import plm.universe.baseball.baseBallView;
import plm.universe.hanoi.HanoiDisk;
import plm.universe.hanoi.HanoiWorldView;
import recursion.hanoi.CyclicHanoiEntity;

import java.awt.*;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Vector;

public class drawDutchFlagSVG {

    public static void main(String[] args) throws UnsupportedEncodingException {
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);


        // Ask the test to render into the SVG Graphics2D implementation.
       // DutchFlagWorldView myWorlds = new DutchFlagWorldView(new FileUtils(ClassLoader.getSystemClassLoader()), "6 lines",6);

        DutchFlagWorld myworld = new DutchFlagWorld(new FileUtils(ClassLoader.getSystemClassLoader()), "300 lines",300);

        myworld.draw();

        //Logger.info(test);
        //Logger.info(myworld.draw().get(0).getOperation());


    }
}
