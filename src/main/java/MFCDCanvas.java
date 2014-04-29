import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.function.BiConsumer;
import javax.swing.JPanel;

/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class MFCDCanvas extends JPanel implements Observer
{
    private static final int FONT_SIZE_OSB = 20;
    private static final int FONT_SIZE_SMALL = 14;
    private static final int WP_BOX_FONTSIZE = 14;
    private static final int OSB_TEXT_MARGIN = 15;
    private static final int OSB_TEXT_BORDER = 5;
    private static final int NAV_MAP_BORDER = 50;
    private static final float DASH_SIZE = 5f;
    private static final Color COLOR_WARNBOX = Color.YELLOW;
    private static final Color COLOR_FORE = Color.GREEN;
    private static final Color COLOR_BACK = Color.BLACK;
    private static final Color COLOR_WP_SELECTED = Color.WHITE;
    private static final Color COLOR_MK = Color.YELLOW;
    private static final Color COLOR_RUNWAY = Color.WHITE;
    
    private static final int OSB01_X = 90;
    private static final int OSB02_X = 170;
    private static final int OSB03_X = 245;
    private static final int OSB04_X = 325;
    private static final int OSB05_X = 400;
    
    private static final int OSB11_X = OSB05_X;
    private static final int OSB12_X = OSB04_X;
    private static final int OSB13_X = OSB03_X;
    private static final int OSB14_X = OSB02_X;
    private static final int OSB15_X = OSB01_X;
    
    private static final int OSB06_Y = 120;
    private static final int OSB07_Y = 190;
    private static final int OSB08_Y = 265;
    private static final int OSB09_Y = 330;
    private static final int OSB10_Y = 405;
    
    private static final int OSB20_Y = OSB06_Y;
    private static final int OSB19_Y = OSB07_Y;
    private static final int OSB18_Y = OSB08_Y;
    private static final int OSB17_Y = OSB09_Y;
    private static final int OSB16_Y = OSB10_Y;
    
    private static final char CHAR_PLUSMINUS = '±';
    private static final char CHAR_ARROWS_HORIZONTAL = '↔';
    private static final char CHAR_ARROWS_VERTICAL = '↕';
    private static final char CHAR_ARROW_RIGHT = '→';
    private static final char CHAR_ARROW_LEFT = '←';
    private static final char CHAR_ARROW_TOP = '↑';
    private static final char CHAR_ARROW_BOTTOM = '↓';
    
    private static final String SYMBOLTEST = "SYMBOLS\n["
        +CHAR_PLUSMINUS
        +CHAR_ARROWS_HORIZONTAL
        +CHAR_ARROWS_VERTICAL
        +CHAR_ARROW_RIGHT
        +CHAR_ARROW_LEFT
        +CHAR_ARROW_TOP 
        +CHAR_ARROW_BOTTOM+ "]";

    final MFCDStatus status;
    final Map<MFCDStatus.Page, BiConsumer<Graphics, Rectangle>> pageMaps;
    
    public MFCDCanvas(MFCDStatus status)
    {
        if (status == null)
            throw new IllegalArgumentException("status is null");
        this.status = status;
        this.status.addObserver(this);
        pageMaps = new HashMap<>();
        pageMaps.put(MFCDStatus.Page.TST, this::drawPage_TEST);
        pageMaps.put(MFCDStatus.Page.POS, this::drawPage_POS);
        pageMaps.put(MFCDStatus.Page.NAV, this::drawPage_NAV);
        pageMaps.put(MFCDStatus.Page.STG, this::drawPage_STG);
        pageMaps.put(MFCDStatus.Page.ENG, this::drawPage_ENG);
        pageMaps.put(MFCDStatus.Page.WPT, this::drawPage_WPT);
    }

    @Override
    public void setSize(Dimension d)
    {
        super.setSize(d);
        onResize();
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);
        onResize();
    }
    
    public void onResize()
    {
        recalculateF(getWidth());
    }
    
    @Override
    public void update(Observable o, Object arg)
    {
        EventQueue.invokeLater(()-> {repaint();});
    }
    
    @Override
    public void paintComponent(Graphics _g)
    {
        Rectangle bounds = new Rectangle();
        bounds.width = getWidth();
        bounds.height = getHeight();

        Graphics g = _g;
        try
        {
            // Clear out the screen
            g.setFont(fontOSB);
            g.setColor(Color.BLACK);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

            // DRAW PIECES
            // Each layer is drawn on top of the other in that order.
            // Draw the page names at the bottom
            drawPageNames(g, bounds);

            // Draw the current page
            drawCurrentPage(g, bounds);

            // Draw disconnected if no connection available
            drawConnectionStatus(g, bounds);
        }
        finally
        {
            g.dispose();
        }
    }
    

    public void drawPage_TEST(Graphics g, Rectangle bounds)
    {
        Utils.writeAtCenter(g, bounds, SYMBOLTEST);
    }

    public void drawPage_NAV(Graphics g, Rectangle bounds)
    {
        String msg;
        Rectangle2D b;
        
        // SET DEFAULT COLOR
        g.setColor(Color.GREEN);
        
        // CENTER OF THE SCREEN
        int centerx = (int) (bounds.getX() + bounds.getWidth()/2);
        int centery = (int) (bounds.getY() + bounds.getHeight()/2);

        // DRAW BE REFERENCE IN THE SCREEN CORNER
        msg = status.getBeBRAStr();
        if (status.isBullseyeInverted())
            msg = CHAR_ARROW_LEFT+" "+msg;
        else
            msg = CHAR_ARROW_RIGHT+" "+msg;
        b = g.getFontMetrics().getStringBounds(msg, g);
        Font oldFont = g.getFont();
        g.setFont(fontSmall);
        g.drawString(msg,
            (int) (bounds.x + scale(OSB_TEXT_MARGIN)),
            (int) (bounds.y + scale(OSB_TEXT_MARGIN) + b.getHeight()));
        g.setFont(oldFont);
        
        // BIG CIRCLE MEASUREMENTS
        int largeCircleX = (int) (bounds.getX() + scale(NAV_MAP_BORDER));
        int largeCircleY = (int) (bounds.getY() + scale(NAV_MAP_BORDER));
        int largeCircleW = (int) (bounds.getWidth() - scale(NAV_MAP_BORDER*2));
        int largeCircleH = (int) (bounds.getHeight() - scale(NAV_MAP_BORDER*2));
        g.drawArc(largeCircleX, largeCircleY, largeCircleW, largeCircleH, 0, 360);
        
        // SMALL CIRCLE MEASUREMENTS
        int smallCircleX = largeCircleX + largeCircleW/4;
        int smallCircleY = largeCircleY + largeCircleH/4;
        int smallCircleW = largeCircleW/2;
        int smallCircleH = largeCircleW/2;
        g.drawArc(smallCircleX, smallCircleY, smallCircleW, smallCircleH, 0, 360);
                
        // Draw the north indicator and other cardinal helpers
        {
            double degree = status.getSimData().getHeading() -360;
            degree += 90; // sin/cos circle starts from RIGHT
            int starttx = (int)(centerx + Math.cos(Math.toRadians(degree)) * smallCircleW/2);
            int starty = (int)(centery - Math.sin(Math.toRadians(degree)) * smallCircleW/2);
            int endx = (int)(centerx + Math.cos(Math.toRadians(degree)) * (smallCircleW/2 + scale(16)));
            int endy = (int)(centery - Math.sin(Math.toRadians(degree)) * (smallCircleW/2 + scale(16)));
            {
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(thick);
                g.drawLine(starttx, starty, endx, endy);
                g2.setStroke(oldStroke);
            }
            // internals
            for (int i = 0; i < 4; i++)
            {
                degree += 90;
                starttx = (int)(centerx + Math.cos(Math.toRadians(degree)) * smallCircleW/2);
                starty = (int)(centery - Math.sin(Math.toRadians(degree)) * smallCircleW/2);
                endx = (int)(centerx + Math.cos(Math.toRadians(degree)) * (smallCircleW/2 - scale(8)));
                endy = (int)(centery - Math.sin(Math.toRadians(degree)) * (smallCircleW/2 - scale(8)));
                g.drawLine(starttx, starty, endx, endy);
            }
        }
        
        // DRAW THE VIEWING RADIUS SIZE
        msg = status.getPageNAVRadiusStr();
        b = g.getFontMetrics().getStringBounds(msg, g);
        g.drawString(msg,
            (int) (bounds.x + bounds.width - b.getWidth() - scale(OSB_TEXT_MARGIN*2)),
            (int) (bounds.y + + b.getHeight() + scale(OSB_TEXT_MARGIN)));

        // DATA RECARGIND THE RADIUS OF THE MAP
        int mapRadiusPX = largeCircleW/2;
        int mapRadius = status.getPageNAVRadius();
        
        // DRAW WAYPOINTS - DRAW ALL AND CONNECT POINTS
        {
            Map<Integer, double[]> wps = status.getSimData().getWaypoints();
            int prevx = 0;
            int prevy = 0;
            boolean prevOutside = true;
            boolean first = true;
            // FIRST DRAW ALL LINES
            for (Map.Entry<Integer, double[]> e: wps.entrySet())
            {
                double[] v = e.getValue();
                double deltaBearing = status.getBearingDelta(status.getBearingToPoint(v[0], v[1]));
                deltaBearing += 90; // sin/cos circle starts from RIGHT
                double distance = status.getDistanceToPoint(v[0], v[1]);
                boolean endsOutside = distance > mapRadius;
                double distancePX = distance * mapRadiusPX / mapRadius;
                int endx = (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * distancePX);
                int endy = (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * distancePX);
                if (first)
                {
                    first = false;
                    prevx = endx;
                    prevy = endy;
                    prevOutside = endsOutside;
                    continue;
                }
                
                // NO NEED TO DRAW
                if (prevOutside && endsOutside)
                {
                    prevx = endx;
                    prevy = endy;
                    prevOutside = endsOutside;
                    continue;
                }

                // DRAW THE CONNECTING LINE FIRST
                // So that the square goes over it.
                g.setColor(COLOR_FORE);
                // Intersect with the circle somewhere?
                int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                if (prevOutside)
                {
                    MathUtils.Point p1 = new MathUtils.Point(prevx, prevy);
                    MathUtils.Point p2 = new MathUtils.Point(endx, endy);
                    MathUtils.Point pc = new MathUtils.Point(centerx, centery);
                    List<MathUtils.Point> p = MathUtils.getCircleLineIntersectionPoint(p1, p2, pc, mapRadiusPX);
                    if (p.size() < 2)
                        continue;
                    x1 = (int) p.get(0).x;
                    y1 = (int) p.get(0).y;
                }
                else
                {
                    x1 = prevx;
                    y1 = prevy;
                }
                if (endsOutside)
                {
                    MathUtils.Point p1 = new MathUtils.Point(prevx, prevy);
                    MathUtils.Point p2 = new MathUtils.Point(endx, endy);
                    MathUtils.Point pc = new MathUtils.Point(centerx, centery);
                    List<MathUtils.Point> p = MathUtils.getCircleLineIntersectionPoint(p1, p2, pc, mapRadiusPX);
                    if (p.size() < 2)
                        continue;
                    x2 = (int) p.get(1).x;
                    y2 = (int) p.get(1).y;
                }
                else
                {
                    x2 = endx;
                    y2 = endy;
                }

                g.drawLine(x1, y1, x2, y2);
                
                prevx = endx;
                prevy = endy;
                prevOutside = endsOutside;
            }
            // THEN DRAW ALL SQUARES
            wps.entrySet().stream().forEach((e) ->
            { 
                Integer k = e.getKey();
                double[] v = e.getValue();
                double deltaBearing = status.getBearingDelta(status.getBearingToPoint(v[0], v[1]));
                deltaBearing += 90;
                double distance = status.getDistanceToPoint(v[0], v[1]);
                boolean outside = distance > mapRadius;
                double distancePX = distance * mapRadiusPX / mapRadius;
                int endx = (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * distancePX);
                int endy = (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * distancePX);
                if (!(outside))
                {
                    String s = String.valueOf(k);
                    if (MFCDStatus.MetricSystem.IMPERIAL.equals(status.getMetricSystem()))
                        s = String.valueOf(k-1);
                    
                    g.setFont(fontWP);
                    Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
                    int maxD = (int) (rect.getWidth() > rect.getHeight() ? rect.getWidth() : rect.getHeight());
                    if (status.getSimData().getCurWaypointNum() == k)
                        g.setColor(COLOR_WP_SELECTED);
                    else
                        g.setColor(COLOR_FORE);
                    g.fillRect(
                        (int) (endx - maxD/2),
                        (int) (endy - maxD/2),
                        (int) maxD,
                        (int) maxD
                    );
                    g.setColor(COLOR_BACK);
                    g.fillRect(
                        (int) (endx - maxD/2) + 1,
                        (int) (endy - maxD/2) + 1,
                        (int) maxD - 2,
                        (int) maxD - 2
                    );
                    if (status.getSimData().getCurWaypointNum() == k)
                        g.setColor(COLOR_WP_SELECTED);
                    else
                        g.setColor(COLOR_FORE);
                    g.drawString(s,
                        (int) (endx - rect.getWidth()/2),
                        (int) (endy + rect.getHeight()/4)
                    );
                    g.setFont(fontOSB);
                }
            });
            // THEN MARKPOINTS SQUARES
            List<double[]> mks = status.getMarkpoints();
            for (int i = 0; i < mks.size(); i++)
            { 
                double[] v = mks.get(i);
                double deltaBearing = status.getBearingDelta(status.getBearingToPoint(v[0], v[1]));
                deltaBearing += 90;
                double distance = status.getDistanceToPoint(v[0], v[1]);
                boolean outside = distance > mapRadius;
                double distancePX = distance * mapRadiusPX / mapRadius;
                int endx = (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * distancePX);
                int endy = (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * distancePX);
                if (!(outside))
                {
                    String s = "M"+String.valueOf(i+1);
                    
                    g.setFont(fontWP);
                    Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
                    int maxD = (int) (rect.getWidth() > rect.getHeight() ? rect.getWidth() : rect.getHeight());
                    g.setColor(COLOR_MK);
                    g.fillRect(
                        (int) (endx - maxD/2 - scale(3)),
                        (int) (endy - maxD/2 - scale(3)),
                        (int) (maxD + scale(6)),
                        (int) (maxD + scale(6))
                    );
                    g.setColor(COLOR_BACK);
                    g.fillRect(
                        (int) (endx - maxD/2 - scale(3)) + 1,
                        (int) (endy - maxD/2 - scale(3)) + 1,
                        (int) (maxD + scale(6)) - 2,
                        (int) (maxD + scale(6)) - 2
                    );
                    g.setColor(COLOR_MK);
                    g.drawString(s,
                        (int) (endx - rect.getWidth()/2),
                        (int) (endy + rect.getHeight()/4)
                    );
                    g.setFont(fontOSB);
                }
            }
        }
        
        g.setColor(COLOR_FORE);
        
        // DRAW BULLSEYE VISUAL REFERENCE
        {
            double deltaBearing = status.getBEBearingDelta();
            deltaBearing += 90; // sin/cos circle starts from RIGHT
            double distance = status.getBEDistance();
            boolean outside = distance > mapRadius;
            if (outside)
            {
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(dashed);
                g2.drawLine(
                    (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * scale(5)),
                    (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * scale(5)),
                    (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * mapRadiusPX),
                    (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * mapRadiusPX));
                g2.setStroke(oldStroke);
            }
            else
            {
                double distancePX = distance * mapRadiusPX / mapRadius;
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(dashed);
                int starttx = (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * scale(5));
                int starty = (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * scale(5));
                int endx = (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * distancePX);
                int endy = (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * distancePX);
                g2.drawLine(starttx, starty, endx, endy);
                g2.setStroke(oldStroke);
                
                for (int i = 6; i > 0; i--)
                {
                    if (i % 2 == 0)
                        g.setColor(COLOR_FORE);
                    else
                        g.setColor(COLOR_BACK);
                    g.fillArc(
                        (int) (endx - scale(i * 2)),
                        (int) (endy - scale(i * 2)),
                        (int) (scale(i * 4)),
                        (int) (scale(i * 4)),
                        0,
                        360
                    );
                }
            }
        }
        
        g.setColor(COLOR_FORE);
        // AIRCRAFT POSITION (center) BY A TRIANGLE
        double triangleSize = 5;
        g.fillPolygon(new int[]
        {
            centerx,
            (int)(centerx+scale(triangleSize)),
            (int)(centerx-scale(triangleSize)),
        },
        new int[]
        {
            (int)(centery-scale(triangleSize)),
            (int)(centery+scale(triangleSize)),
            (int)(centery+scale(triangleSize)),
        },
        3);
        
        g.setFont(oldFont);
        Utils.writeAtOSB(g, bounds, 20, String.valueOf(CHAR_ARROW_TOP), false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 19, String.valueOf(CHAR_ARROW_BOTTOM), false, status.getOsbDown());
    }
    
    public void drawPage_WPT(Graphics g, Rectangle bounds)
    {
        String s = String.valueOf(status.getSimData().getCurWaypointNum());
        if (MFCDStatus.MetricSystem.IMPERIAL.equals(status.getMetricSystem()))
            s = String.valueOf(status.getSimData().getCurWaypointNum()-1);
        if (status.getSimData().getCurWaypointNum() == -1)
            s = "N/A";
        Utils.writeAtOSB(g, bounds, 20, "  CUR WP: " + s, false, status.getOsbDown());
        if (!status.getSimData().getLandingName().isEmpty())
        {
            Utils.writeAtOSB(g, bounds, 19, "  LAND: " + status.getSimData().getLandingName(), false, status.getOsbDown());
        }
        else if (status.getSimData().getCurWaypointNum() != -1)
        {
            Utils.writeAtOSB(g, bounds, 19, "  WP TO: " + status.getWPBRAStr(), false, status.getOsbDown());
            Utils.writeAtOSB(g, bounds, 18, "  LAT: " + status.getLLfromDeg(status.getSimData().getCurWaypointY(), true), false, status.getOsbDown());
            Utils.writeAtOSB(g, bounds, 17, "  LON: " + status.getLLfromDeg(status.getSimData().getCurWaypointX(), false), false, status.getOsbDown());
            Utils.writeAtOSB(g, bounds, 16, "  ALT: " + status.getSimData().getCurWaypointHStr(), false, status.getOsbDown());
        }
        Utils.writeAtOSB(g, bounds, 6, "SET BE HERE "+CHAR_ARROW_LEFT+" ", false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 7, "BE OFFSET "+CHAR_ARROW_LEFT+" ", false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 8, "WP OFFSET "+CHAR_ARROW_LEFT+" ", false, status.getOsbDown());
    }

    public void drawPage_POS(Graphics g, Rectangle bounds)
    {
        String msg = status.isPagePOSAltRadar() ?
            CHAR_ARROWS_VERTICAL+" R-ALT: " + status.getSimData().getRadarAlt() : 
            CHAR_ARROWS_VERTICAL+" ALT: " + status.getSimData().getBarometricAlt();
        Utils.writeAtOSB(g, bounds, 16, msg, false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 18, "  LAT: " + status.getSimData().getPosYLLStr(), false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 17, "  LON: " + status.getSimData().getPosXLLStr(), false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 20, "  BLAT: " + status.getBeYStr(), false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 19, "  BLON: " + status.getBeXStr(), false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 8, "Heading: " + status.getSimData().getHeadingStr() + "  ", false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 9, "Bank: " + status.getSimData().getBankStr() + "  ", false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 10, "Pitch: " + status.getSimData().getPitchStr() + "  ", false, status.getOsbDown());
        msg = status.getBeBRAStr();
        Utils.writeAtOSB(g, bounds, 6, "BE: "+msg+" "+CHAR_ARROW_LEFT, false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 7, "MK HERE "+CHAR_ARROW_LEFT, false, status.getOsbDown());
    }
    
    public void drawPage_ENG(Graphics g, Rectangle bounds)
    {
        Utils.writeAtOSB(g, bounds, 16, "  FUEL: "+status.getSimData().getFuelLeft(), false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 10, "FLOW: "+status.getSimData().getFuelConsumption()+ "  ", false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 17, "  L-TEMP: "+status.getSimData().getEngineTempLeftStr(), false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds,  9, "R-TEMP: "+status.getSimData().getEngineTempRightStr()+ "  ", false, status.getOsbDown());
        Utils.drawPercCake(g, bounds, 4, status.getSimData().getRpmLeft(), status.getSimData().getRpmLeftStr()+"%");
        Utils.drawPercCake(g, bounds, 1, status.getSimData().getRpmRight(), status.getSimData().getRpmRightStr()+"%");
    }

    public void drawPage_STG(Graphics g, Rectangle bounds)
    {
        switch (status.getMetricSystem())
        {
            case IMPERIAL:
                Utils.writeAtOSB(g, bounds, 10, "IMPERIAL "+CHAR_ARROWS_VERTICAL, false, status.getOsbDown());
                break;
            case METRIC:
                Utils.writeAtOSB(g, bounds, 10, "METRIC "+CHAR_ARROWS_VERTICAL, false, status.getOsbDown());
                break;
        }
        if (status.isBullseyeInverted())
            Utils.writeAtOSB(g, bounds, 9, "BE INVERTED "+CHAR_ARROWS_VERTICAL, false, status.getOsbDown());
        else
            Utils.writeAtOSB(g, bounds, 9, "BE STRAIGHT "+CHAR_ARROWS_VERTICAL, false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 8, "CHANGE BE "+CHAR_ARROW_LEFT, false, status.getOsbDown());
        Utils.writeAtOSB(g, bounds, 20, CHAR_ARROW_RIGHT+" RESET NAV", false, status.getOsbDown());
    }

    public void drawPageSelectionMenu(Graphics g, Rectangle bounds)
    {
        Utils.writeAtCenter(g, bounds, "PAGE SELECT");
        for (int i = 0; i < MFCDStatus.LOADPAGE_ITEMS.length; i++)
            if (i >= 0 && i < 10)
                Utils.writeAtOSB(g, bounds, i + 1, MFCDStatus.LOADPAGE_ITEMS[i] != null ? MFCDStatus.LOADPAGE_ITEMS[i].name() : "", status.getPageSelectionItem() == i + 1, status.getOsbDown());
            else if (i >= 10 && i < 16)
                Utils.writeAtOSB(g, bounds, i + 6, MFCDStatus.LOADPAGE_ITEMS[i] != null ? MFCDStatus.LOADPAGE_ITEMS[i].name() : "", status.getPageSelectionItem() == i + 6, status.getOsbDown());
    }

    private void drawCurrentPage(Graphics g, Rectangle bounds)
    {
        if (status.isPageSelectionMenu())
            drawPageSelectionMenu(g, bounds);
        else
        {
            int selectedPage = status.getSelectedPage();
            MFCDStatus.Page[] pageSet = status.getPageSet();
            if (selectedPage >= 0 && selectedPage < pageSet.length && pageSet[selectedPage] != null)
            {
                BiConsumer<Graphics, Rectangle> f = pageMaps.get(pageSet[selectedPage]);
                if (f != null)
                    f.accept(g, bounds);
            }
        }
    }
    
    private void drawPageNames(Graphics g, Rectangle bounds)
    {
        // Draw page names and selected page.
        // Draw them in the lower part buttons
        MFCDStatus.Page[] pageSet = status.getPageSet();
        int selectedPage = status.getSelectedPage();
        for (int i = 0; i < pageSet.length; i++)
            if (pageSet[i] != null)
            {
                String name = pageSet[i].name();
                int osbnum = (5 - i) + 10; // OSB15 is the start in the right
                boolean selected = i == selectedPage;
                Utils.writeAtOSB(g, bounds, osbnum, name, selected, status.getOsbDown());
            }
    }
    
    private void drawConnectionStatus(Graphics g, Rectangle bounds)
    {
        if (!status.isConnected())
            Utils.writeWarningMessage(g, bounds, "NO CONNECTION");
    }
    
    // -------------------------------------------------------------------------
    static double F = 1; // scale
    static double scale(double v){return v*F;}
    static Font fontOSB = new Font("Monospaced", Font.BOLD, FONT_SIZE_OSB);
    static Font fontSmall = new Font("Monospaced", Font.BOLD, FONT_SIZE_SMALL);
    static Font fontWP = new Font("Monospaced", Font.BOLD, WP_BOX_FONTSIZE);
    static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, (float)(DASH_SIZE * F), new float[]{(float)(DASH_SIZE * F)}, 0);
    static BasicStroke thick = new BasicStroke((float) (3 * F));
    // -------------------------------------------------------------------------
    public final void setF(double f)
    {
        F = f;
        fontOSB = new Font("Monospaced", Font.BOLD, (int)((double)FONT_SIZE_OSB * F));
        fontSmall = new Font("Monospaced", Font.BOLD, (int)((double)FONT_SIZE_SMALL * F));
        fontWP = new Font("Monospaced", Font.BOLD, (int)((double)WP_BOX_FONTSIZE * F));
        float dash = (float)((double)DASH_SIZE* F);
        dash = dash < 1 ? 1 : dash;
        dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, dash, new float[]{dash}, 0);
        thick = new BasicStroke((float) (4 * F));
        EventQueue.invokeLater(()->{revalidate(); repaint();});
    }
    public void recalculateF(int length)
    {
        setF((double)length / (double)520); //Original height
    }
    // -------------------------------------------------------------------------

    /**
     * Utility methods for drawing.
     */
    private static class Utils
    {
        /**
         * Writes at the center of the screen.
         *
         * @param g graphics
         * @param bounds rect bounds
         * @param s message to write (can be multilines)
         */
        private static void writeAtCenter(Graphics g, Rectangle bounds, String s)
        {
            String[] lines = s.split("\n");
            Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
            int x = (int) (bounds.x + bounds.width/2 - rect.getWidth()/2);
            int y = (int) (bounds.y + bounds.height/2 + (rect.getHeight() * lines.length)/2);

            g.setColor(COLOR_FORE);
            for (int i = 0; i < lines.length; i++)
                g.drawString(lines[i], x, (int)(y - (lines.length - i) * rect.getHeight()));
        }

        /**
         * Writes a warning message boxed and yellow in the screen center.
         * 
         * @param g graphics
         * @param bounds rect bounds
         * @param msg message to write (single line)
         */
        private static void writeWarningMessage(Graphics g, Rectangle bounds, String msg)
        {
            Rectangle2D rect = g.getFontMetrics().getStringBounds(msg, g);
            int x = (int) (bounds.x + bounds.width/2 - rect.getWidth()/2);
            int y = (int) (bounds.y + bounds.height/2 + bounds.height/4 + rect.getHeight());
            int w = (int) rect.getWidth();
            int h = (int) rect.getHeight();

            g.setColor(COLOR_WARNBOX);
            g.fillRect(
                (int) (x - scale(OSB_TEXT_BORDER)),
                (int) (y - scale(OSB_TEXT_BORDER) - rect.getHeight()/2),
                (int) (w + scale(OSB_TEXT_BORDER*2)),
                (int) (h - rect.getHeight()/3 + scale(OSB_TEXT_BORDER*2))
            );
            g.setColor(COLOR_BACK);
            g.drawString(msg, x, y);
        }

        /**
         * Writes a single line at specified OSB number.
         *
         * @param g graphics
         * @param bounds rect bounds
         * @param osbnum osb number where to draw
         * @param s string to draw
         * @param selected wether the item is currently selected
         */
        private static void writeAtOSB(Graphics g, Rectangle bounds, int osbnum, String s, boolean selected, int osbdown)
        {
            boolean top = false;
            boolean left = false;
            boolean right = false;
            boolean bottom = false;
            int posXorY = 0;
            // Also see if the button is currently pressed
            boolean pressed = osbdown == osbnum;
            switch (osbnum)
            {
                // Tops
                case 1:
                    top = true;
                    posXorY = OSB01_X;
                    break;
                case 2:
                    top = true;
                    posXorY = OSB02_X;
                    break;
                case 3:
                    top = true;
                    posXorY = OSB03_X;
                    break;
                case 4:
                    top = true;
                    posXorY = OSB04_X;
                    break;
                case 5:
                    top = true;
                    posXorY = OSB05_X;
                    break;
                // Rights
                case 6:
                    right = true;
                    posXorY = OSB06_Y;
                    break;
                case 7:
                    right = true;
                    posXorY = OSB07_Y;
                    break;
                case 8:
                    right = true;
                    posXorY = OSB08_Y;
                    break;
                case 9:
                    right = true;
                    posXorY = OSB09_Y;
                    break;
                case 10:
                    right = true;
                    posXorY = OSB10_Y;
                    break;
                // Bottoms
                case 11:
                    bottom = true;
                    posXorY = OSB11_X;
                    break;
                case 12:
                    bottom = true;
                    posXorY = OSB12_X;
                    break;
                case 13:
                    bottom = true;
                    posXorY = OSB13_X;
                    break;
                case 14:
                    bottom = true;
                    posXorY = OSB14_X;
                    break;
                case 15:
                    bottom = true;
                    posXorY = OSB15_X;
                    break;
                // Lefts
                case 16:
                    left = true;
                    posXorY = OSB16_Y;
                    break;
                case 17:
                    left = true;
                    posXorY = OSB17_Y;
                    break;
                case 18:
                    left = true;
                    posXorY = OSB18_Y;
                    break;
                case 19:
                    left = true;
                    posXorY = OSB19_Y;
                    break;
                case 20:
                    left = true;
                    posXorY = OSB20_Y;
                    break;
            }
            Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
            if (top)
            {
                if (selected || pressed)
                {
                    g.setColor(COLOR_FORE);
                    g.fillRect(
                        (int) (bounds.x + scale(posXorY) - rect.getWidth()/2 - scale(OSB_TEXT_BORDER)),
                        (int) (bounds.y + scale(OSB_TEXT_MARGIN) - scale(OSB_TEXT_BORDER)),
                        (int) (rect.getWidth() + scale(OSB_TEXT_BORDER*2)),
                        (int) (rect.getHeight()/2 + scale(OSB_TEXT_BORDER*2))
                    );
                    g.setColor(COLOR_BACK);
                }
                else
                    g.setColor(COLOR_FORE);
                g.drawString(s,
                    (int) (bounds.x + scale(posXorY) - rect.getWidth()/2),
                    (int) (bounds.y + rect.getHeight()/2 + scale(OSB_TEXT_MARGIN))
                );
            }
            else if (bottom)
            {
                if (selected || pressed)
                {
                    g.setColor(COLOR_FORE);
                    g.fillRect(
                        (int) (bounds.x + scale(posXorY) - rect.getWidth()/2 - scale(OSB_TEXT_BORDER)),
                        (int) (bounds.y + bounds.height - rect.getHeight()/2 - scale(OSB_TEXT_MARGIN) - scale(OSB_TEXT_BORDER)),
                        (int) (rect.getWidth() + scale(OSB_TEXT_BORDER*2)),
                        (int) (rect.getHeight()/2 + scale(OSB_TEXT_BORDER*2))
                    );
                    g.setColor(COLOR_BACK);
                }
                else
                    g.setColor(COLOR_FORE);
                g.drawString(s,
                    (int) (bounds.x + scale(posXorY) - rect.getWidth()/2),
                    (int) (bounds.y + bounds.height - scale(OSB_TEXT_MARGIN))
                );
            }
            else if (left)
            {
                if (selected || pressed)
                {
                    g.setColor(COLOR_FORE);
                    g.fillRect(
                        (int) (bounds.x + scale(OSB_TEXT_MARGIN) - scale(OSB_TEXT_BORDER)),
                        (int) (bounds.y + scale(posXorY) - rect.getHeight()/2 - scale(OSB_TEXT_BORDER)),
                        (int) (rect.getWidth() + scale(OSB_TEXT_BORDER*2)),
                        (int) (rect.getHeight()/2 + scale(OSB_TEXT_BORDER*2))
                    );
                    g.setColor(COLOR_BACK);
                }
                else
                    g.setColor(COLOR_FORE);
                g.drawString(s,
                    (int) (bounds.x + scale(OSB_TEXT_MARGIN)),
                    (int) (bounds.y + scale(posXorY))
                );
            }
            else if (right)
            {
                if (selected || pressed)
                {
                    g.setColor(COLOR_FORE);
                    g.fillRect(
                        (int) (bounds.x + bounds.width - scale(OSB_TEXT_MARGIN) - rect.getWidth() - scale(OSB_TEXT_BORDER)),
                        (int) (bounds.y + scale(posXorY) - rect.getHeight()/2 - scale(OSB_TEXT_BORDER)),
                        (int) (rect.getWidth() + scale(OSB_TEXT_BORDER*2)),
                        (int) (rect.getHeight()/2 + scale(OSB_TEXT_BORDER*2))
                    );
                    g.setColor(COLOR_BACK);
                }
                else
                    g.setColor(COLOR_FORE);
                g.drawString(s,
                    (int) (bounds.x + bounds.width - scale(OSB_TEXT_MARGIN) - rect.getWidth()),
                    (int) (bounds.y + scale(posXorY))
                );
            }
        }
    
        /**
         * Draws a percentage cake with a value inside.
         * This will occupy a quadrant regardless of what's written already.
         *
         * @param g graphics
         * @param bounds rect bounds
         * @param quadrant quadrant number (as per cartesian plane)
         * @param value percentage value
         * @param label label to write into the circle
         */
        private static void drawPercCake(Graphics g, Rectangle bounds, int quadrant, double value, String label)
        {
            int x = 0, y = 0;
            switch (quadrant)
            {
                case 1:
                    x = bounds.width/2 + bounds.width/4;
                    y = bounds.height/2 - bounds.height/4;
                    break;
                case 2:
                    x = bounds.width/2 + bounds.width/4;
                    y = bounds.height/2 + bounds.height/4;
                    break;
                case 3:
                    x = bounds.width/2 - bounds.width/4;
                    y = bounds.height/2 + bounds.height/4;
                    break;
                case 4:
                    x = bounds.width/2 - bounds.width/4;
                    y = bounds.height/2 - bounds.height/4;
                    break;
            }
            if (value >= 100f)
                g.setColor(Color.RED);
            else
                g.setColor(Color.GREEN);
            g.fillArc(
                (int) (x - bounds.width/8),
                (int) (y - bounds.height/8),
                (int) (bounds.height/4),
                (int) (bounds.height/4),
                90,
                - (int) (value * 360 / 100)
            );
            Rectangle2D r = g.getFontMetrics().getStringBounds(label, g);
            g.setColor(COLOR_BACK);
            g.fillArc(
                (int) (x - bounds.width/20),
                (int) (y - bounds.height/20),
                (int) (bounds.height/10),
                (int) (bounds.height/10),
                0,
                360
            );
            g.setColor(COLOR_FORE);
            g.drawString(
                label,
                (int) (x - r.getWidth()/2),
                (int) (y + r.getHeight()/2 - r.getHeight()/8)
            );
        }
    }
}
