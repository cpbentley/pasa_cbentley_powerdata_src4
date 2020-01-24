package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerIntArrayOrdered;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntPowerArray;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * An efficient structure for patches of adjacent integer values. Thus values stored unique and ordered (ASC or DESC).
 * <br>
 * <br>
 * This class does <b>not</b> do key-value mapping. For this behavior use {@link PowerOrderedIntToIntRun}.
 * <br>
 * <br>
 * 
 * Only useful header byte overhead that takes 4 + 10 bytes is overcome. This it is efficient to store big intervals like 5-25 and 50-150.
 * <br>
 * 14 + 4 bytes = 18 instead of 120 bytes. Not so bad.
 * <br>
 * <br>
 * 
 * <li>stores from 0 to 255 in ordered value
 * <li>Stores integers in continuity frames.
 * <br>
 * <br>
 * With Small bytes (this information is given by the constructor) we have [3][4] and [4][123] coding for 4,5,6,123,124,125,126.
 * <br>
 * That's 4 bytes instead of 7.
 * <br>
 * <br>
 * @author Charles Bentley
 *
 */
public class PowerIntArrayRun extends ByteObjectManaged implements IPowerIntArrayOrdered, ITechIntPowerArray {

   private PDCtx pdc;

   /**
    * Store biggest integer first DESC
    * smallest integer first ASC
    */

   public PowerIntArrayRun(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerIntArrayRunRoot());
   }

   /**
    * The Tech reference array is used. CAREFULL!
    * <br>
    * <br>
    * 
    * @param mod
    * @param tech
    */
   public PowerIntArrayRun(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc.getBoc(), tech);
      this.pdc = pdc;
   }

   /**
    * Add value to the bag
    * Does not add values lower than -254 and higher than 255;
    * <br>
    * <br>
    * @param value the integer, only the first 8 bits will be stored
    */
   public void addInt(int value) {
      int index = getDataOffsetStartLoaded();
      value = (byte) value; // only 8 bits
      //after reading the chunk header, start coordinate is ready to read the first frame
      int position = index;
      int[] frame = new int[2];
      int[] nextFrame = new int[2];
      int numElements = get4(ARRAY_OFFSET_05_SIZE4);
      //initial case where the chunk is empty without values (just the length size header
      if (numElements == 0) {
         insertLone(value, position);
         incrementNoVersion(ARRAY_OFFSET_05_SIZE4, 4, 1);
         increaseVersionCount();
         return;
      }
      int end = getDataOffsetEndLoaded();
      //loop
      // read frame (frame is the pair (length of frame,start value of frame)
      // start frame check
      // end frame check
      // check if insertion point
      // next frame while current coordinate is smaller than next chunk coordinate
      while (position < end) {
         // read frame

         // frame reading must ensure it does not get on the next chunk
         readNextFrame(frame, position);
         boolean isLastFrame = false;
         //now check if this is the lastframe
         if (position + 2 >= end) {
            //case where a new lone frame has to be inserted
            isLastFrame = true;
         } else {
            // check next frame start for potential bridging in the frame check end
            readNextFrame(nextFrame, position + 2);
         }
         if (isInsideFrame(value, frame)) {
            //found so we leave because there is nothing to add
            return;
         }
         //end read frame

         // check start of frame
         if (checkValueFrameStart(value, frame[1])) {
            //normal extension for [x,y]
            insertNormalExtension(value, position, frame);
            break;
         }

         // check end of frame
         if (checkValueFrameEnd(value, getLast(frame))) {
            // go back to frame start in order to write the new data
            //if last frame.. no check. careful. checkNext -1 = 0-1
            if (!isLastFrame && checkNext(value, nextFrame[1])) {
               //first update the lower frame
               // +1 because len of 0 for the next Frame means one element
               int newlen = frame[0] + nextFrame[0] + 1;
               //bridges the gap. the new length both frames + 1 for the bridge
               data[position] = (byte) newlen;
               // no need to change the first value as it hasn't changed
               //structural change: create new array, 2 bytes smaller
               //modifies the data
               expandDataArray(-2, position + 2);
               //quits the while loop
               break;
            } else {
               //normal extension 
               //check flag bit if we are using it
               // insert new frame length
               data[position] = (byte) (frame[0] + 1);
               // the frame start does not change because we are right now at the end
               break;
            }
         }
         //check insertion point new chunk before first Chunk

         if (checkInsertBeforeNext(value, frame[1])) {
            insertLone(value, position);
            break;
         }
         if (isLastFrame) {
            //last Frame and non of the above. create  a new frame.
            insertLone(value, position + 2);
            break;
         }
         //next frame
         position += 2;
      }
      incrementNoVersion(ARRAY_OFFSET_05_SIZE4, 4, 1);
      increaseVersionCount();
   }

   public void addValue(int value) {
      addInt(value);
   }

   public boolean cancelValue(int value, int[] frame) {
      if (value < frame[1])
         return false;
      return true;
   }

   public boolean checkInsertBeforeNext(int value, int next) {
      return (value < next);
   }

   public boolean checkNext(int value, int next) {
      return (value == next - 1);
   }

   /**
    * Call this at the end of a frame check
    * @param value
    * @param last
    * @return
    */
   public boolean checkValueFrameEnd(int value, int last) {
      return (value == last + 1);
   }

   /**
    * Call this method at the begining of a frame check
    * @param value
    * @param start
    * @return
    */
   public boolean checkValueFrameStart(int value, int start) {
      return (value == start - 1);
   }

   /**
    * 
    */
   public int[] getInts() {
      int index = getDataOffsetStartLoaded();
      int end = getDataOffsetEndLoaded();
      int size = 0;
      for (int i = index; i < end; i += 2) {
         size += (data[i] & 0xFF);
      }
      int[] ar = new int[size];
      int count = 0;
      for (int i = index; i < end; i += 2) {
         int num = data[i] & 0xFF;
         for (int j = 0; j < num; j++) {
            ar[count] = data[i + 1] + j;
            count++;
         }
      }
      return ar;
   }

   /**
    * Return the last value in the frame
    * @param frame
    * @return
    */
   public int getLast(int[] frame) {
      return frame[1] + frame[0] - 1;
   }

   /**
    * 
    */
   public Object getMorph(MorphParams p) {
      return this;
   }

   /**
    * Maps [value] to position for the 0-255 values.
    * <br>
    * <br>
    * replaces the getValuePosition method
    * -1 if no position for value
    * @return
    */
   public int[] getPositionMapping() {
      int[] map = new int[256];
      map[0] = -1;
      for (int i = 1; i < map.length; i++) {
         int val = getValueFromPosition(i);
         if (val == Integer.MAX_VALUE)
            break;
         map[val & 0xFF] = i;
      }
      //	for (int i = 0; i < map.length; i++) {
      //	   if(map[i] == 0)
      //		map[i] = -1;
      //	}
      return map;
   }

   public int getSize() {
      int size = 0;
      int end = getDataOffsetEndLoaded();
      int index = getDataOffsetStartLoaded();
      for (int i = index; i < end; i += 2) {
         size += (data[i] & 0xFF);
      }
      return size;
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   /**
    * Return the value stored at position
    * <br>
    * <br>
    * @param position
    * @return Integer.MAX_VALUE if position is out of range
    */
   public int getValueFromPosition(int position) {
      int count = 1;
      int end = getDataOffsetEndLoaded();
      //iterate over data
      int index = getDataOffsetStartLoaded();
      for (int i = index; i < end; i += 2) {
         int num = (data[i] & 0xFF);
         for (int j = 0; j < num; j++) {
            if (count == position)
               return data[i + 1] + j;
            count++;
         }
      }
      return Integer.MAX_VALUE;
   }

   public int getValueOrderCount(int value) {
      return getValuePosition(value);
   }

   /**
    * Return the position of the value in the integer series
    * <br>
    * <br>
    * @param value
    * @return -1 if value cannot be found
    */
   public int getValuePosition(int value) {
      byte val = (byte) value;
      int count = 1;
      // for all chunks
      int end = getDataOffsetEndLoaded();
      int index = getDataOffsetStartLoaded();
      for (int i = index; i < end; i += 2) {
         //for all values in the chunk
         for (int j = 0; j < (data[i] & 0xFF); j++) {
            if (val == data[i + 1] + j) {
               return count;
            }
            count++;
         }
      }
      return Integer.MAX_VALUE;
   }

   public int[] getValues() {
      return getInts();
   }

   /**
    * Is the value in the bag
    * @param value
    * @return
    */
   public boolean hasValue(int value) {
      return getValuePosition(value) != -1;
   }

   /**
    * Insert a value alone without any adjacent values.
    * <br>
    * <br>
    * 
    * @param value
    * @param position
    */
   private void insertLone(int value, int position) {
      expandDataArray(2, position);
      data[position] = 1;
      data[position + 1] = (byte) value;
   }

   public void insertNormalExtension(int value, int position, int[] frame) {
      //come back at start of the frame
      data[position] = (byte) (frame[0] + 1);
      // the new value becomes the new frame start because it is smaller than the previous one
      data[position + 1] = (byte) value;
   }

   public boolean isInsideFrame(int value, int[] frame) {
      if (value >= frame[1] && value <= getLast(frame)) {
         return true;
      }
      return false;
   }

   private void readNextFrame(int[] result, int position) {
      result[0] = data[position] & 0xFF;
      result[1] = data[position + 1];
   }

   public int remove(int value) {
      return 0;
   }

   /**
    * 
    */
   public byte[] serializePack() {
      return super.toByteArray();
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return null;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerIntArrayRun");
      toStringPrivate(dc);
      super.toString(dc.sup());

   }

   private void toStringPrivate(Dctx dc) {
      dc.append("#IntPowerArrayRun #numElements=" + get4(ARRAY_OFFSET_05_SIZE4));
      int count = 0;
      int index = getDataOffsetStartLoaded();
      int end = getDataOffsetEndLoaded();
      dc.nl();
      dc.append("DataStart=" + index + " DataEnd=" + end);
      for (int i = index; i < end; i += 2) {
         if (i + 1 >= data.length) {
            dc.nl();
            dc.append("ArrayOutOfBoundException for Index=" + i);
            break;
         }
         dc.nl();
         int num = (data[i] & 0xFF);
         dc.append(num + " = ");
         for (int j = 0; j < num; j++) {
            dc.append(" " + (data[i + 1] + j));
            count++;
         }
      }
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerIntArrayRun");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

}
