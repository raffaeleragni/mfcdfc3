
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public final class Main extends JFrame
{
    static
    {
        System.setProperty("sun.java2d.transaccel", "true");
        System.setProperty("sun.java2d.d3d", "true");
        System.setProperty("sun.java2d.ddforcevram", "true");
        System.setProperty("sun.java2d.noddraw", "false");
        System.setProperty("sun.java2d.accthreshold", "0");
    }
    
    final MFCDStatus status;
    final MFCDCanvas canvas;
    final MFCDInput input;
    final MFCDSocket socket;
    final SetBEForm setBEForm;
    final SetUseMFD setMFDForm;
    final OffsetForm offsetForm;
    boolean collapsed = false;
    int borderFactor = 60;
    private List<JButton> OSBs;
    private Point initialClickMove;
    
    public Main()
    {
        initComponents();
        setBEForm = new SetBEForm();
        setMFDForm = new SetUseMFD();
        offsetForm = new OffsetForm();
        status = new MFCDStatus();
        canvas = new MFCDCanvas(status);
        input = new MFCDInput(status);
        socket = new MFCDSocket(status);
        drawPanel.add(canvas);
        canvas.revalidate();
        canvas.repaint();
    
        OSBs = Arrays.asList( OSB1, OSB10, OSB11, OSB12, OSB13, OSB14, OSB15, OSB16, OSB17, OSB18,
            OSB19, OSB2, OSB20, OSB3, OSB4, OSB5, OSB6, OSB7, OSB8, OSB9);
        
        KeyboardFocusManager
            .getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher((final KeyEvent e) ->
        {
            if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_F1)
                toggleButtons();
            if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_F2)
                changeBE();
            else if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_F3)
                changeMFD();
            else if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_F4)
                forceReconnect();
            else if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_F12)
                quit();
            // Pass the KeyEvent to the next KeyEventDispatcher in the chain
            return false;
        });
        
        canvas.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                initialClickMove = e.getPoint();
                getComponentAt(initialClickMove);
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON3)
                    popMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                // get location of Window
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = (thisX + e.getX()) - (thisX + initialClickMove.x);
                int yMoved = (thisY + e.getY()) - (thisY + initialClickMove.y);

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });
    }
    
    public void toggleButtons()
    {
        if (collapsed)
        {
            Point prevLoc = getLocation();
            setSize(getWidth() + borderFactor*2, getHeight() + borderFactor*2);
            drawPanel.setLocation(borderFactor, borderFactor);
            setLocation(prevLoc.x-borderFactor, prevLoc.y-borderFactor);
        }
        else
        {
            Point prevLoc = getLocation();
            setSize(drawPanel.getWidth(), drawPanel.getHeight());
            setLocation(prevLoc.x+drawPanel.getX(), prevLoc.y+drawPanel.getY());
            drawPanel.setLocation(0, 0);
        }
        collapsed = !collapsed;
    }
    
    public void resize(int newSize)
    {
        Dimension currentSize = drawPanel.getSize();
        Point location = drawPanel.getLocation();
        
        final double factor = (double) newSize / (double) currentSize.width;
        borderFactor *= factor;
        
        location.x *= factor;
        location.y *= factor;
        drawPanel.setLocation(location);
        drawPanel.setSize(newSize, newSize);
        Dimension mainSize = getSize();
        mainSize.width *= factor;
        mainSize.height *= factor;
        setSize(mainSize);
        OSBs.stream().forEach((osb)->
        {
            Point p = osb.getLocation();
            Dimension d = osb.getSize();
            p.x *= factor;
            p.y *= factor;
            d.width *= factor;
            d.height *= factor;
            osb.setLocation(p);
            osb.setSize(d);
        });
    }
    
    public void quit()
    {
        System.exit(0);
    }

    public void offsetFrom(double x, double y)
    {
        Dimension d = getSize();
        Point p = getLocation();
        Dimension fd = offsetForm.getSize();
        offsetForm.setLocation(p.x + d.width/2 - fd.width/2, p.y + d.height/2 - fd.height/2);
        offsetForm.x = x;
        offsetForm.y = y;
        offsetForm.setVisible(true);
        offsetForm.clear();
    }
    
    public void changeBE()
    {
        Dimension d = getSize();
        Point p = getLocation();
        Dimension fd = setBEForm.getSize();
        setBEForm.setLocation(p.x + d.width/2 - fd.width/2, p.y + d.height/2 - fd.height/2);
        setBEForm.setVisible(true);
    }
    
    public void changeMFD()
    {
        Dimension d = getSize();
        Point p = getLocation();
        Dimension fd = setMFDForm.getSize();
        setMFDForm.setLocation(p.x + d.width/2 - fd.width/2, p.y + d.height/2 - fd.height/2);
        setMFDForm.setVisible(true);
    }
    
    public void forceReconnect()
    {
        socket.forceReconnect();
    }
    
    private void clickIn()
    {
    }
    private void clickOut()
    {
    }
    
    public void buttonReposition()
    {
        
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        popMenu = new javax.swing.JPopupMenu();
        menuToggleButtons = new javax.swing.JMenuItem();
        menuSetBE = new javax.swing.JMenuItem();
        menuMFD = new javax.swing.JMenuItem();
        menuForceReconnect = new javax.swing.JMenuItem();
        menuQuit = new javax.swing.JMenuItem();
        drawPanel = new javax.swing.JPanel();
        OSB6 = new javax.swing.JButton();
        OSB7 = new javax.swing.JButton();
        OSB8 = new javax.swing.JButton();
        OSB9 = new javax.swing.JButton();
        OSB10 = new javax.swing.JButton();
        OSB15 = new javax.swing.JButton();
        OSB14 = new javax.swing.JButton();
        OSB13 = new javax.swing.JButton();
        OSB12 = new javax.swing.JButton();
        OSB11 = new javax.swing.JButton();
        OSB1 = new javax.swing.JButton();
        OSB2 = new javax.swing.JButton();
        OSB3 = new javax.swing.JButton();
        OSB4 = new javax.swing.JButton();
        OSB5 = new javax.swing.JButton();
        OSB20 = new javax.swing.JButton();
        OSB19 = new javax.swing.JButton();
        OSB18 = new javax.swing.JButton();
        OSB17 = new javax.swing.JButton();
        OSB16 = new javax.swing.JButton();

        menuToggleButtons.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        menuToggleButtons.setText("Toggle borders");
        menuToggleButtons.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuToggleButtonsActionPerformed(evt);
            }
        });
        popMenu.add(menuToggleButtons);

        menuSetBE.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        menuSetBE.setText("Set Bullseye");
        menuSetBE.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuSetBEActionPerformed(evt);
            }
        });
        popMenu.add(menuSetBE);

        menuMFD.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        menuMFD.setText("Use MFD");
        menuMFD.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuMFDActionPerformed(evt);
            }
        });
        popMenu.add(menuMFD);

        menuForceReconnect.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        menuForceReconnect.setText("Force reconnect");
        menuForceReconnect.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuForceReconnectActionPerformed(evt);
            }
        });
        popMenu.add(menuForceReconnect);

        menuQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        menuQuit.setText("Quit");
        menuQuit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuQuitActionPerformed(evt);
            }
        });
        popMenu.add(menuQuit);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setUndecorated(true);
        setResizable(false);
        addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentResized(java.awt.event.ComponentEvent evt)
            {
                formComponentResized(evt);
            }
        });
        getContentPane().setLayout(null);

        drawPanel.setBackground(new java.awt.Color(0, 0, 0));
        drawPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.MOVE_CURSOR));
        drawPanel.setPreferredSize(new java.awt.Dimension(520, 520));
        drawPanel.setRequestFocusEnabled(false);
        drawPanel.setVerifyInputWhenFocusTarget(false);
        drawPanel.setLayout(new javax.swing.BoxLayout(drawPanel, javax.swing.BoxLayout.LINE_AXIS));
        getContentPane().add(drawPanel);
        drawPanel.setBounds(60, 60, 520, 520);

        OSB6.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB6.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB6.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB6.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB6MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB6MouseReleased(evt);
            }
        });
        getContentPane().add(OSB6);
        OSB6.setBounds(590, 155, 42, 42);

        OSB7.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB7.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB7.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB7.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB7MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB7MouseReleased(evt);
            }
        });
        getContentPane().add(OSB7);
        OSB7.setBounds(590, 225, 42, 42);

        OSB8.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB8.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB8.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB8.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB8MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB8MouseReleased(evt);
            }
        });
        getContentPane().add(OSB8);
        OSB8.setBounds(590, 295, 42, 42);

        OSB9.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB9.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB9.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB9.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB9MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB9MouseReleased(evt);
            }
        });
        getContentPane().add(OSB9);
        OSB9.setBounds(590, 365, 42, 42);

        OSB10.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB10.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB10.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB10.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB10MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB10MouseReleased(evt);
            }
        });
        getContentPane().add(OSB10);
        OSB10.setBounds(590, 440, 42, 42);

        OSB15.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB15.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB15.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB15.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB15MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB15MouseReleased(evt);
            }
        });
        getContentPane().add(OSB15);
        OSB15.setBounds(130, 590, 42, 42);

        OSB14.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB14.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB14.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB14.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB14MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB14MouseReleased(evt);
            }
        });
        getContentPane().add(OSB14);
        OSB14.setBounds(210, 590, 42, 42);

        OSB13.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB13.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB13.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB13.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB13MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB13MouseReleased(evt);
            }
        });
        getContentPane().add(OSB13);
        OSB13.setBounds(285, 590, 42, 42);

        OSB12.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB12.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB12.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB12.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB12MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB12MouseReleased(evt);
            }
        });
        getContentPane().add(OSB12);
        OSB12.setBounds(365, 590, 42, 42);

        OSB11.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB11.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB11.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB11.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB11MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB11MouseReleased(evt);
            }
        });
        getContentPane().add(OSB11);
        OSB11.setBounds(440, 590, 42, 42);

        OSB1.setMaximumSize(new java.awt.Dimension(40, 40));
        OSB1.setMinimumSize(new java.awt.Dimension(40, 40));
        OSB1.setPreferredSize(new java.awt.Dimension(40, 40));
        OSB1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB1MouseReleased(evt);
            }
        });
        getContentPane().add(OSB1);
        OSB1.setBounds(130, 10, 40, 40);

        OSB2.setMaximumSize(new java.awt.Dimension(40, 40));
        OSB2.setMinimumSize(new java.awt.Dimension(40, 40));
        OSB2.setPreferredSize(new java.awt.Dimension(40, 40));
        OSB2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB2MouseReleased(evt);
            }
        });
        getContentPane().add(OSB2);
        OSB2.setBounds(210, 10, 40, 40);

        OSB3.setMaximumSize(new java.awt.Dimension(40, 40));
        OSB3.setMinimumSize(new java.awt.Dimension(40, 40));
        OSB3.setPreferredSize(new java.awt.Dimension(40, 40));
        OSB3.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB3MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB3MouseReleased(evt);
            }
        });
        getContentPane().add(OSB3);
        OSB3.setBounds(285, 10, 40, 40);

        OSB4.setMaximumSize(new java.awt.Dimension(40, 40));
        OSB4.setMinimumSize(new java.awt.Dimension(40, 40));
        OSB4.setPreferredSize(new java.awt.Dimension(40, 40));
        OSB4.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB4MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB4MouseReleased(evt);
            }
        });
        getContentPane().add(OSB4);
        OSB4.setBounds(365, 10, 40, 40);

        OSB5.setMaximumSize(new java.awt.Dimension(40, 40));
        OSB5.setMinimumSize(new java.awt.Dimension(40, 40));
        OSB5.setPreferredSize(new java.awt.Dimension(40, 40));
        OSB5.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB5MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB5MouseReleased(evt);
            }
        });
        getContentPane().add(OSB5);
        OSB5.setBounds(440, 10, 40, 40);

        OSB20.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB20.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB20.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB20.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB20MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB20MouseReleased(evt);
            }
        });
        getContentPane().add(OSB20);
        OSB20.setBounds(10, 155, 42, 42);

        OSB19.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB19.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB19.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB19.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB19MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB19MouseReleased(evt);
            }
        });
        getContentPane().add(OSB19);
        OSB19.setBounds(10, 225, 42, 42);

        OSB18.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB18.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB18.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB18.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB18MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB18MouseReleased(evt);
            }
        });
        getContentPane().add(OSB18);
        OSB18.setBounds(10, 295, 42, 42);

        OSB17.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB17.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB17.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB17.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB17MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB17MouseReleased(evt);
            }
        });
        getContentPane().add(OSB17);
        OSB17.setBounds(10, 365, 42, 42);

        OSB16.setMaximumSize(new java.awt.Dimension(42, 42));
        OSB16.setMinimumSize(new java.awt.Dimension(42, 42));
        OSB16.setPreferredSize(new java.awt.Dimension(42, 42));
        OSB16.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                OSB16MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                OSB16MouseReleased(evt);
            }
        });
        getContentPane().add(OSB16);
        OSB16.setBounds(10, 440, 42, 42);

        setSize(new java.awt.Dimension(640, 640));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void OSB1MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB1MousePressed
    {//GEN-HEADEREND:event_OSB1MousePressed
        clickIn();
        input.osbDown(1);
    }//GEN-LAST:event_OSB1MousePressed

    private void OSB1MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB1MouseReleased
    {//GEN-HEADEREND:event_OSB1MouseReleased
        clickOut();
        input.osbUp(1);
    }//GEN-LAST:event_OSB1MouseReleased

    private void OSB2MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB2MousePressed
    {//GEN-HEADEREND:event_OSB2MousePressed
        clickIn();
        input.osbDown(2);
    }//GEN-LAST:event_OSB2MousePressed

    private void OSB2MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB2MouseReleased
    {//GEN-HEADEREND:event_OSB2MouseReleased
        clickOut();
        input.osbUp(2);
    }//GEN-LAST:event_OSB2MouseReleased

    private void OSB3MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB3MousePressed
    {//GEN-HEADEREND:event_OSB3MousePressed
        clickIn();
        input.osbDown(3);
    }//GEN-LAST:event_OSB3MousePressed

    private void OSB3MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB3MouseReleased
    {//GEN-HEADEREND:event_OSB3MouseReleased
        clickOut();
        input.osbUp(3);
    }//GEN-LAST:event_OSB3MouseReleased

    private void OSB4MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB4MousePressed
    {//GEN-HEADEREND:event_OSB4MousePressed
        clickIn();
        input.osbDown(4);
    }//GEN-LAST:event_OSB4MousePressed

    private void OSB4MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB4MouseReleased
    {//GEN-HEADEREND:event_OSB4MouseReleased
        clickOut();
        input.osbUp(4);
    }//GEN-LAST:event_OSB4MouseReleased

    private void OSB5MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB5MousePressed
    {//GEN-HEADEREND:event_OSB5MousePressed
        clickIn();
        input.osbDown(5);
    }//GEN-LAST:event_OSB5MousePressed

    private void OSB5MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB5MouseReleased
    {//GEN-HEADEREND:event_OSB5MouseReleased
        clickOut();
        input.osbUp(5);
    }//GEN-LAST:event_OSB5MouseReleased

    private void OSB6MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB6MousePressed
    {//GEN-HEADEREND:event_OSB6MousePressed
        clickIn();
        input.osbDown(6);
    }//GEN-LAST:event_OSB6MousePressed

    private void OSB6MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB6MouseReleased
    {//GEN-HEADEREND:event_OSB6MouseReleased
        clickOut();
        input.osbUp(6);
    }//GEN-LAST:event_OSB6MouseReleased

    private void OSB7MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB7MousePressed
    {//GEN-HEADEREND:event_OSB7MousePressed
        clickIn();
        input.osbDown(7);
    }//GEN-LAST:event_OSB7MousePressed

    private void OSB7MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB7MouseReleased
    {//GEN-HEADEREND:event_OSB7MouseReleased
        clickOut();
        input.osbUp(7);
    }//GEN-LAST:event_OSB7MouseReleased

    private void OSB8MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB8MousePressed
    {//GEN-HEADEREND:event_OSB8MousePressed
        clickIn();
        input.osbDown(8);
    }//GEN-LAST:event_OSB8MousePressed

    private void OSB8MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB8MouseReleased
    {//GEN-HEADEREND:event_OSB8MouseReleased
        clickOut();
        input.osbUp(8);
    }//GEN-LAST:event_OSB8MouseReleased

    private void OSB9MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB9MousePressed
    {//GEN-HEADEREND:event_OSB9MousePressed
        clickIn();
        input.osbDown(9);
    }//GEN-LAST:event_OSB9MousePressed

    private void OSB9MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB9MouseReleased
    {//GEN-HEADEREND:event_OSB9MouseReleased
        clickOut();
        input.osbUp(9);
    }//GEN-LAST:event_OSB9MouseReleased

    private void OSB10MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB10MousePressed
    {//GEN-HEADEREND:event_OSB10MousePressed
        clickIn();
        input.osbDown(10);
    }//GEN-LAST:event_OSB10MousePressed

    private void OSB10MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB10MouseReleased
    {//GEN-HEADEREND:event_OSB10MouseReleased
        clickOut();
        input.osbUp(10);
    }//GEN-LAST:event_OSB10MouseReleased

    private void OSB11MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB11MousePressed
    {//GEN-HEADEREND:event_OSB11MousePressed
        clickIn();
        input.osbDown(11);
    }//GEN-LAST:event_OSB11MousePressed

    private void OSB11MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB11MouseReleased
    {//GEN-HEADEREND:event_OSB11MouseReleased
        clickOut();
        input.osbUp(11);
    }//GEN-LAST:event_OSB11MouseReleased

    private void OSB12MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB12MousePressed
    {//GEN-HEADEREND:event_OSB12MousePressed
        clickIn();
        input.osbDown(12);
    }//GEN-LAST:event_OSB12MousePressed

    private void OSB12MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB12MouseReleased
    {//GEN-HEADEREND:event_OSB12MouseReleased
        clickOut();
        input.osbUp(12);
    }//GEN-LAST:event_OSB12MouseReleased

    private void OSB13MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB13MousePressed
    {//GEN-HEADEREND:event_OSB13MousePressed
        clickIn();
        input.osbDown(13);
    }//GEN-LAST:event_OSB13MousePressed

    private void OSB13MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB13MouseReleased
    {//GEN-HEADEREND:event_OSB13MouseReleased
        clickOut();
        input.osbUp(13);
    }//GEN-LAST:event_OSB13MouseReleased

    private void OSB14MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB14MousePressed
    {//GEN-HEADEREND:event_OSB14MousePressed
        clickIn();
        input.osbDown(14);
    }//GEN-LAST:event_OSB14MousePressed

    private void OSB14MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB14MouseReleased
    {//GEN-HEADEREND:event_OSB14MouseReleased
        clickOut();
        input.osbUp(14);
    }//GEN-LAST:event_OSB14MouseReleased

    private void OSB15MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB15MousePressed
    {//GEN-HEADEREND:event_OSB15MousePressed
        clickIn();
        input.osbDown(15);
    }//GEN-LAST:event_OSB15MousePressed

    private void OSB15MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB15MouseReleased
    {//GEN-HEADEREND:event_OSB15MouseReleased
        clickOut();
        input.osbUp(15);
    }//GEN-LAST:event_OSB15MouseReleased

    private void OSB16MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB16MousePressed
    {//GEN-HEADEREND:event_OSB16MousePressed
        clickIn();
        input.osbDown(16);
    }//GEN-LAST:event_OSB16MousePressed

    private void OSB16MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB16MouseReleased
    {//GEN-HEADEREND:event_OSB16MouseReleased
        clickOut();
        input.osbUp(16);
    }//GEN-LAST:event_OSB16MouseReleased

    private void OSB17MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB17MousePressed
    {//GEN-HEADEREND:event_OSB17MousePressed
        clickIn();
        input.osbDown(17);
    }//GEN-LAST:event_OSB17MousePressed

    private void OSB17MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB17MouseReleased
    {//GEN-HEADEREND:event_OSB17MouseReleased
        clickOut();
        input.osbUp(17);
    }//GEN-LAST:event_OSB17MouseReleased

    private void OSB18MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB18MousePressed
    {//GEN-HEADEREND:event_OSB18MousePressed
        clickIn();
        input.osbDown(18);
    }//GEN-LAST:event_OSB18MousePressed

    private void OSB18MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB18MouseReleased
    {//GEN-HEADEREND:event_OSB18MouseReleased
        clickOut();
        input.osbUp(18);
    }//GEN-LAST:event_OSB18MouseReleased

    private void OSB19MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB19MousePressed
    {//GEN-HEADEREND:event_OSB19MousePressed
        clickIn();
        input.osbDown(19);
    }//GEN-LAST:event_OSB19MousePressed

    private void OSB19MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB19MouseReleased
    {//GEN-HEADEREND:event_OSB19MouseReleased
        clickOut();
        input.osbUp(19);
    }//GEN-LAST:event_OSB19MouseReleased

    private void OSB20MousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB20MousePressed
    {//GEN-HEADEREND:event_OSB20MousePressed
        clickIn();
        input.osbDown(20);
    }//GEN-LAST:event_OSB20MousePressed

    private void OSB20MouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_OSB20MouseReleased
    {//GEN-HEADEREND:event_OSB20MouseReleased
        clickOut();
        input.osbUp(20);
    }//GEN-LAST:event_OSB20MouseReleased

    private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
    {//GEN-HEADEREND:event_formComponentResized
        canvas.onResize();
    }//GEN-LAST:event_formComponentResized

    private void menuToggleButtonsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuToggleButtonsActionPerformed
    {//GEN-HEADEREND:event_menuToggleButtonsActionPerformed
        toggleButtons();
    }//GEN-LAST:event_menuToggleButtonsActionPerformed

    private void menuQuitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuQuitActionPerformed
    {//GEN-HEADEREND:event_menuQuitActionPerformed
        quit();
    }//GEN-LAST:event_menuQuitActionPerformed

    private void menuSetBEActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuSetBEActionPerformed
    {//GEN-HEADEREND:event_menuSetBEActionPerformed
        changeBE();
    }//GEN-LAST:event_menuSetBEActionPerformed

    private void menuMFDActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuMFDActionPerformed
    {//GEN-HEADEREND:event_menuMFDActionPerformed
        changeMFD();
    }//GEN-LAST:event_menuMFDActionPerformed

    private void menuForceReconnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuForceReconnectActionPerformed
    {//GEN-HEADEREND:event_menuForceReconnectActionPerformed
        forceReconnect();
    }//GEN-LAST:event_menuForceReconnectActionPerformed

    public static final Main main;
    static
    {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        checkLUA();
        main = new Main();
    }
    
    public static void main(String args[])
    {
        Point position = null;
        Integer mfdIndex = null;
        Integer size = null;
        boolean hideButtons = false;
        for (String arg: args)
        {
            if (arg.equals("-noborders"))
                hideButtons = true;
            
            if (arg.startsWith("-xy"))
            {
                String params = arg.substring(3);
                String[] ssParams = params.split(",");
                if (ssParams.length > 1)
                    position = new Point(Integer.parseInt(ssParams[0]), Integer.parseInt(ssParams[1]));
            }
            
            if (arg.startsWith("-useMFD"))
                mfdIndex = Integer.parseInt(arg.substring(7)) - 1;
            
            if (arg.startsWith("-s"))
                size = Integer.parseInt(arg.substring(2));
        }
        
        final boolean _hideButtons = hideButtons;
        final Point _position = position;
        final Integer _mfdIndex = mfdIndex;
        final Integer _size = size;
        java.awt.EventQueue.invokeLater(() ->
        {
            if (_size != null)
                main.resize(_size);
            if (_hideButtons)
                main.toggleButtons();
            if (_position != null)
                main.setLocation(_position);
            if (_mfdIndex != null)
                main.setMFDForm.selectMFDAtIndex(_mfdIndex);
            main.setVisible(true);
        });
    }
    
    private static final String EXPORTLINE = "dofile(require('lfs').writedir()..'Scripts/MFCD_FC3.lua')";
    public static void checkLUA()
    {
        String home = System.getProperty("user.home");
        Path dir1 = Paths.get(home, "Saved Games", "DCS", "Scripts");
        Path dir2 = Paths.get(home, "Saved Games", "DCS.openbeta", "Scripts");
        Arrays.asList(dir1, dir2).stream().forEach((p)->
        {
            if (!p.toFile().exists())
                return;
            
            File sockfile = new File(p.toFile(), "MFCD_FC3.lua");
            File exportfile = new File(p.toFile(), "Export.lua");
            boolean foundline = false;
            byte[] buf = new byte[10240];
            int ct = 0;
            try
            {
                if (!exportfile.exists())
                    exportfile.createNewFile();
                
                try (InputStream is = Main.class.getResourceAsStream("SOCK.lua");
                    OutputStream os = new FileOutputStream(sockfile))
                {
                    while ((ct = is.read(buf)) > 0)
                        os.write(buf, 0, ct);
                }
            
                try (BufferedReader reader = new BufferedReader(new FileReader(exportfile)))
                {
                    while (!foundline && reader.ready())
                        foundline = reader.readLine().contains(EXPORTLINE);
                }
                
                if (!foundline)
                    try (BufferedWriter w = new BufferedWriter(new FileWriter(exportfile, true)))
                    {
                        w.write("\n"+EXPORTLINE+"\n");
                    }
            }
            catch (IOException e) {}
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OSB1;
    private javax.swing.JButton OSB10;
    private javax.swing.JButton OSB11;
    private javax.swing.JButton OSB12;
    private javax.swing.JButton OSB13;
    private javax.swing.JButton OSB14;
    private javax.swing.JButton OSB15;
    private javax.swing.JButton OSB16;
    private javax.swing.JButton OSB17;
    private javax.swing.JButton OSB18;
    private javax.swing.JButton OSB19;
    private javax.swing.JButton OSB2;
    private javax.swing.JButton OSB20;
    private javax.swing.JButton OSB3;
    private javax.swing.JButton OSB4;
    private javax.swing.JButton OSB5;
    private javax.swing.JButton OSB6;
    private javax.swing.JButton OSB7;
    private javax.swing.JButton OSB8;
    private javax.swing.JButton OSB9;
    private javax.swing.JPanel drawPanel;
    private javax.swing.JMenuItem menuForceReconnect;
    private javax.swing.JMenuItem menuMFD;
    private javax.swing.JMenuItem menuQuit;
    private javax.swing.JMenuItem menuSetBE;
    private javax.swing.JMenuItem menuToggleButtons;
    private javax.swing.JPopupMenu popMenu;
    // End of variables declaration//GEN-END:variables
}
