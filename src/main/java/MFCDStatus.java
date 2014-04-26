
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Observable;

/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class MFCDStatus extends Observable
{
    // Only lower buttons can have paes, num = 5
    private static final int PAGE_SET_NUM = 5;
    public static final MFCDStatus.Page[] LOADPAGE_ITEMS = new MFCDStatus.Page[]
    {
        null, null, null, null, null,
        Page.POS, Page.SMS, Page.NAV, null, null,
        null, null, Page.ENG, Page.STG, Page.TST,
    };
    
    public static enum Page {TST, POS, SMS, NAV, STG, ENG}
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
                    MFCDStatus.this.setChanged();
                    MFCDStatus.this.notifyObservers();
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
                    MFCDStatus.this.setChanged();
                    MFCDStatus.this.notifyObservers();
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
                num = new BigDecimal(barometricAlt).multiply(new BigDecimal(3.281)).setScale(0, BigDecimal.ROUND_HALF_UP);
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
                MFCDStatus.this.setChanged();
                MFCDStatus.this.notifyObservers();
            }
        }
        
        private double posX = 0; // Long
        private double posY = 0; // Lat
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
            MFCDStatus.this.setChanged();
            MFCDStatus.this.notifyObservers();
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
            MFCDStatus.this.setChanged();
            MFCDStatus.this.notifyObservers();
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
                    iFuelConsumption = (int) (consumpion * 2.225 * 3600); // to PPH
                    s = "     "+String.valueOf(iFuelLeft);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelLeft = s + " lb";
                    s = "     "+String.valueOf(iFuelConsumption);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelConsumption = s + " pph";
                    break;
                case METRIC:
                    iFuelLeft = (int) left;
                    iFuelConsumption = (int) (consumpion * 3600);
                    s = "     "+String.valueOf(iFuelLeft);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelLeft = s + " kg";
                    s = "     "+String.valueOf(iFuelConsumption);
                    s = s.substring(s.length() - 5, s.length()); 
                    this.fuelConsumption = s + " kph";
                    break;
            }
            MFCDStatus.this.setChanged();
            MFCDStatus.this.notifyObservers();
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
            MFCDStatus.this.setChanged();
            MFCDStatus.this.notifyObservers();
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
            MFCDStatus.this.setChanged();
            MFCDStatus.this.notifyObservers();
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
    // POS PAGE
    private boolean pagePOSAltRadar = true;
    
    // BE in LL degrees
    // Defaults to BLUE-POTI
    private double beX = 41.678888; //Long
    private double beY = 42.186388; // Lat
    
    public MFCDStatus()
    {
        pageSet = new Page[PAGE_SET_NUM];
        pageSet[0] = Page.NAV;
        pageSet[1] = Page.SMS;
        pageSet[2] = Page.POS;
        pageSet[3] = Page.ENG;
        pageSet[4] = Page.STG;
        
        selectedPage = 0;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void setConnected(boolean connected)
    {
        this.connected = connected;
        setChanged();
        notifyObservers();
    }

    public int getSelectedPage()
    {
        return selectedPage;
    }

    public void setSelectedPage(int selectedPage)
    {
        this.selectedPage = selectedPage;
        setChanged();
        notifyObservers();
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
            setChanged();
            notifyObservers();
        }
    }

    public SimData getSimData()
    {
        return simData;
    }

    public MetricSystem getMetricSystem()
    {
        return metricSystem;
    }

    public void setMetricSystem(MetricSystem metricSystem)
    {
        this.metricSystem = metricSystem;
        setChanged();
        notifyObservers();
    }

    public int getOsbDown()
    {
        return osbDown;
    }

    public void setOsbDown(int osbDown)
    {
        this.osbDown = osbDown;
        setChanged();
        notifyObservers();
    }

    public boolean isPageSelectionMenu()
    {
        return pageSelectionMenu;
    }

    public void setPageSelectionMenu(boolean pageSelectionMenu)
    {
        this.pageSelectionMenu = pageSelectionMenu;
        setChanged();
        notifyObservers();
    }

    public int getPageSelectionItem()
    {
        return pageSelectionItem;
    }

    public void setPageSelectionItem(int pageSelectionItem)
    {
        this.pageSelectionItem = pageSelectionItem;
        setChanged();
        notifyObservers();
    }

    public boolean isPagePOSAltRadar()
    {
        return pagePOSAltRadar;
    }

    public void setPagePOSAltRadar(boolean pagePOSAltRadar)
    {
        this.pagePOSAltRadar = pagePOSAltRadar;
        setChanged();
        notifyObservers();
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
        setChanged();
        notifyObservers();
    }

    public void setBe(double beX1, double beX2, double beX3, double beY1, double beY2, double beY3)
    {
        this.beX = beX1+(beX2+(beX3/60))/60;
        this.beY = beY1+(beY2+(beY3/60))/60;
        setChanged();
        notifyObservers();
    }
    
    public String getBeBRAStr()
    {
        double d = getBEDistance();
        String dis;
        // Distance to nm
        if (MetricSystem.IMPERIAL.equals(metricSystem))
            dis = new BigDecimal(d * 0.539957).setScale(1, RoundingMode.HALF_UP) + "nm";
        else
            dis = new BigDecimal(d).setScale(1, RoundingMode.HALF_UP) + "km";
        return ((int)getBEBearing())+"°/"+dis;
    }
    
    public double getBEBearing()
    {
        double lat1 = getSimData().getPosY();
        double lon1 = getSimData().getPosX();
        double lat2 = beY;
        double lon2 = beX;
        
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
//        lon1 = Math.toRadians(lon1);
//        lon2 = Math.toRadians(lon2);
        
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));
        
        brng-= 5; // WHY???
        
        if (brng < 0)
            brng += 360;
        
        return brng;
    }
    
    public double getBEDistance()
    {
        double lat1 = getSimData().getPosY();
        double lon1 = getSimData().getPosX();
        double lat2 = beY;
        double lon2 = beX;
        
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
//        lon1 = Math.toRadians(lon1);
//        lon2 = Math.toRadians(lon2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        
        return d;
    }
    
    public String getLLfromDeg(double _d, boolean lat)
    {
//        if (MetricSystem.IMPERIAL.equals(metricSystem))
//        {
//            int deg = (int) d;
//            String s1 = String.valueOf(deg);
//            s1 = "00" + s1;
//            s1 = s1.substring(s1.length() - 2, s1.length());
//            String s2 = new BigDecimal((d * 60) % 60).setScale(3, RoundingMode.HALF_UP).toString();
//            s2 =  "000000" + s2;
//            s2 = s2.substring(s2.length() - 6, s2.length());
//            return s1+" "+s2;
//        }
//        else
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
}
