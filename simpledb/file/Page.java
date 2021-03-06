package simpledb.file;

import simpledb.server.SimpleDB;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * The contents of a disk block in memory.
 * A page is treated as an array of BLOCK_SIZE bytes.
 * There are methods to get/set values into this array,
 * and to read/write the contents of this array to a disk block.
 * 
 * For an example of how to use Page and 
 * {@link Block} objects, 
 * consider the following code fragment.  
 * The first portion increments the integer at offset 792 of block 6 of file junk.  
 * The second portion stores the string "hello" at offset 20 of a page, 
 * and then appends it to a new block of the file.  
 * It then reads that block into another page 
 * and extracts the value "hello" into variable s.
 * <pre>
 * Page p1 = new Page();
 * Block blk = new Block("junk", 6);
 * p1.read(blk);
 * int n = p1.getInt(792);
 * p1.setInt(792, n+1);
 * p1.write(blk);
 *
 * Page p2 = new Page();
 * p2.setString(20, "hello");
 * blk = p2.append("junk");
 * Page p3 = new Page();
 * p3.read(blk);
 * String s = p3.getString(20);
 * </pre>
 * @author Edward Sciore
 */
public class Page {
   /**
    * The number of bytes in a block.
    * This value is set unreasonably low, so that it is easier
    * to create and test databases having a lot of blocks.
    * A more realistic value would be 4K.
    */
   public static final int BLOCK_SIZE = 400;
   
   /**
    * The size of an integer in bytes.
    * This value is almost certainly 4, but it is
    * a good idea to encode this value as a constant. 
    */
   public static final int INT_SIZE = Integer.SIZE / Byte.SIZE;
   
   /**
    * The maximum size, in bytes, of a string of length n.
    * A string is represented as the encoding of its characters,
    * preceded by an integer denoting the number of bytes in this encoding.
    * If the JVM uses the US-ASCII encoding, then each char
    * is stored in one byte, so a string of n characters
    * has a size of 4+n bytes.
    * @param n the size of the string
    * @return the maximum number of bytes required to store a string of size n
    */
   public static final int STR_SIZE(int n) {
      float bytesPerChar = Charset.defaultCharset().newEncoder().maxBytesPerChar();
      return INT_SIZE + (n * (int)bytesPerChar);
   }
   
   private ByteBuffer contents = ByteBuffer.allocateDirect(BLOCK_SIZE);
   private FileMgr filemgr = SimpleDB.fileMgr();
   
   /**
    * Creates a new page.  Although the constructor takes no arguments,
    * it depends on a {@link FileMgr} object that it gets from the
    * method {@link simpledb.server.SimpleDB#fileMgr()}.
    * That object is created during system initialization.
    * Thus this constructor cannot be called until either
    * {@link simpledb.server.SimpleDB#init(String)} or
    * {@link simpledb.server.SimpleDB#initFileMgr(String)} or
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * {@link simpledb.server.SimpleDB#initFileLogAndBufferMgr(String)}
    * is called first.
    */
   public Page() {}
   
   /**
    * Populates the page with the contents of the specified disk block. 
    * @param blk a reference to a disk block
    */
   public synchronized void read(Block blk) {
      filemgr.read(blk, contents);
   }
   
   /**
    * Writes the contents of the page to the specified disk block.
    * @param blk a reference to a disk block
    */
   public synchronized void write(Block blk) {
      filemgr.write(blk, contents);
   }
   
   /**
    * Appends the contents of the page to the specified file.
    * @param filename the name of the file
    * @return the reference to the newly-created disk block
    */
   public synchronized Block append(String filename) {
      return filemgr.append(filename, contents);
   }
   
   /**
    * Returns the integer value at a specified offset of the page.
    * If an integer was not stored at that location, 
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the integer value at that offset
    */
   public synchronized int getInt(int offset) {
      contents.position(offset);
      return contents.getInt();
   }
   
   /**
    * Writes an integer to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the integer to be written to the page
    */
   public synchronized void setInt(int offset, int val) {
      contents.position(offset);
      contents.putInt(val);
   }
   
   /**
    * Returns the string value at the specified offset of the page.
    * If a string was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the string value at that offset
    */
   public synchronized String getString(int offset) {
      contents.position(offset);
      int len = contents.getInt();
      byte[] byteval = new byte[len];
      contents.get(byteval);
      return new String(byteval);
   }
   
   /**
    * Writes a string to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the string to be written to the page
    */
   public synchronized void setString(int offset, String val) {
      contents.position(offset);
      byte[] byteval = val.getBytes();
      contents.putInt(byteval.length);
      contents.put(byteval);
   }
   
   /*  ------------------   The following methods were added   ------------------------ */
   /*  The methods were implemented in a similar manner as getString() and setString()  */ 
   /*  Similar to getString(), it is assumed that a value has been set at a particular
    *  offset before we get it from that offset   	       ------------------------ */
   
   /**
    * Returns the short value at the specified offset of the page.
    * If a short was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the short value at that offset
    */
   public synchronized short getShort(int offset) {
	  // Set the position of contents to the offset passed 
	  contents.position(offset);
	  // Get the short value present at the offset
	  return contents.getShort();
   }

   /**
    * Writes a short to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the short to be written to the page
    */
   public synchronized void setShort(int offset, short val) {
	  // Set the position of contents to the offset passed  
	  contents.position(offset);
	  // Put the short value in contents
	  contents.putShort(val);
   }
   
   /**
    * Returns the boolean value at the specified offset of the page.
    * If a boolean was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the boolean value at that offset
    */
   public synchronized boolean getBoolean(int offset) {
	  // Set the position of contents to the offset passed 
	  contents.position(offset);
	  // 1 represents true, if 1 was set return true else false
	  return contents.getInt()==1;
   }
   
   /**
    * Writes a boolean to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the boolean to be written to the page
    */
   public synchronized void setBoolean(int offset, boolean val) {
	  // Set the position of contents to the offset passed
	  contents.position(offset);
	  // Set 1 if the value passed is true or else set 0
	  // This is done since we cannot directly put boolean values in contents
	  contents.putInt(val?1:0);
   }
   
   /**
    * Returns the byte array value at the specified offset of the page.
    * If a byte array was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the byte array value at that offset
    */
   public synchronized byte[] getBytes(int offset) {
	  // Set the position of contents to the offset passed  
	  contents.position(offset);
	  // Get the integer value which represents the length of the byte array
	  int len = contents.getInt();
	  // Create the byte array
	  byte[] byteval = new byte[len];
	  // Set the created byte array with the value obtained from contents
	  contents.get(byteval);
	  return byteval;
   }
   
   /**
    * Writes a byte array to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the byte array to be written to the page
    */
   public synchronized void setBytes(int offset, byte[] val) {
	  // Set the position of contents to the offset passed 
	  contents.position(offset);
	  // Put  the length of the byte array
	  contents.putInt(val.length);
	  // Put the byte array
	  contents.put(val);
   }
   
   /**
    * Returns the date value at the specified offset of the page.
    * If a date was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the date value at that offset
    */
   public synchronized Date getDate(int offset) {
	  // Set the position of contents to the offset passed 
	  contents.position(offset);
	  // Read the contents which is stored as a long number of milliseconds and convert to date 
	  return new Date(contents.getLong());
   }
   
   /**
    * Writes a date to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the date to be written to the page
    */
   public synchronized void setDate(int offset, Date val) {
	  // Set the position of contents to the offset passed 
	  contents.position(offset);
	  // The getTime() returns the number of milliseconds since January 1, 1970, 00:00:00 GMT 
	  // represented by the Date object and this value is put in contents
	  contents.putLong(val.getTime());
   }
}
