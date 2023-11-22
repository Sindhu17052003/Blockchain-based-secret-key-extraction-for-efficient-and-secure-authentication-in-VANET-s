
package vehicle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) 
    {        
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        try
        {                    			
            UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
            
            int vehid=Integer.parseInt(JOptionPane.showInputDialog(new JFrame(),"Enter the Vehicle Id:").trim());            
            
            VehicleFrame vf=new VehicleFrame(vehid);
            vf.setTitle("Vehicle - "+vehid);
            vf.setVisible(true);
            vf.setResizable(false);
            vf.jLabel1.setText("Vehicle - "+vehid);
            
            VehicleReceiver vr=new VehicleReceiver(vf,vehid);
            vr.start();
	}
	catch (Exception ex)
	{            
            //System.out.println(ex);
	}   
    }
}
