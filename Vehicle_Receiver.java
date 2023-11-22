
package vehicle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class VehicleReceiver extends Thread{
    
    VehicleFrame vf;
    int vehid,port;
    
    public static ArrayList loc=new ArrayList();
    public static ArrayList rsus=new ArrayList();
    int count=0;
    
    VehicleReceiver(VehicleFrame f,int vid)
    {
        vf=f;
        vehid=vid;
        port=vehid+7000;
    }
    
    public void run()
    {
        try
        {
            DatagramSocket ds=new DatagramSocket(port);
            System.out.println("port is: "+port);
            if(count==0)
            {
                getLocations();
                count=1;
            }
            while(true)
            {                
                byte data[]=new byte[10000];
                DatagramPacket dp=new DatagramPacket(data,0,data.length);
                ds.receive(dp);
                String str=new String(dp.getData()).trim(); 
                System.out.println("Received: "+str);
                String req[]=str.split("#");                
                if(req[0].equals("AvailableLocations"))       
                {
                    String locations=req[1].trim();
                    if(locations.trim().contains(","))
                    {
                        String sp[]=locations.trim().split(",");
                        for(int i=0;i<sp.length;i++)
                        {
                            String spk[]=sp[i].trim().split("@");
                            loc.add(spk[0].trim());
                            rsus.add(spk[1].trim());
                            vf.jComboBox1.addItem(spk[0].trim());                            
                        }
                    }
                    else
                    {
                        String spk[]=locations.trim().split("@");
                        loc.add(spk[0].trim());
                        rsus.add(spk[1].trim());
                        vf.jComboBox1.addItem(spk[0].trim());
                    }
                }
                if(req[0].equals("Keys"))       
                {
                    vf.jTextField2.setText(req[1].trim());
                    vf.jTextField3.setText(req[2].trim());
                    vf.jTextField4.setText(req[3].trim());
                    vf.jTextField5.setText(req[4].trim());
                }
                if(req[0].equals("PseudonymSet"))       
                {
                    JOptionPane.showMessageDialog(vf,"Encrypted Pseudonym Set has been received Successfully!");
                    vf.jTextArea1.setText(req[1].trim());                    
                }
                if(req[0].equals("DestinationPseudonym"))       
                {
                    vf.jTextField7.setText(req[1].trim());
                }
                if(req[0].equals("CommunicateReq"))       
                {
                    JOptionPane.showMessageDialog(vf,"Communicate Request has been received Successfully!");
                    String sourcepseudo=req[1].trim();
                    String message=req[2].trim();
                    
                    DefaultTableModel dm=(DefaultTableModel)vf.jTable1.getModel();
                    Vector v=new Vector();
                    v.add(sourcepseudo.trim());
                    v.add(message.trim());
                    dm.addRow(v);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }    

    public final void getLocations()
    {
        String msg="Locations#"+vehid;
        int pt=5000;
        packetTransmission(msg,pt);
    }

    private void packetTransmission(String msg, int pt) {
        try
        {
            byte data1[]=msg.getBytes();
            DatagramSocket ds1=new DatagramSocket();
            DatagramPacket dp1=new DatagramPacket(data1,0,data1.length,InetAddress.getByName("127.0.0.1"),pt);
            ds1.send(dp1);
            System.out.println("Port is "+pt+"\n");                        
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }   
    }
}
