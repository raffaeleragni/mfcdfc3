import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import javax.swing.JOptionPane;

/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class MFCDStatus extends Observable
{
    private static final Path MARKPOINTS_PATH = Paths.get("markpoints.txt");
    
    // Only lower buttons can have pages, num = 5
    private static final int PAGE_SET_NUM = 5;
    public static final MFCDStatus.Page[] LOADPAGE_ITEMS = new MFCDStatus.Page[]
    {
        null, null, null, null, null,
        Page.POS, Page.NAV, Page.WPT, Page.ADI, null,
        null, null, Page.ENG, Page.STG, Page.TST,
    };
    
    public static enum Page {TST, POS, NAV, STG, ENG, WPT, ADI}
    public static enum MetricSystem {METRIC, IMPERIAL}
    
    public class SimData
    {
        private static final String RADAR_ALT_NOVALUE = "*****";
        private String radarAlt = RADAR_ALT_NOVALUE;
        public String getRadarAlt()
        {
            return radarAlt;
        }
        /** Always set in meters, but get converted
         * @param radarAlt */
        public void setRadarAlt(double radarAlt)
        {
            if (radarAlt > 1524) // 5000ft
            {
                if (!RADAR_ALT_NOVALUE.equals(this.radarAlt))
                {
                    this.radarAlt = RADAR_ALT_NOVALUE;
                    MFCDStatus.this.triggerUpdate();
                }
            }
            else
            {
                BigDecimal num;
                if (metricSystem == MetricSystem.IMPERIAL)
                    num = new BigDecimal(radarAlt).multiply(new BigDecimal(3.281)).setScale(0, BigDecimal.ROUND_HALF_UP);
                else
                    num = new BigDecimal(radarAlt).setScale(0, BigDecimal.ROUND_HALF_UP);
                String newnum = "     "+num.toString();
                newnum = newnum.substring(newnum.length()-5, newnum.length());
                switch (metricSystem)
                {
                    case IMPERIAL:
                        newnum += "ft";
                        break;
                    case METRIC:
                        newnum += "m";
                        break;
                }
                if (this.radarAlt == null || !newnum.equals(this.radarAlt))
                {
                    this.radarAlt = newnum;
                    MFCDStatus.this.triggerUpdate();
                }
            }
        }
        
        
        private static final String BAROMETRIC_ALT_NOVALUE = "*****";
        private String barometricAlt = BAROMETRIC_ALT_NOVALUE;
        public String getBarometricAlt()
        {
            return barometricAlt;
        }
        /** Always set in meters, but get converted
         * @param barometricAlt */
        public void setBarometricAlt(double barometricAlt)
        {
            BigDecimal num;
            if (metricSystem == MetricSystem.IMPERIAL)
                num = new BigDecimal(ftFromMeters(barometricAlt)).setScale(0, BigDecimal.ROUND_HALF_UP);
            else
                num = new BigDecimal(barometricAlt).setScale(0, BigDecimal.ROUND_HALF_UP);
            String newnum = "     "+num.toString();
            newnum = newnum.substring(newnum.length()-5, newnum.length());
            switch (metricSystem)
            {
                case IMPERIAL:
                    newnum += "ft";
                    break;
                case METRIC:
                    newnum += "m";
                    break;
            }
            if (this.barometricAlt == null || !newnum.equals(this.barometricAlt))
            {
                this.barometricAlt = newnum;
                MFCDStatus.this.triggerUpdate();
            }
        }
        
        private double posX = 41.6; // Long
        private double posY = 42.1; // Lat
        public double getPosX()
        {
            return posX;
        }
        public double getPosY()
        {
            return posY;
        }
        public String getPosXLLStr()
        {
            return getLLfromDeg(posX, false);
        }
        public String getPosYLLStr()
        {
            return getLLfromDeg(posY, true);
        }
        
        public void setPosition(double x, double y)
        {
            posX = x;
            posY = y;
            MFCDStatus.this.triggerUpdate();
        }
        
        private int heading = 0;
        private int bank = 0;
        private int pitch = 0;
        public int getHeading()
        {
            return heading;
        }
        public int getBank()
        {
            return bank;
        }
        public int getPitch()
        {
            return pitch;
        }
        public String getHeadingStr()
        {
            String s = "    "+heading;
            s = s.substring(s.length()-4, s.length()) +  "°";
            return s;
        }
        public String getBankStr()
        {
            String s = "    "+bank;
            s = s.substring(s.length()-4, s.length()) +  "°";
            return s;
        }
        public String getPitchStr()
        {
            String s = "    "+pitch;
            s = s.substring(s.length()-4, s.length()) +  "°";
            return s;
        }
        public void setHBP(double h, double b, double p)
        {
            this.heading = (int) h;
            this.bank = (int) b;
            this.pitch = (int) p;
            MFCDStatus.this.triggerUpdate();
        }
        
        private String fuelLeft = "";
        private String fuelConsumption = "";
        public String getFuelLeft()
        {
            return fuelLeft;
        }
        public String getFuelConsumption()
        {
            return fuelConsumption;
        }
        public void setFuel(double left, double consumpion)
        {
            int iFuelLeft = 0;
            int iFuelConsumption = 0;
            String s;
            switch (metricSystem)
            {
                case IMPERIAL:
                    iFuelLeft = (int) (left * 2.225); // to pounds (lb)
                    iFuelConsumption = (int) (consumpion * 2.225d * 3600d); // to PPH
                    s = "     "+String.valueOf(iFuelLeft);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelLeft = s + " lb";
                    s = "     "+String.valueOf(iFuelConsumption);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelConsumption = s + " pph";
                    break;
                case METRIC:
                    iFuelLeft = (int) left;
                    iFuelConsumption = (int) (consumpion * 3600d);
                    s = "     "+String.valueOf(iFuelLeft);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelLeft = s + " kg";
                    s = "     "+String.valueOf(iFuelConsumption);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelConsumption = s + " kph";
                    break;
            }
            MFCDStatus.this.triggerUpdate();
        }
        
        private double rpmLeft = 0;
        private double rpmRight = 0;
        public double getRpmLeft()
        {
            return rpmLeft;
        }
        public double getRpmRight()
        {
            return rpmRight;
        }
        public String getRpmLeftStr()
        {
            return new BigDecimal(rpmLeft).setScale(0, RoundingMode.HALF_UP).toString();
        }
        public String getRpmRightStr()
        {
            return new BigDecimal(rpmRight).setScale(0, RoundingMode.HALF_UP).toString();
        }
        public void setRPM(double left, double right)
        {
            this.rpmLeft = left;
            this.rpmRight = right;
            MFCDStatus.this.triggerUpdate();
        }
        
        private double engineTempLeft = 0;
        private double engineTempRight = 0;
        public double getEngineTempLeft()
        {
            return engineTempLeft;
        }
        public double getEngineTempRight()
        {
            return engineTempRight;
        }
        public String getEngineTempLeftStr()
        {
            return new BigDecimal(engineTempLeft).setScale(0, RoundingMode.HALF_UP).toString()+"°C";
        }
        public String getEngineTempRightStr()
        {
            return new BigDecimal(engineTempRight).setScale(0, RoundingMode.HALF_UP).toString()+"°C";
        }
        public void setEngineTemp(double left, double right)
        {
            this.engineTempLeft = left;
            this.engineTempRight = right;
            MFCDStatus.this.triggerUpdate();
        }
        
        String landingName = "";
        double landingX = 0;
        double landingY = 0;
        int curWaypointNum = -1;
        double curWaypointX = 0;
        double curWaypointY = 0;
        double curWaypointH = 0;
        Map<Integer, double[]> waypoints = new TreeMap<>();
        public void addWaypoint(int num, double x, double y, double h)
        {
            curWaypointNum = num;
            curWaypointX = x;
            curWaypointY = y;
            curWaypointH = h;
            waypoints.put(num, new double[]{x, y, h});
            landingName = "";
            landingX = 0;
            landingY = 0;
            MFCDStatus.this.triggerUpdate();
        }
        public String getLandingName()
        {
            return landingName;
        }
        public double getLandingX()
        {
            return landingX;
        }
        public double getLandingY()
        {
            return landingY;
        }
        public void landingAt(String s, double x, double y)
        {
            landingName = s;
            landingX = x;
            landingY = y;
            curWaypointNum = -1;
            MFCDStatus.this.triggerUpdate();
        }
        public int getCurWaypointNum()
        {
            return curWaypointNum;
        }
        public double getCurWaypointX()
        {
            return curWaypointX;
        }
        public double getCurWaypointY()
        {
            return curWaypointY;
        }
        public double getCurWaypointH()
        {
            return curWaypointH;
        }
        public String getCurWaypointHStr()
        {
            if (metricSystem == MetricSystem.IMPERIAL)
                return new BigDecimal(ftFromMeters(curWaypointH)).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "ft";
            return new BigDecimal(curWaypointH).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "m";
        }
        public Map<Integer, double[]> getWaypoints()
        {
            return waypoints;
        }
    }
    
    private boolean connected = false;

    private final SimData simData = new SimData();
    
    private MetricSystem metricSystem = MetricSystem.IMPERIAL;
    private final Page[] pageSet;
    private int selectedPage;
    private int osbDown;
    private boolean pageSelectionMenu;
    private int pageSelectionItem = 0; // OSB number
    private boolean bullseyeInverted = false;
    List<double[]> markpoints = new ArrayList<>();
    // POS PAGE
    private boolean pagePOSAltRadar = true;
    // NAV PAGE
    private int pageNAVRadius = 0;
    
    // BE in LL degrees
    // Defaults to BLUE-POTI
    private double beX = 41.678888; //Long
    private double beY = 42.186388; // Lat
    
    public MFCDStatus()
    {
        pageSet = new Page[PAGE_SET_NUM];
        pageSet[0] = Page.NAV;
        pageSet[1] = Page.WPT;
        pageSet[2] = Page.POS;
        pageSet[3] = Page.ENG;
        pageSet[4] = Page.STG;
        
        selectedPage = 0;
        
        pageNAVRadiusDecrease();
        loadMarkpoints();
    }
    
    public void updatePageSet(List<Page> pages)
    {
        for (int i = 0; i < 5; i++)
            pageSet[i] = pages.size() > i ? pages.get(i) : null;
        triggerUpdate();
    }
    
    public void reset()
    {
        getSimData().curWaypointNum = -1;
        getSimData().landingName = "";
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void setConnected(boolean connected)
    {
        this.connected = connected;
        triggerUpdate();
    }

    public int getSelectedPage()
    {
        return selectedPage;
    }

    public void setSelectedPage(int selectedPage)
    {
        this.selectedPage = selectedPage;
        triggerUpdate();
    }
    
    public Page[] getPageSet()
    {
        // MUST BE READ ONLY
        // so that gets notification when using the changePageAt
        return pageSet.clone();
    }

    public void changePageAt(Page pageToChange, int position)
    {
        if (position >= 0 && position < pageSet.length)
        {
            pageSet[position] = pageToChange;
            triggerUpdate();
        }
    }

    public SimData getSimData()
    {
        return simData;
    }

    public List<double[]> getMarkpoints()
    {
        return markpoints;
    }
    
    public void addMark(double x, double y)
    {
        markpoints.add(new double[]{x, y});
        triggerUpdate();
    }

    public void loadMarkpoints()
    {
        try {
            List<String> points = Files.readAllLines(MARKPOINTS_PATH);
            markpoints = points.stream()
                .map(s -> {
                    String[] ss = s.split(",");
                    System.out.println("reading: "+s);
                    return new double[] {Double.valueOf(ss[0]), Double.valueOf(ss[1])};
                })
                .collect(toList());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.err.println(ex.getMessage());
        }
    }

    public void saveMarkpoints()
    {
        List<String> points = markpoints.stream()
            .map(point -> String.valueOf(point[0]) + "," + String.valueOf(point[1]))
            .collect(toList());
        try {
            Files.write(MARKPOINTS_PATH, points);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.err.println(ex.getMessage());
        }
    }
    
    public void addMarkFromOffset(double x, double y, double deg, double dis)
    {
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            dis *= 1.852d;
        deg = 360 - deg + 90;
//        deg += 1; // WHY???
        // CCW E to CW N
//        deg = 360 - deg;
//        dis /= 5d; // WHY???
        double dx = dis*Math.cos(Math.toRadians(deg));
        double dy = dis*Math.sin(Math.toRadians(deg));
        double delta_latitude = dy/111d;
        double delta_longitude = dx/(111d*Math.cos(Math.toRadians(y)));
        double newx = x + delta_longitude;
        double newy = y + delta_latitude;
        addMark(newx, newy);
        // triggerUpdate() already called in addMark()
    }

    public MetricSystem getMetricSystem()
    {
        return metricSystem;
    }

    public void setMetricSystem(MetricSystem metricSystem)
    {
        this.metricSystem = metricSystem;
        this.pageNAVRadius = 0;
        pageNAVRadiusDecrease();
        triggerUpdate();
    }

    public int getOsbDown()
    {
        return osbDown;
    }

    public void setOsbDown(int osbDown)
    {
        this.osbDown = osbDown;
        triggerUpdate();
    }

    public boolean isPageSelectionMenu()
    {
        return pageSelectionMenu;
    }

    public void setPageSelectionMenu(boolean pageSelectionMenu)
    {
        this.pageSelectionMenu = pageSelectionMenu;
        triggerUpdate();
    }

    public int getPageSelectionItem()
    {
        return pageSelectionItem;
    }

    public void setPageSelectionItem(int pageSelectionItem)
    {
        this.pageSelectionItem = pageSelectionItem;
        triggerUpdate();
    }

    public boolean isPagePOSAltRadar()
    {
        return pagePOSAltRadar;
    }

    public void setPagePOSAltRadar(boolean pagePOSAltRadar)
    {
        this.pagePOSAltRadar = pagePOSAltRadar;
        triggerUpdate();
    }

    public int getPageNAVRadius()
    {
        return pageNAVRadius;
    }

    public String getPageNAVRadiusStr()
    {
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            return pageNAVRadius+"nm";
        return pageNAVRadius+"km";
    }
    
    public void pageNAVRadiusIncrease()
    {
        pageNAVRadius *= 2;
        
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            pageNAVRadius = pageNAVRadius > 160 ? 160 : pageNAVRadius;
        else if (MetricSystem.METRIC.equals(metricSystem))
            pageNAVRadius = pageNAVRadius > 320 ? 320 : pageNAVRadius;
        
        triggerUpdate();
    }
    
    public void pageNAVRadiusDecrease()
    {
        pageNAVRadius /= 2;
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            pageNAVRadius = pageNAVRadius < 10 ? 10 : pageNAVRadius;
        else if (MetricSystem.METRIC.equals(metricSystem))
            pageNAVRadius = pageNAVRadius < 20 ? 20 : pageNAVRadius;
        
        triggerUpdate();
    }

    public boolean isBullseyeInverted()
    {
        return bullseyeInverted;
    }

    public void setBullseyeInverted(boolean bullseyeInverted)
    {
        this.bullseyeInverted = bullseyeInverted;
        triggerUpdate();
    }

    public double getBeX()
    {
        return beX;
    }

    public double getBeY()
    {
        return beY;
    }

    public String getBeXStr()
    {
        return getLLfromDeg(beX, false);
    }

    public String getBeYStr()
    {
        return getLLfromDeg(beY, true);
    }

    public void setBe(double beX, double beY)
    {
        this.beX = beX;
        this.beY = beY;
        triggerUpdate();
    }

    public void setBe(double beX1, double beX2, double beX3, double beY1, double beY2, double beY3)
    {
        this.beX = beX1+(beX2+(beX3/60))/60;
        this.beY = beY1+(beY2+(beY3/60))/60;
        triggerUpdate();
    }
    
    public String getBeBRAStr()
    {
        double d = getBEDistance();
        String dis;
        // Distance to nm
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            dis = new BigDecimal(d).setScale(1, RoundingMode.HALF_UP) + "nm";
        else
            dis = new BigDecimal(d).setScale(1, RoundingMode.HALF_UP) + "km";
        int bear = (int) getBEBearing();
        if (bullseyeInverted)
            bear -= 180;
        if (bear < 0)
            bear +=360;
        return bear+"°/"+dis;
    }
    
    public String getWPBRAStr()
    {
        double d = getDistanceToPoint(simData.curWaypointX, simData.curWaypointY);
        String dis;
        // Distance to nm
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            dis = new BigDecimal(d).setScale(1, RoundingMode.HALF_UP) + "nm";
        else
            dis = new BigDecimal(d).setScale(1, RoundingMode.HALF_UP) + "km";
        int bear = (int) getBearingToPoint(simData.curWaypointX, simData.curWaypointY);
        if (bear < 0)
            bear +=360;
        return bear+"°/"+dis;
    }
    
    public double getBEBearing()
    {
        return getBearingToPoint(beX, beY);
    }
    
    public double getBEBearingDelta()
    {
        return getSimData().getHeading() - getBEBearing();
    }
    
    public double getBEDistance()
    {
        return getDistanceToPoint(beX, beY);
    }
    
    // -------------------------------------------------------------------------
    
    public double getBearingDelta(double bearing)
    {
        return getSimData().getHeading() - bearing;
    }
    
    public double getDistanceToPoint(double x, double y)
    {
        return getDistanceFromToPoint(getSimData().getPosX(), getSimData().getPosY(), x, y);
    }
    
    public double getBearingToPoint(double _x, double _y)
    {
        return getBearingFromToPoint(getSimData().getPosX(), getSimData().getPosY(), _x, _y);
    }
    
    public double getDistanceFromToPoint(double fx, double fy, double x, double y)
    {
        double lat1 = fy;
        double lon1 = fx;
        double lat2 = y;
        double lon2 = x;
        
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            d = d * 0.539957;
        
        return d;
    }
    
    public double getBearingFromToPoint(double fx, double fy, double _x, double _y)
    {
        double lat1 = fy;
        double lon1 = fx;
        double lat2 = _y;
        double lon2 = _x;
        
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));
        
        brng-= 5; // WHY???
        
        if (brng < 0)
            brng += 360;
        
        return brng;
    }
    
    public String getLLfromDeg(double _d, boolean lat)
    {
        if (MetricSystem.IMPERIAL.equals(metricSystem))
        {
            int deg = (int) _d;
            String s1 = String.valueOf(deg);
            s1 = "00" + s1;
            s1 = s1.substring(s1.length() - 2, s1.length());
            String s2 = new BigDecimal((_d * 60) % 60).setScale(3, RoundingMode.HALF_UP).toString();
            s2 =  "000000" + s2;
            s2 = s2.substring(s2.length() - 6, s2.length());
            char l = ' ';
            if (lat)
                l = _d < 0 ? 'S' : 'N';
            else
                l = _d < 0 ? 'W' : 'E';
            return s1+"°"+s2+"\'"+l;
        }
        else
        {
            double d = _d < 0 ? - _d : _d;
            int deg = (int) d;
            int deg1 = (int) (d * 60 % 60);
            int deg2 = (int) ((d * 60 % 60) * 60 % 60);
            String s1 = String.valueOf(deg);
            s1 = "00" + s1;
            s1 = s1.substring(s1.length() - 2, s1.length());
            String s2 = String.valueOf(deg1);
            s2 = "00" + s2;
            s2 = s2.substring(s2.length() - 2, s2.length());
            String s3 = String.valueOf(deg2);
            s3 = "00" + s3;
            s3 = s3.substring(s3.length() - 2, s3.length());
            char l = ' ';
            if (lat)
                l = _d < 0 ? 'S' : 'N';
            else
                l = _d < 0 ? 'W' : 'E';
                
            return s1+"°"+s2 + "\'"+s3+"\""+l;
        }
    }
    
    public double ftFromMeters(double m)
    {
        return m * 3.281d;
    }
    
    private static final long FRAME_WAIT = 50;
    long lastTime = 0;
    private void triggerUpdate()
    {
        long t = System.currentTimeMillis();
        if (Math.abs(t - lastTime) > FRAME_WAIT)
        {
            setChanged();
            notifyObservers();
        }
        lastTime = t;
    }
}
