package module;

import GUI.DialogBox;

import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.math.BigInteger;
import java.security.MessageDigest;

public class DSA
{

    byte[] plainText;
    BigInteger p,q,h,g,x,y,k,r,s,w,u1,u2,v,pm1,km1;
    BigInteger[] sign;
    MessageDigest digest;
    int keyLen=512; //ta wartość daje długość p=512
    int ilZnHex=keyLen/4;//ilość znaków hex wyświetlanych w polu klucza
    Random random=new Random();


    public void generateKey() throws NoSuchAlgorithmException {
        //tworzymy losową liczbę bitów dla p
        int rand = 512 + (int)random.nextFloat() * 512;
        //następnie musimy ją dobić tak aby była wielokrotnością 64
        while (true) {
            if (rand % 64 == 0) {
                break;
            } else {
                rand++;
            }
        }
        keyLen = rand;
        q = BigInteger.probablePrime(160,new Random());
        BigInteger pom1, pom2;
        do {
            pom1 = BigInteger.probablePrime(keyLen,new Random());
            pom2 = pom1.subtract(BigInteger.ONE);
            pom1 = pom1.subtract(pom2.remainder(q));
        } while (!pom1.isProbablePrime(2));
        p = pom1;
        pm1 = p.subtract(BigInteger.ONE);
        h = new BigInteger(keyLen-2,random);
        while(true) {
            if (h.modPow(pm1.divide(q),p).compareTo(BigInteger.ONE) == 1) {
                break;
            } else {
                h = new BigInteger(keyLen-2,random);
            }
        }
        g = h.modPow(pm1.divide(q),p);
        do {
            x=new BigInteger(160-2,random);
        } while (x.compareTo(BigInteger.ZERO) != 1);
        y = g.modPow(x,p);

        digest = MessageDigest.getInstance("SHA-256");
    }


    public BigInteger[] podpisuj(byte[] tekst) {
        k = new BigInteger(160-2,random);
        r = g.modPow(k, p).mod(q);
        km1 = k.modInverse(q);

        digest.update(tekst);
        BigInteger hash = new BigInteger(1, digest.digest());
        BigInteger pom = hash.add(x.multiply(r));
        s = km1.multiply(pom).mod(q);
        BigInteger podpis[] = new BigInteger[2];
        podpis[0] = r;
        podpis[1] = s;
        return podpis;
    }

    public BigInteger[] podpisuj(String tekst)
    {
        digest.update(tekst.getBytes());
        k = new BigInteger(160-2,random);
        r = g.modPow(k, p).mod(q);
        km1 = k.modInverse(q);

        BigInteger hash = new BigInteger(1, digest.digest());
        BigInteger pom = hash.add(x.multiply(r));
        s = km1.multiply(pom).mod(q);
        BigInteger podpis[] = new BigInteger[2];
        podpis[0] = r;
        podpis[1] = s;
        return podpis;
    }


    public boolean weryfikujBigInt(byte[] tekstJawny, BigInteger[] podpis)
    {
        digest.update(tekstJawny);
        BigInteger hash = new BigInteger(1, digest.digest());
        w = podpis[1].modInverse(q);
        u1 = hash.multiply(w).mod(q);
        u2 = podpis[0].multiply(w).mod(q);
        v = g.modPow(u1, p).multiply(y.modPow(u2, p)).mod(p).mod(q);
        if(v.compareTo(podpis[0]) == 0) {
            return true;
        } else {
            return false;
        }
    }


    //zakładamy, że podpis jest w postaci hexadecymalnych znaków
    public boolean weryfikujString(String tekstJawny, String podpis) {
        digest.update(tekstJawny.getBytes());
        BigInteger hash = new BigInteger(1, digest.digest());
        String tab[] = podpis.split("\n");
        BigInteger r1 = new BigInteger(1,hexToBytes(tab[0]));
        BigInteger s1 = new BigInteger(1,hexToBytes(tab[1]));
        w=s1.modInverse(q);
        u1=hash.multiply(w).mod(q);
        u2=r1.multiply(w).mod(q);
        v=g.modPow(u1, p).multiply(y.modPow(u2, p)).mod(p).mod(q);
        if(v.compareTo(r1)==0) {
            return true;
        } else {
            return false;
        }
    }

    public static String bytesToHex(byte bytes[])
    {
        byte rawData[] = bytes;
        StringBuilder hexText = new StringBuilder();
        String initialHex = null;
        int initHexLength = 0;

        for (int i = 0; i < rawData.length; i++)
        {
            int positiveValue = rawData[i] & 0x000000FF;
            initialHex = Integer.toHexString(positiveValue);
            initHexLength = initialHex.length();
            while (initHexLength++ < 2)
            {
                hexText.append("0");
            }
            hexText.append(initialHex);
        }
        return hexText.toString();
    }

    //konwertuje ciąg znaków w systemie heksadecymalnym na tablicę bajtów
    public static byte[] hexToBytes(String tekst)
    {
        if (tekst == null) {
            return null;
        } else if (tekst.length() < 2) {
            return null;
        } else {
            if (tekst.length() % 2 != 0) tekst += '0';
            int dl = tekst.length() / 2;
            byte[] wynik = new byte[dl];
            for (int i = 0; i < dl; i++) {
                try {
                    wynik[i] = (byte) Integer.parseInt(tekst.substring(i * 2, i * 2 + 2), 16);
                } catch (NumberFormatException e) {
                    DialogBox.dialogAboutError("Cant convert Hex to Byte");
                }
                return wynik;
            }
        }
        return null;
    }

    public BigInteger[] getSign() {
        return sign;
    }

    public void setSign(BigInteger[] sign) {
        this.sign = sign;
    }

    public byte[] getPlainText() {
        return plainText;
    }

    public void setPlainText(byte[] plainText) {
        this.plainText = plainText;
    }
}