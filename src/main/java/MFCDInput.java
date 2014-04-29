
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;


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
        pageMaps.put(MFCDStatus.Page.NAV, this::delegatePageOSBClock_NAV);
        pageMaps.put(MFCDStatus.Page.STG, this::delegatePageOSBClock_STG);
        pageMaps.put(MFCDStatus.Page.ENG, this::delegatePageOSBClock_ENG);
        pageMaps.put(MFCDStatus.Page.WPT, this::delegatePageOSBClock_WPT);
    }
    
    volatile Timer longPressTimer = null;
    volatile int longClicked = 0;
    
    public void osbDown(final int num)
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
    
    public void osbUp(int num)
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
        // Extra repaint - bug fix
        Main.main.canvas.repaint();
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
    
    
    public void delegatePageOSBClock_WPT(int osbnum)
    {
        switch (osbnum)
        {
            case 6: // SET BE HERE
                status.setBe(status.getSimData().getCurWaypointX(), status.getSimData().getCurWaypointY());
                break;
            case 7: // BE OFFSET
                Main.main.offsetFrom(status.getBeX(), status.getBeY());
                break;
            case 8: // WP OFFSET
                if (status.getSimData().getCurWaypointNum() != -1)
                    Main.main.offsetFrom(status.getSimData().getCurWaypointX(), status.getSimData().getCurWaypointY());
                break;
        }
    }
    
    public void delegatePageOSBClock_NAV(int osbnum)
    {
        
        switch (osbnum)
        {
            case 20: // Range decrease
                status.pageNAVRadiusDecrease();
                break;
            case 19: // Range increase
                status.pageNAVRadiusIncrease();
                break;
        }
    }
    
    public void delegatePageOSBClock_STG(int osbnum)
    {
        switch (osbnum)
        {
            case 10: //IMPERIAL/METRIC
                if (MFCDStatus.MetricSystem.IMPERIAL.equals(status.getMetricSystem()))
                    status.setMetricSystem(MFCDStatus.MetricSystem.METRIC);
                else
                    status.setMetricSystem(MFCDStatus.MetricSystem.IMPERIAL);
                break;
            case 9:
                status.setBullseyeInverted(!status.isBullseyeInverted());
                break;
            case 8:
                Main.main.changeBE();
                break;
            case 20: // RESET
                status.getSimData().getWaypoints().clear();
                status.getMarkpoints().clear();
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
            case 7: // MK here
                status.addMark(status.getSimData().getPosX(), status.getSimData().getPosY());
                break;
        }
    }
    
    public void delegatePageOSBClock_ENG(int osbnum)
    {
        // DO nothing or TEST something
    }
    
    Thread joystickThread = null;
    Controller joystick = null;
    public void connectJoystick(Controller c)
    {
        if (joystickThread != null)
            joystickThread.interrupt();
        joystick = c;
        joystickThread = new Thread(joystickRunnable);
        joystickThread.start();
    }
    Runnable joystickRunnable = () ->
    {
        try
        {
            while(joystick != null)
            {
                joystick.poll();
                EventQueue queue = joystick.getEventQueue();
                Event event = new Event();
                while (queue.getNextEvent(event))
                {
                    Component comp = event.getComponent();
                    try
                    {
                        final int num = Integer.parseInt(comp.getIdentifier().getName()) + 1;
                        if (event.getValue() != 0)
                            osbDown(num);
                        else
                            osbUp(num);
                    }
                    catch (NumberFormatException | NullPointerException e){}
                    Thread.sleep(1);
                }
            }
        }
        catch (InterruptedException e)
        {
            // Just exit the loop
        }
    };
}