package gov.nist.csd.pm.application.openoffice;

import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.NotConnectedException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XSeekable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static gov.nist.csd.pm.common.io.InputStreams.getAllFromInputStream;

/**
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class OOInputStream extends ByteArrayInputStream implements XInputStream,XSeekable
{
    public OOInputStream(InputStream stream){

        this(getAllFromInputStream(stream));
    }



    public OOInputStream(byte[] buf)
   {
      super(buf);
   }
   public void seek(long p1) throws IllegalArgumentException, com.sun.star.io.IOException 
   { 
      pos=(int)p1; 
   } 
   public long getPosition() throws com.sun.star.io.IOException 
   { 
      return pos; 
   } 
   public long getLength() throws com.sun.star.io.IOException 
   { 
      return count; 
   } 
   public int readBytes(byte[][] p1, int p2) throws NotConnectedException, BufferSizeExceededException, com.sun.star.io.IOException 
   { 
      try 
      { 
         byte[] b=new byte[p2]; 
         int res=super.read(b); 
         if(res>0) 
         { 
            if(res<p2) 
            { 
               byte[] b2=new byte[res]; 
               System.arraycopy(b,0,b2,0,res); 
               b=b2; 
            } 
         } 
         else 
         { 
            b=new byte[0]; 
            res=0; 
         } 
         p1[0]=b; 
         return res; 
      } 
      catch (java.io.IOException e) {throw new com.sun.star.io.IOException(e.getMessage(),this);} 
   } 
   public int readSomeBytes(byte[][] p1, int p2) throws NotConnectedException, BufferSizeExceededException, com.sun.star.io.IOException 
   { 
      return readBytes(p1,p2); 
   } 
   public void skipBytes(int p1) throws NotConnectedException, BufferSizeExceededException, com.sun.star.io.IOException 
   { 
      skip(p1); 
   } 
   public int available() 
   { 
      return super.available(); 
   } 
   public void closeInput() throws NotConnectedException, com.sun.star.io.IOException 
   { 
      try 
      { 
         close(); 
      } 
      catch (java.io.IOException e) {throw new com.sun.star.io.IOException(e.getMessage(),this);} 
   } 
} 
