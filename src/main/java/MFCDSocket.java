import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class MFCDSocket
{
    private static final int RECONNECTION_TIMEOUT = 3000;
    private static final String MSG_ALTITUDE = "ALT";
    private static final String MSG_POSITION_LL = "PLL";
    private static final String MSG_HEADING_BANK_PITCH = "HBP";
    private static final String MSG_FUEL = "FUEL";
    private static final String MSG_RPM = "RPM";
    private static final String MSG_ENGINE_TEMP = "ET";
    private static final String MSG_EXIT = "EXIT";
    private static final String MSG_WAYPOINT = "WP";
    private static final String MSG_LAND = "LAND";
    
    final MFCDStatus status;
    final Timer timer;
    Socket socket = null;
    Thread dataRead = null;
    
    public static enum Messages {RDALT}

    public MFCDSocket(MFCDStatus status)
    {
        if (status == null)
            throw new IllegalArgumentException("status is null");
        this.status = status;
        this.timer = new Timer(true);
        this.timer.schedule(socketTask, 0, RECONNECTION_TIMEOUT);
    }

    public void readSocket()
    {
        while (socket != null && !socket.isClosed())
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (reader.ready() && !socket.isClosed())
                {
                    String s = reader.readLine();
                    if (s != null)
                    {
                        String[] pars = s.split(":");
                        try
                        {
                            switch (pars[0])
                            {
                                case MSG_ALTITUDE:
                                    if (pars.length > 2)
                                    {
                                        status.getSimData().setBarometricAlt(Double.parseDouble(pars[1]));
                                        status.getSimData().setRadarAlt(Double.parseDouble(pars[2]));
                                    }
                                    break;
                                case MSG_POSITION_LL:
                                    if (pars.length > 2)
                                        status.getSimData().setPosition(Double.parseDouble(pars[1]), Double.parseDouble(pars[2]));
                                    break;
                                case MSG_HEADING_BANK_PITCH:
                                    if (pars.length > 3)
                                        status.getSimData().setHBP(Double.parseDouble(pars[1]), Double.parseDouble(pars[2]), Double.parseDouble(pars[3]));
                                    break;
                                case MSG_FUEL:
                                    if (pars.length > 2)
                                        status.getSimData().setFuel(Double.parseDouble(pars[1]), Double.parseDouble(pars[2]));
                                    break;
                                case MSG_RPM:
                                    if (pars.length > 2)
                                        status.getSimData().setRPM(Double.parseDouble(pars[1]), Double.parseDouble(pars[2]));
                                    break;
                                case MSG_ENGINE_TEMP:
                                    if (pars.length > 2)
                                        status.getSimData().setEngineTemp(Double.parseDouble(pars[1]), Double.parseDouble(pars[2]));
                                    break;
                                case MSG_WAYPOINT:
                                    if (pars.length > 4)
                                        status.getSimData().addWaypoint(Integer.parseInt(pars[1]), Double.parseDouble(pars[2]), Double.parseDouble(pars[3]), Double.parseDouble(pars[4]));
                                    break;
                                case MSG_EXIT:
                                    socket.close();
                                    socket = null;
                                    dataRead.interrupt();
                                    dataRead = null;
                                    status.setConnected(false);
                                    break;
                                case MSG_LAND:
                                    if (pars.length > 3)
                                        status.getSimData().landingAt(pars[1], Double.parseDouble(pars[2]), Double.parseDouble(pars[3]));
                                    break;
                            }
                        }
                        catch (Exception e) {/*Catch all conversion errors*/}
                    }
                }
            }
            catch (IOException ex)
            {
                Logger.getLogger(MFCDSocket.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        
        if (socket != null && socket.isClosed())
            status.setConnected(false);
    }
    
    TimerTask socketTask = new TimerTask()
    {
        @Override
        public void run()
        {
            if (socket == null || socket.isClosed())
            {
                // Close an existing data read thread with a non valid socket
                if (dataRead != null)
                {
                    dataRead.interrupt();
                    dataRead = null;
                }
                // Open a new socket
                try
                {
                    status.reset();
                    socket = new Socket("localhost", 20000);
                    socket.setSoLinger(true, 5);
                    if (!socket.isClosed())
                    {
                        // notify of connection
                        status.setConnected(true);
                        // start a new socket read
                        dataRead = new Thread(MFCDSocket.this::readSocket);
                        dataRead.start();
                    }
                }
                catch (IOException ex)
                {
//                    Logger.getLogger(MFCDDataInject.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
            else
            {
                if (!status.isConnected())
                    status.setConnected(true);
            }
        }
    };
    
    public void forceReconnect()
    {
        try
        {
            socket.close();
            socket.getInputStream().close();
            dataRead.interrupt();
        }
        catch (IOException ex)
        {
            Logger.getLogger(MFCDSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        dataRead = null;
        socket = null;
    }
}
