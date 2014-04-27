import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
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
    private static final float DASH_SIZE = 5f;
    
    private static final int OSB01_X = 80;
    private static final int OSB02_X = 160;
    private static final int OSB03_X = 240;
    private static final int OSB04_X = 320;
    private static final int OSB05_X = 400;
    
    private static final int OSB06_Y = 80;
    private static final int OSB07_Y = 160;
    private static final int OSB08_Y = 240;
    private static final int OSB09_Y = 320;
    private static final int OSB10_Y = 400;
    
    private static final int OSB20_Y = 80;
    private static final int OSB19_Y = 160;
    private static final int OSB18_Y = 240;
    private static final int OSB17_Y = 320;
    private static final int OSB16_Y = 400;
    
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
    
    
    double F = 1; // scale
    Font fontOSB = new Font("Monospaced", Font.BOLD, FONT_SIZE_OSB);
    Font fontSmall = new Font("Monospaced", Font.BOLD, FONT_SIZE_SMALL);
    BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, (float)(DASH_SIZE * F), new float[]{(float)(DASH_SIZE * F)}, 0);
    
    double scale(double v){return v*F;}
    int scale(int v){return (int)((double)v*F);}
    public final void setF(double f)
    {
        fontOSB = new Font("Monospaced", Font.BOLD, (int)(FONT_SIZE_OSB * F));
        fontSmall = new Font("Monospaced", Font.BOLD, (int)(FONT_SIZE_SMALL * F));
        dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, (float)(DASH_SIZE* F), new float[]{(float)(DASH_SIZE * F)}, 0);
        EventQueue.invokeLater(()->{revalidate(); repaint();});
    
    }

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
        pageMaps.put(MFCDStatus.Page.STG, this::drawPage_PREF);
        pageMaps.put(MFCDStatus.Page.ENG, this::drawPage_ENG);
        pageMaps.put(MFCDStatus.Page.WPT, this::drawPage_WPT);
    }
    
    @Override
    public void update(Observable o, Object arg)
    {
        EventQueue.invokeLater(()-> {repaint();});
    }

    public void drawPage_TEST(Graphics g, Rectangle bounds)
    {
        writeAtCenter(g, bounds, SYMBOLTEST);
    }
    
    public void drawPage_WPT(Graphics g, Rectangle bounds)
    {
        
    }

    public void drawPage_NAV(Graphics g, Rectangle bounds)
    {
        String msg;
        Rectangle2D b;
        
        Font oldFont = g.getFont();
        g.setFont(fontSmall);
        
        msg = status.getBeBRAStr();
        if (status.isBullseyeInverted())
            msg = CHAR_ARROW_LEFT+" "+msg;
        else
            msg = CHAR_ARROW_RIGHT+" "+msg;
        g.setColor(Color.GREEN);
        g.drawString(msg, scale(20), scale(20+FONT_SIZE_SMALL));
        g.setFont(oldFont);
        
        int centerx = (int) (bounds.getX() + bounds.getWidth()/2);
        int centery = (int) (bounds.getY() + bounds.getHeight()/2);
            
        int largeCircleX = (int) bounds.getX() + scale(50);
        int largeCircleY = (int) bounds.getY() + scale(50);
        int largeCircleW = (int) bounds.getWidth() - scale(100);
        int largeCircleH = (int) bounds.getHeight() - scale(100);
        g.drawArc(largeCircleX, largeCircleY, largeCircleW, largeCircleH, 0, 360);
        
        int smallCircleX = largeCircleX + largeCircleW/4;
        int smallCircleY = largeCircleY + largeCircleH/4;
        int smallCircleW = largeCircleW/2;
        int smallCircleH = largeCircleW/2;
        g.drawArc(smallCircleX, smallCircleY, smallCircleW, smallCircleH, 0, 360);
        
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
        
        // Draw the north indicator
        {
            double degree = status.getSimData().getHeading() -360;
            degree += 90; // sin/cos circle starts from RIGHT
            int starttx = (int)(centerx + Math.cos(Math.toRadians(degree)) * smallCircleW/2);
            int starty = (int)(centery - Math.sin(Math.toRadians(degree)) * smallCircleW/2);
            int endx = (int)(centerx + Math.cos(Math.toRadians(degree)) * (smallCircleW/2 + scale(16)));
            int endy = (int)(centery - Math.sin(Math.toRadians(degree)) * (smallCircleW/2 + scale(16)));
            g.drawLine(starttx, starty, endx, endy);
            // Other points internals
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
        
        msg = status.getPageNAVRadiusStr();
        b = g.getFontMetrics().getStringBounds(msg, g);
        g.drawString(msg, (int)(bounds.getX() + bounds.getWidth() - b.getWidth() - scale(20)), scale(20+FONT_SIZE_SMALL));
        
        int mapRadiusPX = largeCircleW;
        int mapRadius = status.getPageNAVRadius();
        
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
                    (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * scale(largeCircleW/2)),
                    (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * scale(largeCircleH/2)));
                g2.setStroke(oldStroke);
            }
            else
            {
                double distancePX = distance * largeCircleW/2 / mapRadius;
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(dashed);
                int starttx = (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * scale(5));
                int starty = (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * scale(5));
                int endx = (int)(centerx + Math.cos(Math.toRadians(deltaBearing)) * scale(distancePX));
                int endy = (int)(centery - Math.sin(Math.toRadians(deltaBearing)) * scale(distancePX));
                g2.drawLine(starttx, starty, endx, endy);
                g2.setStroke(oldStroke);
                
                g.setColor(Color.GREEN);
                g.fillArc(endx - scale(10), endy - scale(10), scale(20), scale(20), 0, 360);
                g.setColor(Color.BLACK);
                g.fillArc(endx - scale(8), endy - scale(8), scale(16), scale(16), 0, 360);
                g.setColor(Color.GREEN);
                g.fillArc(endx - scale(6), endy - scale(6), scale(12), scale(12), 0, 360);
                g.setColor(Color.BLACK);
                g.fillArc(endx - scale(4), endy - scale(4), scale(8), scale(8), 0, 360);
                g.setColor(Color.GREEN);
                g.fillArc(endx - scale(2), endy - scale(2), scale(4), scale(4), 0, 360);
            }
        }
        
        g.setFont(oldFont);
        writeAtOSB(g, bounds, 20, String.valueOf(CHAR_ARROW_TOP), false);
        writeAtOSB(g, bounds, 19, String.valueOf(CHAR_ARROW_BOTTOM), false);
    }

    public void drawPage_PREF(Graphics g, Rectangle bounds)
    {
        switch (status.getMetricSystem())
        {
            case IMPERIAL:
                writeAtOSB(g, bounds, 10, "IMPERIAL "+CHAR_ARROWS_VERTICAL, false);
                break;
            case METRIC:
                writeAtOSB(g, bounds, 10, "METRIC "+CHAR_ARROWS_VERTICAL, false);
                break;
        }
        if (status.isBullseyeInverted())
            writeAtOSB(g, bounds, 9, "BE INVERTED "+CHAR_ARROWS_VERTICAL, false);
        else
            writeAtOSB(g, bounds, 9, "BE STRAIGHT "+CHAR_ARROWS_VERTICAL, false);
        writeAtOSB(g, bounds, 8, "CHANGE BE "+CHAR_ARROW_LEFT, false);
    }
    
    public void drawPage_ENG(Graphics g, Rectangle bounds)
    {
        writeAtOSB(g, bounds, 16, "  FUEL: "+status.getSimData().getFuelLeft(), false);
        writeAtOSB(g, bounds, 10, "FLOW: "+status.getSimData().getFuelConsumption()+ "  ", false);
        writeAtOSB(g, bounds, 17, "  L-TEMP: "+status.getSimData().getEngineTempLeftStr(), false);
        writeAtOSB(g, bounds,  9, "R-TEMP: "+status.getSimData().getEngineTempRightStr()+ "  ", false);
        drawPercCake(g, bounds, 4, status.getSimData().getRpmLeft(), status.getSimData().getRpmLeftStr()+"%");
        drawPercCake(g, bounds, 1, status.getSimData().getRpmRight(), status.getSimData().getRpmRightStr()+"%");
    }

    public void drawPage_POS(Graphics g, Rectangle bounds)
    {
        String msg = status.isPagePOSAltRadar() ?
            CHAR_ARROWS_VERTICAL+" R-ALT: " + status.getSimData().getRadarAlt() : 
            CHAR_ARROWS_VERTICAL+" ALT: " + status.getSimData().getBarometricAlt();
        writeAtOSB(g, bounds, 16, msg, false);
        writeAtOSB(g, bounds, 18, "  LAT: " + status.getSimData().getPosYLLStr(), false);
        writeAtOSB(g, bounds, 17, "  LON: " + status.getSimData().getPosXLLStr(), false);
        writeAtOSB(g, bounds, 20, "  BLAT: " + status.getBeYStr(), false);
        writeAtOSB(g, bounds, 19, "  BLON: " + status.getBeXStr(), false);
        writeAtOSB(g, bounds, 8, "Heading: " + status.getSimData().getHeadingStr() + "  ", false);
        writeAtOSB(g, bounds, 9, "Bank: " + status.getSimData().getBankStr() + "  ", false);
        writeAtOSB(g, bounds, 10, "Pitch: " + status.getSimData().getPitchStr() + "  ", false);
        msg = status.getBeBRAStr();
        writeAtOSB(g, bounds, 6, "BE: "+msg+" "+CHAR_ARROW_LEFT, false);
    }

    public void drawPageSelectionMenu(Graphics g, Rectangle bounds)
    {
        writeAtCenter(g, bounds, "PAGE SELECT");
        
        for (int i = 0; i < MFCDStatus.LOADPAGE_ITEMS.length; i++)
            if (i >= 0 && i < 10)
                writeAtOSB(g, bounds, i + 1, MFCDStatus.LOADPAGE_ITEMS[i] != null ? MFCDStatus.LOADPAGE_ITEMS[i].name() : "", status.getPageSelectionItem() == i + 1);
            else if (i >= 10 && i < 16)
                writeAtOSB(g, bounds, i + 6, MFCDStatus.LOADPAGE_ITEMS[i] != null ? MFCDStatus.LOADPAGE_ITEMS[i].name() : "", status.getPageSelectionItem() == i + 6);
    }
    
    /**
    Tjhis will occupy a quadrant regardless of what's written
    @param g
    @param bounds
    @param quadrant
    @param value
    @param label 
    */
    public void drawPercCake(Graphics g, Rectangle bounds, int quadrant, double value, String label)
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
        g.setColor(Color.GREEN);
        g.fillArc(scale(x - 80), scale(y - 80), scale(160), scale(160), 90, - (int) (value * 360 / 100));
        Rectangle2D r = g.getFontMetrics().getStringBounds(label, g);
        g.setColor(Color.BLACK);
        g.fillArc(scale(x - 30), scale(y - 30), scale(60), scale(60), 0, 360);
        g.setColor(Color.GREEN);
        g.drawString(label, (int)scale(x - r.getWidth()/2), (int)scale(y + r.getHeight()/2 - 6));
    }

    /**
        Can write multilines
        @param g
        @param bounds
        @param s 
    */
    public void writeAtCenter(Graphics g, Rectangle bounds, String s)
    {
        String[] lines = s.split("\n");
        g.setColor(Color.GREEN);
        Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
        int x = (int) (bounds.x + bounds.width/2 - rect.getWidth()/2);
        int y = bounds.y + bounds.height/2 + FONT_SIZE_OSB;
        for (int i = 0; i < lines.length; i++)
            g.drawString(s, scale(x), scale(y - i + i/2));
    }
    
    /**
        Writes a single line
        @param g
        @param bounds
        @param osbnum
        @param s
        @param selected 
    */
    public void writeAtOSB(Graphics g, Rectangle bounds, int osbnum, String s, boolean selected)
    {
        boolean top = false;
        boolean left = false;
        boolean right = false;
        int posXorY = 0;
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
            // 11 to 15 = base pages - no render
        }
        boolean pressed = status.getOsbDown() == osbnum;
        Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
        if (top)
        {
            if (selected || pressed)
            {
                g.setColor(Color.GREEN);
                g.fillRect(scale((int) (bounds.x + posXorY - rect.getWidth()/2) - 5),
                    scale(bounds.y + 5),
                    scale((int) rect.getWidth() + 10),
                    scale((int) rect.getHeight() + 10));
                g.setColor(Color.BLACK);
            }
            else
                g.setColor(Color.GREEN);
            g.drawString(s, scale((int) (bounds.x + posXorY - rect.getWidth()/2)), scale(bounds.y + 10 + FONT_SIZE_OSB));
        }
        else if (left)
        {
            if (selected || pressed)
            {
                g.setColor(Color.GREEN);
                g.fillRect(scale(bounds.x + 5),
                    scale((int) (bounds.y + posXorY - + rect.getHeight()/2 - 5)),
                    scale((int) rect.getWidth() + 10),
                    scale((int) rect.getHeight() + 10));
                g.setColor(Color.BLACK);
            }
            else
                g.setColor(Color.GREEN);
            g.drawString(s, scale(bounds.x + 10), scale((int) (bounds.y + posXorY + rect.getHeight()/2 - 5)));
        }
        else if (right)
        {
            if (selected || pressed)
            {
                g.setColor(Color.GREEN);
                g.fillRect(scale((int) (bounds.x + bounds.width - 15 - rect.getWidth())),
                    scale((int) (bounds.y + bounds.getY() + posXorY - rect.getHeight()/2 - 5)),
                    scale((int) rect.getWidth() + 10),
                    scale((int) rect.getHeight() + 10));
                g.setColor(Color.BLACK);
            }
            else
                g.setColor(Color.GREEN);
            g.drawString(s, scale((int) (bounds.x + bounds.width - 10 - rect.getWidth())), scale((int) (bounds.y + bounds.getY() + posXorY + rect.getHeight()/2 - 5)));
        }
    }
    
    @Override
    public void paintComponent(Graphics _g)
    {
        Rectangle bounds = new Rectangle();
        bounds.width = 500;
        bounds.height = 500;

        Graphics g = _g;
        try
        {
            // Clear out the screen
            g.setFont(fontOSB);
            g.setColor(Color.BLACK);
            g.fillRect(scale(bounds.x), scale(bounds.y), scale(bounds.width), scale(bounds.height));

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
        int hmargin = 6;
        int selectedPage = status.getSelectedPage();
        int w = 60;
        int h = (int) (FONT_SIZE_OSB + hmargin);
        for (int i = 0; i < pageSet.length; i++)
        {
            boolean selected = i == selectedPage;
            boolean pressed = 5 - (status.getOsbDown() - 10) == i;
            int x = bounds.x + 50 + i*80;
            int y = bounds.height - 10;
            if (pageSet[i] != null)
            {
                String name = pageSet[i].name();
                int strw = g.getFontMetrics().stringWidth(name);
                if (selected || pressed)
                {
                    g.setColor(Color.GREEN);
                    g.fillRect(scale(x), scale(y-h), scale(w), scale(h));
                    g.setColor(Color.BLACK);
                }
                else
                    g.setColor(Color.GREEN);
                g.drawString(name, scale(x + (w-strw)/2), scale(y - hmargin));
            }
        }
    }
    
    private void drawConnectionStatus(Graphics g, Rectangle bounds)
    {
        if (!status.isConnected())
            drawWarningMessage(g, bounds, "NO CONNECTION");
    }
    
    private void drawWarningMessage(Graphics g, Rectangle bounds, String msg)
    {
        int marginbottom = 50;
        int strw = g.getFontMetrics().stringWidth(msg);
        int w = strw + 20;
        int h = FONT_SIZE_OSB + 20;
        g.setColor(Color.YELLOW);
        g.fillRect(scale(bounds.x + (bounds.width - w)/2), scale(bounds.y + bounds.height - h - marginbottom - FONT_SIZE_OSB - 8), scale(w), scale(h));
        g.setColor(Color.BLACK);
        g.drawString(msg, scale(bounds.x + (bounds.width - w)/2 + (w-strw)/2), scale(bounds.y + bounds.height - h - marginbottom));
    }
}
