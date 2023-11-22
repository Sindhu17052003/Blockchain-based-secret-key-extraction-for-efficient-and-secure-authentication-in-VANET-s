package trustedauthority;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import sun.misc.BASE64Encoder;

public class TrustedReceiver extends Thread{
    TrustedAuthorityFrame tf;
    ArrayList noreplocations=new ArrayList();
    ArrayList updatedPoolSize=new ArrayList();
    ArrayList VehicleDetails=new ArrayList();
    
    TrustedReceiver(TrustedAuthorityFrame f)
    {
        tf=f;
    }
    
    public void run()
    {
        try
        {
            DatagramSocket ds=new DatagramSocket(5000);
            while(true)
            {                
                byte data[]=new byte[10000];
                DatagramPacket dp=new DatagramPacket(data,0,data.length);
                ds.receive(dp);
                String str=new String(dp.getData()).trim(); 
                System.out.println("Received: "+str);
                String req[]=str.split("#");
                if(req[0].equals("Connect"))       
                {
                    DefaultTableModel dm=(DefaultTableModel)tf.jTable1.getModel();
                    Vector v=new Vector();
                    v.add(req[1].trim());
                    v.add(req[2].trim());
                    v.add(req[3].trim());
                    dm.addRow(v);
                    
                    if(!(noreplocations.contains(req[2].trim()+"@"+req[1].trim())))
                    {
                        noreplocations.add(req[2].trim()+"@"+req[1].trim());
                    }                                        
                }
                if(req[0].equals("Locations"))       
                {
                    String loca="";
                    for(int i=0;i<noreplocations.size();i++)
                    {
                        String loc=noreplocations.get(i).toString().trim();
                        loca=loca+loc.trim()+",";                        
                    }
                    if(!(loca.trim().equals("")))
                    {
                        String location=loca.substring(0,loca.lastIndexOf(','));
                        String msg="AvailableLocations#"+location.trim();
                        int pt=Integer.parseInt(req[1].trim())+7000;
                        packetTransmission(msg,pt);
                    }
                }
                if(req[0].equals("Register"))       
                {
                    String vehid=req[1].trim();
                    
                    int size =32;
                    Random rnd = new Random();
                    BigInteger p = BigInteger.probablePrime(size/2,rnd);
                    BigInteger q = p.nextProbablePrime();
                    BigInteger n = p.multiply(q);
                    BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
                    BigInteger e = getCoprime(m);
                    BigInteger d = e.modInverse(m);           
            
                    String pubkey = e.toString() + "," + n.toString();
                    String privkey = d.toString() + "," + n.toString();                    
                    String certificate = Certificate(vehid, pubkey);
                    String initialpseu=TrustedAuthorityFrame.RandomStringGenerator.generateRandomString(10,TrustedAuthorityFrame.RandomStringGenerator.Mode.ALPHANUMERIC);
                    
                    DefaultTableModel dm=(DefaultTableModel)tf.jTable3.getModel();
                    Vector v=new Vector();
                    v.add(vehid.trim());
                    v.add(pubkey.trim());
                    v.add(privkey.trim());
                    v.add(certificate.trim());
                    v.add(initialpseu.trim());
                    dm.addRow(v);
                                        
                    String msg="Keys#"+pubkey.trim()+"#"+privkey.trim()+"#"+certificate.trim()+"#"+initialpseu.trim();
                    int pt=Integer.parseInt(vehid.trim())+7000;
                    packetTransmission(msg,pt);
                }
                if(req[0].equals("Disconnect"))       
                {
                    String vehid=req[1].trim();
                    int cou=-1;
                    for(int i=0;i<tf.jTable2.getRowCount();i++)
                    {
                        String veid=tf.jTable2.getValueAt(i,0).toString().trim();
                        if(vehid.trim().equals(veid.trim()))
                        {
                            cou=i;
                        }
                    }
                    if(cou!=-1)
                    {
                        DefaultTableModel dm=(DefaultTableModel)tf.jTable2.getModel();
                        dm.removeRow(cou);
                    }
                }
                if(req[0].equals("VehicleDetails"))       
                {
                    String pseudonymset=req[1].trim();
                    String rsuid=req[2].trim();
                    String vehid=req[3].trim();
                    
                    VehicleDetails.add(pseudonymset+"#"+rsuid.trim()+"#"+vehid.trim());
                    
                    DefaultTableModel dm=(DefaultTableModel)tf.jTable2.getModel();
                    Vector v=new Vector();
                    v.add(pseudonymset.trim());
                    v.add(rsuid.trim());
                    dm.addRow(v);
                }
                if(req[0].equals("UpdatePoolSize"))       
                {
                    for(int i=0;i<tf.jTable1.getRowCount();i++)
                    {
                        String rsuid=tf.jTable1.getValueAt(i,0).toString().trim();
                        String rc="";
                        for(int k=0;k<3;k++)
                        {
                            String psu=TrustedAuthorityFrame.RandomStringGenerator.generateRandomString(10,TrustedAuthorityFrame.RandomStringGenerator.Mode.ALPHANUMERIC);
                            while(tf.norep.contains(psu.trim()))
                            {
                                psu=TrustedAuthorityFrame.RandomStringGenerator.generateRandomString(10,TrustedAuthorityFrame.RandomStringGenerator.Mode.ALPHANUMERIC);
                            }
                            tf.norep.add(psu.trim());
                            rc=rc+psu.trim()+"@";
                        }
                        String newpseudonymset=rc.substring(0,rc.lastIndexOf('@'));
                        updatedPoolSize.add(rsuid.trim()+"#"+newpseudonymset.trim());
                        String msg="NewPseudonymSets#"+newpseudonymset;
                        int pt=Integer.parseInt(rsuid.trim())+6000;
                        packetTransmission(msg,pt);
                    }
                    JOptionPane.showMessageDialog(tf,"All RSU's Pool Size are Updated & Distributed Successfully!");
                    tf.jTextArea1.setText("");
                    for(int i=0;i<tf.allRSU.size();i++)
                    {
                        String rsuid=tf.allRSU.get(i).toString().trim();
                        String pseudo=tf.allPseudonymsets.get(i).toString().trim();                        
                        tf.jTextArea1.append("=====================================\n         RSU - "+rsuid.trim()+"\n=====================================\n");                        
                        String ro=pseudo.trim()+"\n\n";
                        for(int j=0;j<updatedPoolSize.size();j++)
                        {
                            String sp[]=updatedPoolSize.get(j).toString().trim().split("#");
                            if(sp[0].trim().equals(rsuid.trim()))
                            {
                                ro=ro+sp[1].trim()+"\n\n";
                            }                            
                        }
                        String pseduonymset=ro.substring(0,ro.lastIndexOf('\n'));
                        tf.allPseudonymsets.set(i, pseduonymset.trim());
                        tf.jTextArea1.append(pseduonymset.trim().replaceAll("@","\n")+"\n\n");
                    }
                }
                if(req[0].equals("PseudonymRequest"))       
                {
                    System.out.println("VehicleDetails: "+VehicleDetails);
                    String vehid=req[1].trim();
                    String dvid=req[2].trim();
                    
                    for(int i=0;i<VehicleDetails.size();i++)
                    {
                        String sp[]=VehicleDetails.get(i).toString().trim().split("#");
                        System.out.println(dvid.trim()+"#"+sp[2].trim());
                        if(dvid.trim().equals(sp[2].trim()))
                        {
                            String pseudo=sp[0].trim();
                            String msg="DestinationPseudonym#"+pseudo;
                            int pt=Integer.parseInt(vehid.trim())+7000;
                            packetTransmission(msg,pt);
                            break;
                        }
                    }
                }
                if(req[0].equals("Communicate"))       
                {
                    String sourcevepseudo=req[1].trim();
                    String message=req[2].trim();
                    String destvehpseudo=req[3].trim();
                    
                    for(int i=0;i<VehicleDetails.size();i++)
                    {
                        String sp[]=VehicleDetails.get(i).toString().trim().split("#");
                        if(destvehpseudo.trim().equals(sp[0].trim()))
                        {
                            String rsuid=sp[1].trim();
                            String destvehid=sp[2].trim();
                            String msg="CommunicateReq#"+sourcevepseudo.trim()+"#"+message.trim()+"#"+destvehid.trim();
                            int pt=Integer.parseInt(rsuid.trim())+6000;
                            JOptionPane.showMessageDialog(tf,"Communicate Request has been forware to RSU - "+rsuid.trim()+" Successfully!");
                            packetTransmission(msg,pt);
                            break;
                        }
                    }
                }
                if(req[0].equals("Shuffling"))       
                {
                    ArrayList norep=new ArrayList();
                    for(int i=0;i<tf.allRSU.size();i++)
                    {
                        String rsuid=tf.allRSU.get(i).toString().trim();
                        int r=(int)(Math.random()*tf.allPseudonymsets.size());
                        while(norep.contains(r))
                        {
                            r=(int)(Math.random()*tf.allPseudonymsets.size());
                        }
                        norep.add(r);
                        String pseudo=tf.allPseudonymsets.get(r).toString().trim();
                        String msg="PseudonymSets#"+pseudo;
                        int pt=Integer.parseInt(rsuid.trim())+6000;
                        packetTransmission(msg,pt);
                    }
                    JOptionPane.showMessageDialog(tf,"Shuffled Successfully!");
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

    public static BigInteger getCoprime(BigInteger m) {
        Random rnd = new Random();
        int length = m.bitLength()-1;
        BigInteger e = BigInteger.probablePrime(length,rnd);
        while (! (m.gcd(e)).equals(BigInteger.ONE) ) {
      	 e = BigInteger.probablePrime(length,rnd);
        }
        return e;
    }

    private String Certificate(String vehid, String pubkey) throws Exception 
    {
        String key1 = pubkey;
	String key=convertFlexibleKeysize(key1);
	System.out.println("Key is "+key);      
	byte[] ciphertext = encrypt(key, vehid);
	  
            //Convert byte array to String
        BASE64Encoder encoder = new BASE64Encoder();
	String cipherstring = encoder.encode(ciphertext);
        return cipherstring;
    }

    public static String convertFlexibleKeysize(String key) throws Exception 
    {
	String validKey="";
	if(key.length()==16)
	{
            validKey=key;
	}
	else
	{			
            if(key.length()<16)
            {
		String te="1234567890123456";
		char ch[]=te.toCharArray();
		for(int i=key.length();i<ch.length;i++)
		{
                    key=key+ch[i];
		}
		validKey=key;
            }
            else
            {
		char ch1[]=key.toCharArray();
		String key1="";
		for(int i=0;i<16;i++)
		{
                    key1=key1+ch1[i];
		}
		validKey=key1;
            }
	}
	return validKey;
    }

    public static byte[] encrypt(String key, String value)
      throws GeneralSecurityException {

        byte[] raw = key.getBytes(Charset.forName("US-ASCII"));
        if (raw.length != 16) 
        {
            throw new IllegalArgumentException("Invalid key size.");
        }

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec,
            new IvParameterSpec(new byte[16]));
        return cipher.doFinal(value.getBytes(Charset.forName("US-ASCII")));
    }
}
