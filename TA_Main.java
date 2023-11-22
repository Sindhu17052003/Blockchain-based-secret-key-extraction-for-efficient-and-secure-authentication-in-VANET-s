package trustedauthority;

import javax.swing.JDialog;
import javax.swing.JFrame;
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
            
            TrustedAuthorityFrame tf=new TrustedAuthorityFrame();
            tf.setTitle("Trusted Authority");
            tf.setVisible(true);
            tf.setResizable(false);  
            
            TrustedReceiver tr=new TrustedReceiver(tf);
            tr.start();
	}
	catch (Exception ex)
	{            
         
	}        
    }
}
