
package rsu;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class RSUReceiver extends Thread
{
    
    RSUFrame rf;
    int rsuid,port;
    ArrayList norep=new ArrayList();
    ArrayList allPseudonymSets=new ArrayList();
    int cou=0;
    String vehid,pseudonym,publickey;
    
    RSUReceiver(RSUFrame f, int rid)
    {
        rf=f;
        rsuid=rid;
        port=rsuid+6000;
    }
    
    public void run()
    {
        try
        {
            DatagramSocket ds=new DatagramSocket(port);
            while(true)
            {                
                byte data[]=new byte[10000];
                DatagramPacket dp=new DatagramPacket(data,0,data.length);
                ds.receive(dp);
                String str=new String(dp.getData()).trim(); 
                System.out.println("Received: "+str);
                String req[]=str.split("#");
                if(req[0].equals("ConnectwithRSU"))       
                {
                    vehid=req[1].trim();
                    pseudonym=req[2].trim();
                    publickey=req[3].trim();
                    
                    DefaultTableModel dm=(DefaultTableModel)rf.jTable1.getModel();
                    Vector v=new Vector();
                    v.add(pseudonym.trim());
                    v.add(publickey.trim());
                    dm.addRow(v);                                        
                    
                    rf.currentflowrate++;
                    rf.jTextField1.setText(""+rf.currentflowrate);
                    
                    if(rf.currentflowrate>rf.poolsize)
                    {
                        String msg="UpdatePoolSize#"+"#"+rsuid;
                        int pt=5000;
                        JOptionPane.showMessageDialog(rf,"Current Flow Rate is greater than Pool Size! So Pool Size Update Request has been sent to TA!");
                        packetTransmission(msg,pt);
                    }   
                    else
                    {                                                                    
                        String pseudonymset=allPseudonymSets.get(cou).toString().trim();
                        
                        String msg="VehicleDetails#"+pseudonymset.trim()+"#"+rsuid+"#"+vehid.trim();
                        int pt=5000;
                        packetTransmission(msg,pt);                                                
                        
                        String[] s5 = publickey.trim().split(",");
                        String e = s5[0];
                        String n = s5[1];                                    
                
                        String ce = "";
                        char ch[]=pseudonymset.trim().toCharArray();
                        for(int i=0;i<ch.length;i++)
                        {
                            char c=ch[i];                    
                            System.out.println((int)c);                    
                            BigInteger orig = BigInteger.valueOf((int)c);
                            String ci = orig.modPow(new BigInteger(e), new BigInteger(n)).toString();
                            ce = ce + ci + ",";
                        }
                        String cippseudonymset = ce.substring(0, ce.lastIndexOf(','));
                        JOptionPane.showMessageDialog(rf, "Entered Vehicle - "+pseudonym+" is Accepted & Pseudonym Set is Generated Successfully!");
                        JOptionPane.showMessageDialog(rf, "Pseudonym Set is Encrypted & Forward to Entered Vehicle - "+pseudonym+" Successfully!");
                        
                        String msg1="PseudonymSet#"+cippseudonymset.trim();
                        int pt1=Integer.parseInt(vehid.trim())+7000;
                        packetTransmission(msg1,pt1);
                        cou++;
                    }
                }
                if(req[0].equals("Disconnect"))       
                {
                    String vehpseuo=req[2].trim();
                    String pseudonymset=req[3].trim();
                    
                    int cou1=-1;
                    for(int i=0;i<rf.jTable1.getRowCount();i++)
                    {
                        String veid=rf.jTable1.getValueAt(i,0).toString().trim();
                        if(vehpseuo.trim().equals(veid.trim()))
                        {
                            cou1=i;
                        }
                    }
                    if(cou1!=-1)
                    {
                        DefaultTableModel dm=(DefaultTableModel)rf.jTable1.getModel();
                        dm.removeRow(cou1);
                    }
                    
                    String msg="Disconnect#"+pseudonymset.trim()+"#"+rsuid;
                    int pt=5000;
                    packetTransmission(msg,pt);
                }
                if(req[0].equals("PseudonymSets"))       
                {
                    allPseudonymSets=new ArrayList();
                    rf.jTextArea1.setText(req[1].trim().replaceAll("\n","\n\n").replaceAll("@","\n"));
                    String sp[]=req[1].trim().split("\n");
                    for(int i=0;i<sp.length;i++)
                    {
                        allPseudonymSets.add(sp[i].trim());
                    }
                    rf.poolsize=sp.length;
                    rf.jTextField2.setText(""+rf.poolsize);
                }
                if(req[0].equals("NewPseudonymSets"))       
                {
                    rf.jTextArea1.append("\n\n"+req[1].trim().replaceAll("@","\n"));
                    allPseudonymSets.add(req[1].trim());
                    rf.poolsize++;
                    rf.jTextField2.setText(""+rf.poolsize);
                    
                    String pseudonymset=allPseudonymSets.get(cou).toString().trim();
                    
                    String msg="VehicleDetails#"+pseudonymset.trim()+"#"+rsuid+"#"+vehid.trim();
                    int pt=5000;
                    packetTransmission(msg,pt);                                            
                    
                    String[] s5 = publickey.trim().split(",");
                    String e = s5[0];
                    String n = s5[1];                                    
                
                    String ce = "";
                    char ch[]=pseudonymset.trim().toCharArray();
                    for(int i=0;i<ch.length;i++)
                    {
                        char c=ch[i];                    
                        System.out.println((int)c);                    
                        BigInteger orig = BigInteger.valueOf((int)c);
                        String ci = orig.modPow(new BigInteger(e), new BigInteger(n)).toString();
                        ce = ce + ci + ",";
                    }
                    String cippseudonymset = ce.substring(0, ce.lastIndexOf(','));
                    JOptionPane.showMessageDialog(rf, "Pseudonyms Set is Encrypted & Forward to Entered Vehicle - "+vehid+" Successfully!");
                        
                    String msg1="PseudonymSet#"+cippseudonymset.trim();
                    int pt1=Integer.parseInt(vehid.trim())+7000;
                    packetTransmission(msg1,pt1);
                    cou++;
                }
                if(req[0].equals("Communicate"))       
                {
                    JOptionPane.showMessageDialog(rf,"Communicate Request has been received Successfully!");
                    String sourcepseudo=req[1].trim();
                    String message=req[2].trim();
                    String destpseudo=req[3].trim();
                    
                    String msg="Communicate#"+sourcepseudo.trim()+"#"+message.trim()+"#"+destpseudo.trim();
                    int pt=5000;                    
                    JOptionPane.showMessageDialog(rf,"Communicate Request has been forward to Trusted Authority Successfully!");
                    packetTransmission(msg,pt);
                }
                if(req[0].equals("CommunicateReq"))       
                {
                    JOptionPane.showMessageDialog(rf,"Communicate Request has been received Successfully!");
                    String sourcepseudo=req[1].trim();
                    String message=req[2].trim();
                    String destvehid=req[3].trim();
                    
                    String msg="CommunicateReq#"+sourcepseudo.trim()+"#"+message.trim();
                    int pt=Integer.parseInt(destvehid.trim())+7000;                    
                    JOptionPane.showMessageDialog(rf,"Communicate Request has been forward to Destination Vehicle - "+destvehid.trim()+" Successfully!");
                    packetTransmission(msg,pt);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
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
