package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;

/**
 * Request for a Pointer.
 * 
 * <li>supports reading
 * <li>supports writing and thus splitting blocks
 * @author Charles Bentley
 *
 */
public class HeaderLocate {

   public ByteObjectManaged bom;

   public int               headerSize;

   public int               offset;

   /**
    * Zero based pointer
    * @param pointer
    * @return
    */
   public int getPosition(int pointer) {
      int flag = bom.get1(offset);
      int valTop = bom.get4(offset);
      int valueBelow = bom.get4(offset + 4);

      if (pointer > valueBelow) {
         //go the right side
      }
      return 0;
   }
}
