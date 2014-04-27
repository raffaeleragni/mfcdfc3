
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;


/**
 *
 * @author Raffaele Ragni <raffaele.ragni@gmail.com>
 */
public class SetUseMFD extends javax.swing.JFrame
{
    public SetUseMFD()
    {
        initComponents();
        List<Controller> result = Arrays.asList(ControllerEnvironment.getDefaultEnvironment().getControllers())
            .stream()
            .filter((p)-> p.getName().contains("MFD"))
            .collect(Collectors.toList());
        ComboBoxModel<Controller> model = new DefaultComboBoxModel<>(result.toArray(new Controller[]{}));
        combo.setModel(model);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        combo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();

        setTitle("Use MFD Joystick");
        setAlwaysOnTop(true);

        jLabel1.setText("Use this Joystick");

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnCancelActionPerformed(evt);
            }
        });

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(combo, 0, 202, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnOK))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void selectMFDAtIndex(int index)
    {
        if (index < 0 || index >= combo.getItemCount())
            return;
        combo.setSelectedIndex(index);
        Main.main.input.connectJoystick((Controller) combo.getSelectedItem());
    }
    
    private void btnOKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnOKActionPerformed
    {//GEN-HEADEREND:event_btnOKActionPerformed
        setVisible(false);
        if (combo.getSelectedIndex() < 0 || combo.getSelectedIndex() >= combo.getItemCount())
            return;
        
        Main.main.input.connectJoystick((Controller) combo.getSelectedItem());
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnCancelActionPerformed
    {//GEN-HEADEREND:event_btnCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JComboBox combo;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}