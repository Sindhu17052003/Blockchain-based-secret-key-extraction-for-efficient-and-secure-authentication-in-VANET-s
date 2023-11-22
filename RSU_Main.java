package rsu;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Main 
{
    public static void main(String[] args) 
    {        
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        try
        {                    			
            UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");                                                            
            
            int rsuid=Integer.parseInt(JOptionPane.showInputDialog(new JFrame(),"Enter the RSU Id:").trim()); 
            String location=JOptionPane.showInputDialog(new JFrame(),"Enter the Location: ");
            
            RSUFrame rf=new RSUFrame(rsuid,location);
            rf.setTitle("RSU - "+rsuid);
            rf.setVisible(true);
            rf.setResizable(false);
            rf.jLabel1.setText("RSU - "+rsuid);
            
            RSUReceiver rr=new RSUReceiver(rf,rsuid);
            rr.start();
	}
	catch (Exception ex)
	{            
            //System.out.println(ex);
	}        
    }
}
