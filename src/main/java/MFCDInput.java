
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;


/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class MFCDInput
{
    final MFCDStatus status;
    final Map<MFCDStatus.Page, Consumer<Integer>> pageMaps;
    
    public MFCDInput(MFCDStatus status)
    {
        this.status = status;
        pageMaps = new HashMap<>();
        pageMaps.put(MFCDStatus.Page.TST, this::delegatePageOSBClock_TEST);
        pageMaps.put(MFCDStatus.Page.POS, this::delegatePageOSBClock_POS);
        pageMaps.put(MFCDStatus.Page.NAV, this::delegatePageOSBClock_MAP);
        pageMaps.put(MFCDStatus.Page.SMS, this::delegatePageOSBClock_SMS);
        pageMaps.put(MFCDStatus.Page.STG, this::delegatePageOSBClock_PREF);
        pageMaps.put(MFCDStatus.Page.ENG, this::delegatePageOSBClock_ENG);
    }
    
    Timer longPressTimer = null;
    int longClicked = 0;
    
    private static final Object LOCK = new Object();
    
    public void osbDown(final int num)
    {
        synchronized(LOCK)
        {
            if (longPressTimer != null)
            {
                longPressTimer.cancel();
                longPressTimer = null;
            }

            status.setOsbDown(num);

            longPressTimer = new Timer();
            longPressTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    longClicked = num;
                    osbLongClicked(num);
                }
            }, 550);
        }
    }
    
    public void osbUp(int num)
    {
        synchronized(LOCK)
        {
            if (longPressTimer != null)
            {
                longPressTimer.cancel();
                longPressTimer = null;
            }

            status.setOsbDown(0);

            if (longClicked != 0)
                longClicked = 0;
            else
                osbClicked(num);
        }
    }
    
    public void osbClicked(int num)
    {
        switch (num)
        {
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                // Page change
                if (status.isPageSelectionMenu() && status.getPageSelectionItem() > 0)
                {
                    int newReplace = status.getPageSelectionItem() - 1;
                    if (newReplace > 14)
                        newReplace -= 5;
                    int newSelected = 5 - (num - 10);
                    if (newSelected >= 0 && newSelected < status.getPageSet().length)
                        if (newReplace >= 0 && newReplace < MFCDStatus.LOADPAGE_ITEMS.length)
                        {
                            MFCDStatus.Page newPage = MFCDStatus.LOADPAGE_ITEMS[newReplace];
                            status.changePageAt(newPage, newSelected);
                            status.setPageSelectionItem(0);
                        }
                }
                else
                {
                    status.setPageSelectionMenu(false);
                    // Page select
                    int newSelected = 5 - (num - 10);
                    if (newSelected >= 0 && newSelected < status.getPageSet().length && status.getPageSet()[newSelected] != null)
                        status.setSelectedPage(newSelected);
                }
                break;
            default:
                int selectedPage = status.getSelectedPage();
                delegatePageOSBClick(selectedPage, num);
                break;
        }
        
    }
    public void osbLongClicked(int num)
    {
        longClicked = num;
        switch (num)
        {
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                // Page change menu
                status.setPageSelectionMenu(true);
                // Also page is deselected
                status.setSelectedPage(-1);
                break;
            default:
                // Consider a normal click for other buttons
                osbClicked(num);
                break;
        }
    }
    
    public void delegatePageOSBClick(int selectedPage, int osbnum)
    {
        if (status.isPageSelectionMenu())
            delegateSelectionPageOSBClick(osbnum);
        else
        {
            MFCDStatus.Page[] pageSet = status.getPageSet();
            if (selectedPage >= 0 && selectedPage < pageSet.length && pageSet[selectedPage] != null)
            {
                Consumer<Integer> f = pageMaps.get(pageSet[selectedPage]);
                if (f != null)
                    f.accept(osbnum);
            }
        }
    }
    
    public void delegateSelectionPageOSBClick(int osbnum)
    {
        // Exclude bottom ones
        if ((osbnum > 0 && osbnum < 11) || osbnum > 15)
        {
            int oldSelected = status.getPageSelectionItem();
            // If already selected, just deselect it
            if (oldSelected == osbnum)
                status.setPageSelectionItem(0);
            else
                status.setPageSelectionItem(osbnum);
        }            
    }
    
    public void delegatePageOSBClock_TEST(int osbnum)
    {
        // DO nothing or TEST something
    }
    
    public void delegatePageOSBClock_MAP(int osbnum)
    {
        // DO nothing or TEST something
    }
    
    public void delegatePageOSBClock_SMS(int osbnum)
    {
        // DO nothing or TEST something
    }
    
    public void delegatePageOSBClock_PREF(int osbnum)
    {
        switch (osbnum)
        {
            case 10: //IMPERIAL/METRIC
                if (MFCDStatus.MetricSystem.IMPERIAL.equals(status.getMetricSystem()))
                    status.setMetricSystem(MFCDStatus.MetricSystem.METRIC);
                else
                    status.setMetricSystem(MFCDStatus.MetricSystem.IMPERIAL);
                break;
        }
    }
    
    public void delegatePageOSBClock_POS(int osbnum)
    {
        switch (osbnum)
        {
            case 16: // ALT SWITCH TYPE
                status.setPagePOSAltRadar(!status.isPagePOSAltRadar());
                break;
            case 6: // BE-BRA change BE
                Main.main.changeBE();
                break;
        }
    }
    
    public void delegatePageOSBClock_ENG(int osbnum)
    {
        // DO nothing or TEST something
    }
}
