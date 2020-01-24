package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntToInts;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerIntArrayOrderToInt;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkOrderedIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechOrderedIntToIntRun;
import pasa.cbentley.powerdata.src4.base.PowerRunBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * A memory efficient ordered sequence of positive integers (values) for {@link IPowerLinkOrderedIntToInt}
 * <br>
 * In effect a int[][] with data and int[] without data
 * <br>
 * The idea is to store 6 7 8 and 9 as 6-4. This is called patching.
 * <br>
 * <br>
 * 
 * Starting from 7 bit long values, only the useful bits are stored. For 1-6 bit long values, the 7 size is used.
 * The association is an option turned off when data bit size is 0. The structure is then a simple list.
 * <br>
 * <br>
 * See {@link ITechOrderedIntToIntRun} for the specification.
 * <br>
 * <br>
 * <b>Efficient</b>:
 * <li> when it needs to store lots of big intervals 5-60 70-90. This stores 75 values with a 2 chunks. 76 bytes against
 * <br>
 * <br>
 * 
 * <b>Not efficient </b>
 * <li>Lots of small values 1  3 6 8 16 85 gives a lot of overhead unless the bit flag removes patching.
 * <br>
 * <br>
 * The table option makes the table an array where the integer (value) is stored alongside its data of a given bit size.
 * <br>
 * <br>
 *  Automatic expansion of data bitsize is an option
 * 
 * The flagbit use allows to save a few bits for configurations with lots of lone values and few big frames
 * <br>
 * <br>
 * 
 * if true, one flag bit of 0 means a lone integer. a flag bit of 1 means that a length
 * field is following and has to be read
 * <br>
 * 
 * Flag bit is memory efficient if X (lone values) Y (blocks), L length bitsize
 * <br>
 * 
 * equations are for the bit costs
 * without flagbit = L * (X + Y) = bitsused
 * <br>
 * with flag bit = (L+1 * Y) + X = bitsused
 * <br>
 * Flag bit can be used on a chunk basis
 * <br>
 * <br>
 * 
 * <b>BASE HEADER</b>
 * <li>[base Chunk 1 byte] 
 * <li>[order 1 byte]
 * <li>[databit size 1 byte]
 * <li>[auto databitsize 1 byte] (0 = no datas, 1=auto, 2=set 3 buffer known
 * <li>[flabit use 1byte] (1 = use 0 = never use) 
 * <li>[ # of values stored 4 bytes ]
 * <li>[X # of lones frames stored 3 bytes] {@link ITechOrderedIntToIntRun#OITI_RUN_OFFSET_06_TOTAL_LONERS3}
 * <li>[Y # of big frames stored 3 bytes] 
 * <li>[lastusedbit 4 bytes]
 * <li>[offset sublcas 4 bytes]
 * <br>
 * <br>
 * 
 * <b>Chunks</b>
 * <br>
 * <br>
 * 
 * Each chunk has a header:
 * <li>chunk 0   [datas offset] [ dataBitSize  1 byte] [3 empty bytes] 0 chunk describes the data located at the end
 * <li>chunk 1-7 [values offset in byte array = bits (4 bits + byte postion (19 bits)] [lengthbitsize 1byte] [flagbit 1byte] [#X 3 bytes] [#Y 3 Bytes] [#num values below 3 bytes]
 * <li>chunk 8   [values offset 4 bytes] [lengthbitsize 1byte] [flagbit 1byte] [#X 3 bytes] [#Y 3 Bytes] [#num values below 3 bytes]
 * <br>
 * Negative Chunk
 * <br>
 * 
 * <b>Byte Data</b>
 * 
 * <li>[flagbit 1bit] [frame length chunkLengthBitSize] [frame startvalue 7bits]
 * <li>[flagbit 1bit] [frame length chunkLengthBitSize] [frame startvalue 8bits]
 * 
 * <br>
 * <br>
 * The flagbit is 1 for loners and 0 for big frames
 * <br>
 * <br>
 * 
 * [data are stored sequentially in the same order as the values]
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class PowerOrderedIntToIntRun extends PowerRunBase implements IPowerIntArrayOrderToInt, ITechOrderedIntToIntRun {



   /**
    * The size of the databits stored stored as a simple array of consecutive datas.
    * <br>
    * 0 when there isn't any data
    */
   private int dataBitSize;

   private int DEFAULTLengthSize        = 1;

   /**
    * 
    */
   private int defDataBitSize;

   /**
    * Number of bits to ignite a change between flagbit ON or OFF
    */
   public int  FLAGBIT_CHANGE_THRESHOLD = 8;

   /**
    * NOT LOADED
    * the last bit of relevant information
    * relative to offset.
    * there is a method to load it with offset value
    * <br>
    * @volatile
    */
   private int lastusedbit;

   /**
    * Create a Sequence with no datas. only values
    *
    */
   public PowerOrderedIntToIntRun(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerOrderedIntToIntRunRoot());
   }

   /**
    * Use this contructor when you know the boundary of the data domain
    * sets flag for buffer known
    * @param dataBitSize
    * @param dataBuffer
    */
   /**
    * 
    * @param dataBitSize 0 if table options is OFF, size of 1 means data is automatic
    */
   public PowerOrderedIntToIntRun(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      //basic init
      init();
   }

   public PowerOrderedIntToIntRun(PDCtx pdc, ByteController mod, ByteObjectManaged tech) {
      super(pdc, tech);
      init();
   }

   /**
    * Add one chunk 
    *
    */
   private void addChunk() {
      //the future number of chunks 
      int num = getChunkBase() + 1;
      //keep a copy of the old value for the offset updating
      int old = getChunkBase();
      setChunkBase(num);
      //the number of 
      int changeInBits = 8 * ITechOrderedIntToIntRun.CHUNK_HEADER_BYTE_SIZE;
      //since we are insert CHUNK_HEADER_BYTE_SIZE bytes. the coordinates of all Chunks 
      //changes
      //to ease write operation, there is a buffer area
      ensureBufferCapacity(ITechOrderedIntToIntRun.CHUNK_HEADER_BYTE_SIZE * 8);

      //adding a chunk adds bits in between, therefor must be updated
      updateChunkCoordinates(0, old, changeInBits);
      //this must be done after the update
      createChunk(num);
      //System.out.println(motherDebug());

   }

   /**
    * the number of chunks to add
    * ChunkBase will be increased of num
    * @param num
    */
   public void addChunks(int num) {
      for (int i = 0; i < num; i++) {
         addChunk();
      }
   }

   public void addValue(int value) {
      addValue(value, 0);
   }

   /**
    * Add value to the bag
    * value is hashed on the bit length
    * <br>
    * What about negative values? They are stored in the negative.
    * What about zero value added.
    * 
    * @param value
    * @param data 0 if no data
    * 
    * POST: data structure is created if not present before
    * with the automatic bit size mode
    */
   public void addValue(int value, int data) {
      this.addValue(value, data, false);
   }

   /**
    * 
    * @param value
    * @param data
    * @param ignoreData if true, data will not be written
    */
   public void addValue(int value, int data, boolean ignoreData) {
      if (value < 0) {
         //throw new IllegalArgumentException("Value must be > 0 :" + value);
      }
      int width = getBU().widthInBits(value);
      //minimum width of value is 7 
      if (width < 7)
         width = 7;
      //starts with 1 for the range of 1 to 7 bits numbers
      int chunkNumber = width - 6;
      if (chunkNumber > getChunkBase()) {
         addChunks(chunkNumber - getChunkBase());
      }
      // ensure capacity for the buffer. do it after the chunk creation which eats buffer
      ensureBufferCapacity((2 * width) + defDataBitSize);

      //System.out.println(this);

      if (useFlagBit()) {
         setFlagBitEfficient(chunkNumber);
      }
      boolean useFlagBit = useFlagBit(chunkNumber);
      // we need two coordinates: the chunk start coordinate and chunk end coordinate
      //use the static one
      // go to the end of the chunk
      BitCoordinate endCoordinate = (BitCoordinate) getChunkCoordinate(chunkNumber + 1).clone();
      //now go the start of the chunk
      BitCoordinate startCoordinate = getChunkCoordinate(chunkNumber);

      //at the begining of every chunk, we have a header telling the bit length to use for reading the length.
      int lengthsize = getChunkLengthBitSize(chunkNumber);
      //after reading the chunk header, start coordinate is ready to read the first frame
      int[] frame = new int[2];
      int[] nextFrame = new int[2];
      //counter used to track value position in order to insert data
      int count = 0;
      //initial case where the chunk is empty without values (just the length size header
      if (startCoordinate.compare(endCoordinate) != -1) {
         //change value method call occurs inside method
         insertLone(chunkNumber, value, width, startCoordinate, lengthsize, useFlagBit);
         count++;
      }
      int frameCount = 0;
      int nonLoneframeCount = 0;
      boolean inside = false;
      //loop
      // read frame (frame is the pair (length of frame,start value of frame)
      // start frame check
      // end frame check
      // check if insertion point
      // next frame while current coordinate is smaller than next chunk coordinate
      while (startCoordinate.compare(endCoordinate) == -1) {
         // read frame

         // tick current position to compute the frane bit length
         startCoordinate.tick();

         // frame reading must ensure it does not get on the next chunk
         readNextFrame(frame, startCoordinate, width, lengthsize, useFlagBit);

         int bitFrameLength = startCoordinate.getInterval();
         boolean isLastFrame = false;
         //now check if this is the lastframe
         if (startCoordinate.compare(endCoordinate) != -1) {
            //case where a new lone frame has to be inserted
            isLastFrame = true;
         } else {
            // check next frame start for potential bridging in the frame check end
            readNextFrame(nextFrame, startCoordinate, width, lengthsize, useFlagBit);
         }
         if (isInsideFrame(value, frame)) {
            //found so we leave because there is nothing to add, except maybe data
            count += getPositinInFrame(frame[1], value);
            inside = true;
            //		int val = frame[1];
            //		count++;
            //		while (val != value) {
            //		   val++;
            //		   count++;
            //		}
            //		inside = true;
            break;
         }
         //end read frame

         // check start of frame
         if (value == frame[1] - 1) {
            //normal extension for [x,y]
            //come back at start of the frame
            startCoordinate.tack();
            if (useFlagBit) {
               //very important step which insert length if frame[0] == 0
               prepareStartCoordinate(chunkNumber, startCoordinate, lengthsize, frame);
            }
            //ASSERT startCoordinate points to the length field
            int newlen = frame[0] + 1;
            int old = lengthsize;
            lengthsize = getLengthSize(newlen, lengthsize, chunkNumber);
            int diff = lengthsize - old;
            //the start coordinate is not more correct if lengthsize has been modified
            //forward the difference x times the frames previously read
            startCoordinate.forward((frameCount * diff));
            getBU().copyBits(this.data, startCoordinate, newlen, lengthsize);
            // the new value becomes the new frame start because it is smaller than the previous one
            getBU().copyBits(this.data, startCoordinate, value, width);
            //extension of a frame
            if (frame[0] == 0) {
               changeValues(chunkNumber, 1, -1, 1);
            } else {
               //num below changes
               changeValues(chunkNumber, 1, 0, 0);
            }

            //simply +1 because at the start of the frame
            count++;
            break;
         }

         // check end of frame
         if (checkValueFrameEnd(value, getLast(frame))) {
            // go back to frame start in order to write the new data
            if (checkNext(value, nextFrame[1])) {
               //CASE WHERE A BRIDGE IS CREATED
               //first update the lower frame. 
               startCoordinate.tack(); // comes back at start of frame
               // +1 because len of 0 for the next Frame means one element
               int newlen = frame[0] + nextFrame[0] + 1 + 1;
               ////FIRST UPDATE THE LENGTH TO AVOID INTERFERENCES
               int old = lengthsize;
               lengthsize = getLengthSize(newlen, lengthsize, chunkNumber);
               int diff = lengthsize - old;
               //the start coordinate is not more correct if lengthsize has been modified
               //forward the difference x times the frames previously read
               if (useFlagBit) {
                  startCoordinate.forward(nonLoneframeCount * diff);
               } else {
                  startCoordinate.forward(((frameCount) * diff));
               }
               //// START COORDINATE IS POSITIONED FOR 1st FRAME

               int bitsused = 2 * (width + lengthsize);
               int bitsneeded = width + lengthsize;
               //now start is at the start of the next Frame

               //offset is present in start
               int bitnum = startCoordinate.unmap() + 1;
               if (useFlagBit) {
                  bitsneeded++;
                  bitsused += 2;
                  if (frame[0] == 0) {
                     bitsused -= lengthsize;
                  }
                  if (nextFrame[0] == 0) {
                     bitsused -= lengthsize;
                  }
               }
               //the number of bits that will change
               int shiftsize = bitsused - bitsneeded;
               //System.out.println("len="+newlen + " start=" + frame[1] + " bitused="+bitsused + " bitsneed="+bitsneeded + " shift="+shiftsize);
               //System.out.println(toString());
               //System.out.println(startCoordinate.unmap());
               writeFrame(width, lengthsize, useFlagBit, startCoordinate, newlen, frame[1]);
               //shift from the first bit after the next Frame
               getBU().shiftBitsDown(this.data, shiftsize, bitnum + bitsneeded + shiftsize, getLastUsedBitLoaded(), true);

               updateChunkCoordinates(chunkNumber + 1, getChunkBase(), 0 - shiftsize);
               lastusedbit -= shiftsize;
               //the lone is now a big frame
               if (frame[0] == 0) {
                  //loss of a lone
                  changeValues(chunkNumber, 1, -1, 0);
                  if (nextFrame[0] == 0) {
                     //loss of 2 lones for a frame
                     changeValues(chunkNumber, 0, -1, 1);
                  }
                  //only loss of a lone
               } else {
                  if (nextFrame[0] == 0) {
                     //loss of lone. appends when bridging with a lone
                     changeValues(chunkNumber, 1, -1, 0);
                  } else {
                     //loss of frame
                     changeValues(chunkNumber, 1, 0, -1);
                  }
               }
               //number of values + 1 for the new value
               count += (frame[0] + 1 + 1);
               break;
            } else {
               //normal extension 
               //check flag bit if we are using it
               startCoordinate.tack();
               if (useFlagBit) {
                  prepareStartCoordinate(chunkNumber, startCoordinate, lengthsize, frame);
               }
               int newlen = frame[0] + 1;
               int old = lengthsize;
               lengthsize = getLengthSize(newlen, lengthsize, chunkNumber);
               int diff = lengthsize - old;
               //the start coordinate is not more correct if lengthsize has been modified
               //forward the difference x times the frames previously read
               //here -1 because we go back one frame
               if (useFlagBit) {
                  //in case of flagbit use, the frameCount is not correct because
                  //it does not exclude frames without a length value having a flagbit instead
                  startCoordinate.forward((nonLoneframeCount) * diff);
                  //we don't need to update the next coordinate
               } else {
                  startCoordinate.forward((frameCount) * diff);
               }
               // insert new frame length
               getBU().copyBits(this.data, startCoordinate, newlen, lengthsize);
               // the frame start does not change because we are right now at the end
               // BitMask.copyBits(_data, startCoordinate, value, width);
               //
               if (frame[0] == 0)
                  changeValues(chunkNumber, 1, -1, 1);
               else
                  changeValues(chunkNumber, 1, 0, 0);

               //number of values + 1 for the new value
               count += (frame[0] + 1 + 1);
               break;
            }
         }
         //	case add 10 with [0:12]
         if (value < frame[1]) {
            startCoordinate.tack();
            insertLone(chunkNumber, value, width, startCoordinate, lengthsize, useFlagBit);
            count++;
            break;
         }
         // check if insertion point
         // case add 12 [0:9] [0:20] || case add 12 for [0:10]. -1 because 69 [70] is not valid
         if (value < nextFrame[1] - 1) {
            //case where a new lone frame has to be inserted before an existing next frame
            //since startCoordinate points
            startCoordinate.tack();
            startCoordinate.forward(bitFrameLength);
            //change value method call occurs inside method
            insertLone(chunkNumber, value, width, startCoordinate, lengthsize, useFlagBit);
            count += (frame[0] + 1 + 1);
            break;
         }
         if (isLastFrame) {
            //change value method call occurs inside method
            insertLone(chunkNumber, value, width, startCoordinate, lengthsize, useFlagBit);
            count += (frame[0] + 1 + 1);
            break;
         }
         //next frame
         startCoordinate.tack();
         startCoordinate.forward(bitFrameLength);
         count += (frame[0] + 1);
         if (useFlagBit && frame[0] != 0)
            nonLoneframeCount++;
         frameCount++;
      }

      if (!ignoreData) {
         int widthInBits = getBU().widthInBits(data);
         if (data != 0 && dataBitSize == 0) {
            createDataTable(widthInBits);
         }
         //check databitsize if automatic option is ON
         if (dataBitSize == 1) {
            setDataBitSize(widthInBits);
         }
         //now insert data, only if and insertion was do we expand
         if (dataBitSize != 0) {
            BitCoordinate c = getChunkCoordinate(0);
            count += getNumBelow(chunkNumber);
            //System.out.println(count);
            c.forward((count - 1) * defDataBitSize);
            if (!inside) {
               int bitnum = c.unmap() + 1;
               //only expand if a value was added
               getBU().shiftBitsUp(this.data, defDataBitSize, bitnum, getLastUsedBitLoaded(), true);
               lastusedbit += defDataBitSize;
            }
            getBU().copyBits(this.data, c, data, defDataBitSize);
         }

      }
   }

   /**
    * make room for value
    */
   public void addValueShift(int value) {
      addValueShift(value, 0);
   }

   private static int getChunkNumber(int width) {
      if (width <= 7)
         return 1;
      return width - 6;
   }

   public void addValueShift(int value, int data) {
      if (value < 1) {
         throw new IllegalArgumentException("Value must be > 0 :" + value);
      }

      int widthv = getBU().widthInBits(value);
      int startChunk = getChunkNumber(widthv);
      int chunkBase = getChunkBase();
      int[] frame = new int[2];
      ensureBufferCapacity((2 * widthv) + defDataBitSize);
      //track
      //tracks the change to be made in the count below value of each chunk
      //we add one
      // if 0:255 a new chunk of 9 bits must be created with 0:256
      // same for 2:253 which becomes 1:254 and 0:256 if there was 1:256, it becomes 2:256
      //strategy:
      // Start with chunk of value. If value belongs to a frame, frame is updated on the spot
      // if shifting create a boundary hop, hop is flagged and will be managed afterwards
      // 3a) add the boundary value in the chunk
      // 4a) update the n,x and y values
      //keep tracks
      boolean[] boundariesForward = new boolean[chunkBase + 1];
      boolean[] valueInBoundary = new boolean[chunkBase + 1];
      int[] boundaryFrameSize = new int[chunkBase + 1];
      BitCoordinate c = createBitCoordinate();
      for (int chunkNumber = chunkBase; chunkNumber >= startChunk; chunkNumber--) {
         int width = 6 + chunkNumber;
         int lastbit = getLastChunkBit(chunkNumber);
         int lenbitsize = getChunkLengthBitSize(chunkNumber);
         int frameCount = 0;
         int nonLoneframeCount = 0;
         BitCoordinate next = createBitCoordinate();
         next.map(lastbit);
         boolean useFlagBit = useFlagBit(chunkNumber);
         c = getChunkCoordinate(chunkNumber);
         //iterates over every frame on this chunk
         while (c.compare(next) == -1) {
            c.tick();
            readNextFrame(frame, c, width, lenbitsize, useFlagBit);
            System.out.println("read frame in addshift " + frame[0] + ":" + frame[1]);
            //either frame smaller
            if (frame[1] + frame[0] < value) {
               if (useFlagBit && frame[0] != 0)
                  nonLoneframeCount++;
               frameCount++;
               continue;
            }

            //either value inside the frame
            if (isInsideFrame(value, frame)) {
               //case when an up frame is shifted
               int newLength = frame[0] + 1;
               //
               if (getBU().widthInBits(frame[1] + newLength) > width) {
                  boundariesForward[chunkNumber] = true; //frame boundaries issue for chunk.
                  boundaryFrameSize[chunkNumber] = frame[0];
                  valueInBoundary[chunkNumber] = true;
                  //frame does not change because value will be added
                  //add a new value
                  int nval = 1 << (6 + chunkNumber);
                  addValue(nval, 0, true);
               } else {
                  //case where shift occurs inside a frame like 71 for 3:70
                  c.tack();
                  if (useFlagBit && frame[0] == 0) {
                     //we will have a big frame. meaning the length field has to be inserted
                     //between the flagbit and the value field
                     c.tick();
                     insertLenghInFrame(chunkNumber, c, lenbitsize);
                     //update the new
                     c.tack();
                  }
                  //System.out.println("==== Before update");
                  //System.out.println(this.debug());
                  int old = lenbitsize;
                  //System.out.println(c);
                  lenbitsize = getLengthSize(newLength, lenbitsize, chunkNumber);
                  int diff = lenbitsize - old;
                  if (useFlagBit) {
                     //in case of flagbit use, the frameCount is not correct because
                     //it does not exclude frames without a length value having a flagbit instead
                     c.forward((nonLoneframeCount) * diff);
                     next.forward(getNumFrames(chunkNumber) * diff);
                  } else {
                     c.forward((frameCount) * diff);
                     next.forward((getNumFrames(chunkNumber) + getNumLoners(chunkNumber)) * diff);
                  }

                  //System.out.println(c);
                  //System.out.println(this.debug());
                  writeFrame(width, lenbitsize, useFlagBit, c, newLength, frame[1]);
                  //System.out.println(this.debug());
                  if (newLength == 1) {
                     changeValues(chunkNumber, 1, -1, 1);
                     //+update numBelow of chunks aboe
                  } else {
                     changeValues(chunkNumber, 1, 0, 0);
                  }
               }
            } else {
               //case of a loner

               //case frame gets bigger
               int length = frame[0];
               int newFrameStart = frame[1] + 1;

               if (getBU().widthInBits(length + newFrameStart) > width) {
                  //flag this chunk having its boundary erase
                  boundariesForward[chunkNumber] = true;
                  boundaryFrameSize[chunkNumber] = frame[0];
                  //write the frame or remove it
                  c.tack();
                  if (length == 0) {
                     //we can remove it because we are at the end of the frame.
                     //remove current frame.
                     remove(frame[1], true);
                     //                     writeFrame(width, lenbitsize, useFlagBit, c, 0, 0);
                     //                     //remove by one the number of items below  
                     //                     changeValues(chunkNumber, -1, -1, 0);
                  } else {
                     remove(frame[1] + length, true);
                     //                     writeFrame(width, lenbitsize, useFlagBit, c, length - 1, newFrameStart);
                     //                     //remove by one the number of items below  
                     //                     changeValues(chunkNumber, -1, 0, 0);
                     System.out.println("### Boundary Found at chunkNumber " + chunkNumber + " ValueStart=" + frame[1] + " newFrameStart/Len=" + newFrameStart + "," + (length - 1));
                  }
                  int nval = 1 << (6 + chunkNumber);
                  addValue(nval, 0, true);
               } else {
                  //case of a simple shift
                  c.tack();
                  System.out.println("newFrameStart=" + newFrameStart);
                  writeFrame(width, lenbitsize, useFlagBit, c, frame[0], newFrameStart);
               }
            }
            if (useFlagBit && frame[0] != 0)
               nonLoneframeCount++;
            frameCount++;
         }
         //changeValues(chunkNumber, n, addx, addy);
      }
      //code has switch
      //#debug
      //printDataStruct("addValueShift After Updating Frames " + this.toString("\n\t"));

      //System.out.println(this.toString());
      //write back the boundaries, without changing the data array at the end
      //      for (int chunkNumber = 1; chunkNumber < boundariesForward.length; chunkNumber++) {
      //         if (boundariesForward[chunkNumber]) {
      //            //for next chunk add the very first value
      //            int nval = 1 << (6 + chunkNumber);
      //            System.out.println(chunkNumber + " Boundary new value=" + nval);
      //            System.out.println("Before adding " + nval + " " + this);
      //            addValue(nval, 0, true);
      //            System.out.println("After adding " + nval + " " + this);
      //            if (valueInBoundary[chunkNumber]) {
      //               remove(value, true);
      //            } else {
      //               //case where value addedshift is NOT in the boundary frame
      ////               if (boundaryFrameSize[chunkNumber] == 0) {
      ////                  remove(nval - 1, true);
      ////               } else {
      ////                  //remove(nval - 1 - boundaryFrameSize[chunkNumber], true);
      ////               }
      //            }
      //         }
      //      }
      //System.out.println("=========After removal boundary");
      //System.out.println(this.toString());
      //add and update value
      //check if the value is already in the
      int orderc = getValueOrderCount(value);
      //In boundary cases, the value isn't there. so we must add it
      if (orderc == 0) {
         //this add is the only one modifying the num below
         addValue(value, data, true);
         orderc = getValueOrderCount(value);
      }
      //check databitsize if automatic option is ON
      int widthInBits = getBU().widthInBits(data);
      if (data != 0 && dataBitSize == 0) {
         createDataTable(widthInBits);
      }
      if (dataBitSize == 1) {
         setDataBitSize(widthInBits);
      }
      BitCoordinate dataCoord = getChunkCoordinate(0);
      //System.out.println("orderc " + orderc + " for value " + value + " datacoord=" + dataCoord.toString());
      int bits = getDataBitSize();
      dataCoord.forward(bits * (orderc - 1));
      int bitnum = dataCoord.unmap() + 1;
      getBU().shiftBitsUp(this.data, defDataBitSize, bitnum, getLastUsedBitLoaded(), true);
      lastusedbit += defDataBitSize;
      getBU().copyBits(this.data, dataCoord, data, defDataBitSize);
   }

   /**
    * return true if value is smaller than the frame
    * i.e. value is not in the sequence
    * @param value
    * @param frame
    * @return
    */
   public boolean cancelValue(int value, int[] frame) {
      int v = frame[1];
      if (value < v)
         return true;
      return false;
   }

   /**
    * n=1 (add)
    * case x=1 	y=0 	(one lone added)
    * case x=0 	y=0 	(added on a border of frame)
    * case x=-1 	y=0 	(merge between frame and lone)
    * case x=0	y=-1	(merge between 2 frames) 
    * n=-1 (remove)
    * case x=-1 	y=0	(removed a lone) 
    * case x=0	y=1	(cut a frame into 2 frames)
    * case x=1 	y=0 	(removed just after a boundary to create one lone and keep the old frame)
    * case x=2 	y=-1 	(removed just after a boundary to create 2 lones, frame lost)
    * @param lone
    */
   private void changeValues(int chunk, int n, int x, int y) {
      //System.out.println("n=" + n + " x=" + x + " y=" + y);
      IntUtils.writeIntBE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_05_TOTAL_NUM_VALUES4, IntUtils.readIntBE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_05_TOTAL_NUM_VALUES4) + n);
      IntUtils.writeInt24BE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_06_TOTAL_LONERS3, IntUtils.readInt24BE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_06_TOTAL_LONERS3) + x);
      IntUtils.writeInt24BE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_07_TOTAL_BIG_FRAMES3, IntUtils.readInt24BE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_07_TOTAL_BIG_FRAMES3) + y);
      changeXY(chunk, x, y);
      //update
      for (int i = chunk + 1; i <= getChunkBase(); i++) {
         int numBelow = getNumBelow(i);
         setNumBelow(i, numBelow + n);
         //         int off = getChunkHeaderOffsetLoaded(i);
         //         off += ITechSequenceTableRun.OFFSET_CHUNK_06_NUM_BELOW_3;
         //         IntUtils.writeIntBE24(data, off, IntUtils.readInt24BE(data, off) + n);
      }
   }

   private void changeXY(int chunkNumber, int x, int y) {
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      off += ITechOrderedIntToIntRun.OFFSET_CHUNK_04_NUM_LONERS_3;
      int ox = IntUtils.readInt24BE(data, off);
      ox = ox + x;
      IntUtils.writeInt24BE(data, off, ox);
      off += 3;
      int oy = IntUtils.readInt24BE(data, off);
      oy = oy + y;
      IntUtils.writeInt24BE(data, off, oy);
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
    * 
    * @param num
    */
   private void createChunk(int num) {

      //start position for the new chunk header
      int newChunkHeaderOffset = getChunkHeaderOffsetLoaded(num);
      //to ease write operation, there is a buffer area
      int lastLoadedByte = getLastLoadUsedByte();

      ensureBufferCapacity(ITechOrderedIntToIntRun.CHUNK_HEADER_BYTE_SIZE * 8);

      //
      getBU().shiftBytesUp(data, ITechOrderedIntToIntRun.CHUNK_HEADER_BYTE_SIZE, newChunkHeaderOffset, lastLoadedByte);

      //update the position of lastusedbit
      lastusedbit += (ITechOrderedIntToIntRun.CHUNK_HEADER_BYTE_SIZE * 8);
      //System.out.println("lastusedbit=" + _lastusedbit);
      //	if (_lastusedbit / 8 > _len)
      //	   throw new RuntimeException(_lastusedbit / 8 + " " + _len);

      //now that the lastusedbit is updated with the new header. 
      //it is used to compute the original Chunk Coordinate
      BitCoordinate end = createBitCoordinate();
      if (num >= 1) {
         end = getChunkCoordinate(0);
      } else {
         end.map(getLastUsedBitLoaded());
      }
      setChunkCoordinate(num, end);

      //System.out.println("chunk bit coordinate for chunk #" + num + " = " + (end.bitnum + (8 * end.bytenum)) + " bits");
      //update the old chunk offsets. x bytes shifted up

      //field tells the #of bits to read the length
      //copy the default length bitsize for reading the frame size. 
      //either 1 or something else. 6 + because when num=1 we want to have 7 bitsize
      if (num != 0) {
         setChunkLengthBitSize(num, DEFAULTLengthSize);
      }

      if (num > 1) {
         setNumBelow(num, getSize());
      }

   }

   /**
    * When creating the data table, changes the flag
    * @param widthInBits
    */
   private void createDataTable(int widthInBits) {
      setAutoDataHeader(1);
      //create the structure. give 0 as data for all existing values
      BitCoordinate c = getChunkCoordinate(0);
      int size = getSize();
      int len = size * widthInBits;
      ensureBufferCapacity(len);
      int bitnum = c.unmap() + 1;
      getBU().shiftBitsUp(data, len, bitnum, getLastUsedBitLoaded(), true);
      lastusedbit += len;
   }

   /**
    * Ensures that the bitValue 
    * @param bitValue new number of bits to be supported
    */
   public void ensureBufferCapacity(int bitValue) {
      int loadedPosition = getNextAlienOffset();
      ensureBufferCapacity(bitValue, loadedPosition);
   }

   /**
    * Expand array at the bit position.
    * 
    * @param bitValue
    * @param position offset loaded position
    */
   public void ensureBufferCapacity(int bitValue, int position) {
      //keep in mind lastusedbit is relative to offset. 
      //so we are making a relative comparaison.
      if (lastusedbit + bitValue >= getBitTableBufferEnd()) {
         //incr should reflect memory setting HIGH or LOW
         int incr = bitValue + (lastusedbit / 8);
         //incr has a byte meaning for method expand array
         expandDataArray(incr, position);
      }
   }

   /**
    * Set the flag bit value to on.
    * method makes the necessary changes
    * @param chunkNumber
    * @param on
    */
   public void setFlagBitForce(int chunkNumber, boolean on) {
      setFlagBitEfficient(chunkNumber, on, false);
      //      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      //      int L = data[off + ITechSequenceTableRun.SQ_RUN_OFFSET_04_CHUNK_L1];
      //      int flagbit = data[off + ITechSequenceTableRun.OFFSET_CHUNK_03_FLAGBIT_1];
      //      int x = getX(chunkNumber);
      //      int y = getY(chunkNumber);
      //      //(L+1 * Y) + X 
      //      int bitsUsedWithFlag = ((L + 1) * y) + x;
      //      //L * (X + Y)
      //      int bitsUsedWithOutFlag = L * (x + y);
      //      //check
      //      int width = 6 + chunkNumber;
      //      bitsUsedWithOutFlag += ((x + y) * width);
      //      bitsUsedWithFlag += ((x + y) * width);
      //      if (flagbit == 0 && on) {
      //         //must be negative because we will shrink
      //         int bitsChange = bitsUsedWithFlag - bitsUsedWithOutFlag;
      //         setFlagBit(chunkNumber, bitsUsedWithFlag, bitsChange, true);
      //      } else if (flagbit == 1 && !on) {
      //         //remove flagbit
      //         int bitsChange = bitsUsedWithOutFlag - bitsUsedWithFlag;
      //         setFlagBit(chunkNumber, bitsUsedWithOutFlag, bitsChange, false);
      //      }
   }

   private void setFlagBitEfficient(int chunkNumber, boolean on, boolean threshold) {
      int length = getChunkLengthBitSize(chunkNumber);
      boolean useFlagBit = useFlagBit(chunkNumber);
      int x = getNumLoners(chunkNumber);
      int y = getNumFrames(chunkNumber);
      //(L+1 * Y) + X 
      int bitsUsedWithFlag = ((length + 1) * y) + x;
      //L * (X + Y)
      int bitsUsedWithOutFlag = length * (x + y);
      //check
      //System.out.println("bitsUsedWithFlag=" + bitsUsedWithFlag + " bitsUsedWithOutFlag=" + bitsUsedWithOutFlag + " x=" + x + " y=" + y + " Len=" + length);
      int width = 6 + chunkNumber;
      bitsUsedWithFlag += ((x + y) * width);
      bitsUsedWithOutFlag += ((x + y) * width);

      if (useFlagBit) {
         //currently using it. do we remove it?
         if (!on || (threshold && bitsUsedWithOutFlag + FLAGBIT_CHANGE_THRESHOLD < bitsUsedWithFlag)) {
            //remove flagbit
            int bitsChange = bitsUsedWithOutFlag - bitsUsedWithFlag;
            setFlagBit(chunkNumber, bitsUsedWithOutFlag, bitsChange, false);
         }
      } else {
         //not using it. do we set
         if (on || (threshold && bitsUsedWithFlag + FLAGBIT_CHANGE_THRESHOLD < bitsUsedWithOutFlag)) {
            int bitsChange = bitsUsedWithFlag - bitsUsedWithOutFlag;
            setFlagBit(chunkNumber, bitsUsedWithFlag, bitsChange, true);
         }
      }
   }

   public void setFlagBitEfficient(int chunkNumber) {
      setFlagBitEfficient(chunkNumber, false, true); //
      //      int length = getChunkLengthBitSize(chunkNumber);
      //      boolean useFlagBit = useFlagBit(chunkNumber);
      //      int x = getX(chunkNumber);
      //      int y = getY(chunkNumber);
      //      //(L+1 * Y) + X 
      //      int bitsUsedWithFlag = ((length + 1) * y) + x;
      //      //L * (X + Y)
      //      int bitsUsedWithOutFlag = length * (x + y);
      //      //check
      //      //System.out.println("bitsUsedWithFlag=" + bitsUsedWithFlag + " bitsUsedWithOutFlag=" + bitsUsedWithOutFlag + " x=" + x + " y=" + y + " Len=" + length);
      //      int width = 6 + chunkNumber;
      //      bitsUsedWithFlag += ((x + y) * width);
      //      bitsUsedWithOutFlag += ((x + y) * width);
      //      if (bitsUsedWithFlag + FLAGBIT_CHANGE_THRESHOLD < bitsUsedWithOutFlag && !useFlagBit) {
      //         //must be negative because we will shrink
      //         int bitsChange = bitsUsedWithFlag - bitsUsedWithOutFlag;
      //         setFlagBit(chunkNumber, bitsUsedWithFlag, bitsChange, true);
      //      } else if (bitsUsedWithOutFlag + FLAGBIT_CHANGE_THRESHOLD < bitsUsedWithFlag && useFlagBit) {
      //         //remove flagbit
      //         int bitsChange = bitsUsedWithOutFlag - bitsUsedWithFlag;
      //         setFlagBit(chunkNumber, bitsUsedWithOutFlag, bitsChange, false);
      //      }
   }

   public int getBitSize(int chunkNumber) {
      int lastbit = getLastChunkBit(chunkNumber);
      BitCoordinate c = getChunkCoordinate(chunkNumber);
      int start = c.unmap();
      return lastbit - start;
   }

   /**
    * Number of bits con
    * NOT LOADED
    * @return _len * 8.
    */
   protected int getBitTableBufferEnd() {
      return getLength() * 8;
   }

   /**
    * The Chunk Base is the current number of chunks in the Sequence
    * 1-7 bitsizes is chunk number one. bitsize 8 is chunk #2. 
    * @return 0 if no chunks
    */
   public int getChunkBase() {
      return get1(OITI_RUN_OFFSET_00_HEADER_CHUNK_BASE1);
   }

   /**
    * Return the position for reading chunk values (length chunk values)
    * <br>
    * The special 0 chunk is the byte area for storing datas. 
    * <br>
    * @param bitChunk 1 for(7 bits) 2 for 8 bits, 0 for data chunk
    * @return
    * the data chunk coordinate for chunks bigger than the baseChunk
    * _offset loaded
    */
   public BitCoordinate getChunkCoordinate(int bitChunk) {
      BitCoordinate c = createBitCoordinate();
      if (bitChunk > getChunkBase()) {
         //throw new RuntimeException(bitChunk + ">"+ getChunkBase());
         return getChunkCoordinate(0);
      }
      //position of the chunk header of length 15 bytes
      int bitnum = 0;
      int bytenum = getChunkHeaderOffsetLoaded(bitChunk);
      c.setByteAndBit(bytenum, bitnum);

      //read the coordinate from the chunk header
      bitnum = getBU().readBits(data, c, 4);
      bytenum = getBU().readBits(data, c, ITechOrderedIntToIntRun.DEFAULT_BYTE_POINTER_SIZE);

      bytenum = bytenum + index;
      c.setByteAndBit(bytenum, bitnum);
      return c;
   }

   /**
    * Return the byte position where one can read the Chunk header.
    * i.e the Coordinate and # of values below that chunk
    * <br>
    * Note: because theses pointers are never bitshifted, we keep them in simple byte coordinate
    * <br>
    * <br>
    * @param chunk the chunk to get the position for
    * @return _offset loaded
    */
   private int getChunkHeaderOffsetLoaded(int chunk) {
      //chunk 0 exists
      return index + ITechOrderedIntToIntRun.OITI_RUN_BASIC_SIZE + ((chunk) * ITechOrderedIntToIntRun.CHUNK_HEADER_BYTE_SIZE);
   }

   public int getChunkLengthBitSize(int chunkNumber) {
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      off += ITechOrderedIntToIntRun.OFFSET_CHUNK_02_LENGTH_BITSIZE_1;
      return data[off];
   }

   private BitCoordinate getComparator(int chunkNumber) {
      BitCoordinate next = createBitCoordinate();
      int lastbit = getLastChunkBit(chunkNumber);
      next.map(lastbit);
      return next;
   }

   /**
    * Return the data stored for that value
    * @param value
    * @return 0 if no datas or no values
    */
   public int getData(int value) {
      if (dataBitSize == 0)
         return 0;
      int count = getValueOrderCount(value);
      return getDataFromOrderCount(count);
   }

   /**
    * Current Databit size
    * {@link ITechOrderedIntToIntRun#OITI_RUN_OFFSET_02_DATA_BITSIZE1}
    * @return
    */
   public int getDataBitSize() {
      return defDataBitSize;
   }

   public BitCoordinate getDataCoordinate() {
      return getChunkCoordinate(0);
   }

   /**
    * Return 0 if count is 0
    * @param count
    * @return
    */
   public int getDataFromOrderCount(int count) {
      if (count == 0)
         return 0;
      //count is the id for the data
      BitCoordinate data = getChunkCoordinate(0);
      //error 205 byte
      int bits = getDataBitSize();
      data.forward(bits * (count - 1));
      return getBU().readBits(this.data, data, bits);
   }

   /**
    * Return the number of bits consumed by the frame position at Coordinate c
    * @param c start of the frame
    * @param width
    * @param lengthsize
    * @param useFlagBit
    * @return
    */
   public int getFrameLength(BitCoordinate c, int width, int lengthsize, boolean useFlagBit) {
      int flagbit = 0;
      if (useFlagBit) {
         flagbit = getBU().readBit(data, c);
         c.rewind(1);
         width++;
      }
      if (flagbit == 1) {
         //lone integer
         return width;
      } else {
         //framed integers
         return width + lengthsize;
      }

   }

   public int getLast(int[] frame) {
      return frame[1] + frame[0];
   }

   /**
    * 
    * @param chunk
    * @return loaded bit value
    */
   public int getLastChunkBit(int chunk) {
      if (chunk == 0)
         return getLastUsedBitLoaded();
      if (chunk == getChunkBase()) {
         return getChunkCoordinate(0).unmap();
      } else
         return getChunkCoordinate(chunk + 1).unmap();
   }

   public int getLastLoadUsedByte() {
      int _lastusedbit = getLastUsedBitLoaded();
      if (_lastusedbit % 8 == 0)
         return _lastusedbit / 8;
      return (_lastusedbit / 8) + 1;
   }

   /**
    * 
    * @return
    */
   public int getLastUsedBitLoaded() {
      return (index * 8) + lastusedbit;
   }

   /**
    * Last byte with relevant data relative to _offset
    * @return
    */
   public int getLastUsedByte() {
      if (lastusedbit % 8 == 0)
         return lastusedbit / 8;
      return (lastusedbit / 8) + 1;
   }

   /**
    * 
    * @param newlen real value
    * @param oldlengthbitsize bit size
    * @param chunkNumber
    * @return
    */
   private int getLengthSize(int newlen, int oldlengthbitsize, int chunkNumber) {
      int newlenbitsize = getBU().widthInBits(newlen);
      if (newlenbitsize > oldlengthbitsize) {
         updateLengthSize(chunkNumber, oldlengthbitsize, newlenbitsize);
         return newlenbitsize;
      }
      return oldlengthbitsize;
   }

   public Object getMorph(MorphParams p) {
      if (p.cl == IntToInts.class) {
         IntToInts its = new IntToInts(pdc.getUCtx());

         return its;
      }
      return this;
   }

   public int getNumNegatives() {
      int base = getChunkBase();
      if (base == 26) {
         return getSize() - getNumBelow(26);
      }
      return 0;
   }

   private int addInts(int i, int[] vals, int offset, boolean data) {
      int add = 1;
      if (data)
         add = 2;
      int count = offset;
      int width = 6 + i;
      int lastbit = getLastChunkBit(i);
      int lenbitsize = getChunkLengthBitSize(i);
      int[] frame = new int[2];
      BitCoordinate next = createBitCoordinate();
      next.map(lastbit);
      boolean useFlagBit = useFlagBit(i);
      BitCoordinate c = getChunkCoordinate(i);
      while (c.compare(next) == -1) {
         readNextFrame(frame, c, width, lenbitsize, useFlagBit);
         for (int j = 0; j <= frame[0]; j++) {
            vals[count] = frame[1] + j;
            count += add;
         }
      }

      return count;
   }

   private int[] getMyInts(boolean data) {
      int add = 1;
      if (data) {
         add = 2;
      }
      int size = getSize();
      int[] ar = new int[size * add];
      int numNegatives = 0;
      int base = getChunkBase();
      int count = 0;
      if (base == 26) {
         count = addInts(base, ar, 0, data);
         base = 25;
         if (data) {
            //add the negative data
            BitCoordinate coordNegativeData = getChunkCoordinate(0);
            int numPositives = getNumBelow(26);
            numNegatives = size - numPositives;
            coordNegativeData.forward(defDataBitSize * numPositives);
            int negativeDataIndexCount = 1;
            for (int i = 0; i < numNegatives; i++) {
               ar[negativeDataIndexCount] = getBU().readBits(this.data, coordNegativeData, defDataBitSize);
               negativeDataIndexCount += 2;
            }
         }
      }
      try {
         BitCoordinate c = null;
         for (int i = 1; i <= base; i++) {
            int width = 6 + i;
            int lastbit = getLastChunkBit(i);
            int lenbitsize = getChunkLengthBitSize(i);
            int[] frame = new int[2];
            BitCoordinate next = createBitCoordinate();
            next.map(lastbit);
            boolean useFlagBit = useFlagBit(i);
            c = getChunkCoordinate(i);
            while (c.compare(next) == -1) {
               readNextFrame(frame, c, width, lenbitsize, useFlagBit);
               for (int j = 0; j <= frame[0]; j++) {
                  ar[count] = frame[1] + j;
                  count += add;
               }
            }
         }
         if (!data) {
            return ar;
         }
         count = 1 + numNegatives * 2;
         int numPos = size - numNegatives;
         c = getChunkCoordinate(0);
         for (int i = 0; i < numPos; i++) {
            ar[count] = getBU().readBits(this.data, c, defDataBitSize);
            count += 2;
         }
      } catch (ArrayIndexOutOfBoundsException e) {
         return ar;
      }
      return ar;
   }

   /**
    * Return the number of values stored below a chunkNumber
    * 
    * @param chunkNumber
    * @return
    */
   public int getNumBelow(int chunkNumber) {
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      off += ITechOrderedIntToIntRun.OFFSET_CHUNK_06_NUM_BELOW_3;
      return IntUtils.readInt24BE(data, off);
   }

   private int getPositinInFrame(int root, int value) {
      int count = 1;
      //safety valve. should never be covered
      if (value < root)
         return 0;
      while (root != value) {
         root++;
         count++;
      }
      return count;
   }

   public int getSize() {
      return IntUtils.readIntBE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_05_TOTAL_NUM_VALUES4);
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   /**
    * Return the value stored at position
    * @param position starts at 1
    * @return Integer.MAX_VALUE if position is out of range
    */
   public int getValueFromPosition(int count) {
      int chunkbase = getChunkBase();
      for (int chunkNumber = chunkbase; chunkNumber >= 1; chunkNumber--) {
         int numBelow = getNumBelow(chunkNumber);
         if (count > numBelow) {
            //we are in this chunk
            BitCoordinate c = getChunkCoordinate(chunkNumber);
            BitCoordinate next = createBitCoordinate();
            int lastbit = getLastChunkBit(chunkNumber);
            int lengthsize = getChunkLengthBitSize(chunkNumber);
            int width = chunkNumber + 6;
            int[] frame = new int[2];
            boolean useFlagBit = useFlagBit(chunkNumber);
            next.map(lastbit);
            int var = numBelow;
            while (c.compare(next) == -1) {
               // read frame
               readNextFrame(frame, c, width, lengthsize, useFlagBit);
               var += frame[0] + 1;
               if (count <= var) {
                  return frame[1] + (frame[0] - (var - count));
               }
            }
         }
      }
      return Integer.MAX_VALUE;
   }

   /**
    * Return the value's position in the ordered sequence
    * <br>
    * <br>
    * @param value
    * @return between 1 and max. 0 if value not in the sequence.
    */
   public int getValueOrderCount(int value) {
      int width = getBU().widthInBits(value);
      if (width < 7)
         width = 7;
      int chunkNumber = width - 6;
      if (chunkNumber > getChunkBase()) {
         return 0;
      }
      int count = getNumBelow(chunkNumber);
      int lengthsize = getChunkLengthBitSize(chunkNumber);
      int[] frame = new int[2];
      boolean useFlagBit = useFlagBit(chunkNumber);
      BitCoordinate c = getChunkCoordinate(chunkNumber);
      BitCoordinate next = createBitCoordinate();
      int lastbit = getLastChunkBit(chunkNumber);
      next.map(lastbit);
      while (c.compare(next) == -1) {
         // read frame
         readNextFrame(frame, c, width, lengthsize, useFlagBit);
         if (cancelValue(value, frame))
            return 0;
         if (isInsideFrame(value, frame)) {
            //found so we leave because there is nothing to add
            count += getPositinInFrame(frame[1], value);
            return count;
         }
         count += (frame[0] + 1);
      }
      return 0;
   }

   /**
    * Only return the values
    * @return
    */
   public int[] getValues() {
      return getMyInts(false);
   }

   /**
    * Return the values followed in +1 with its data
    * if no datas at all, datas will be 0
    * @return
    */
   public int[] getValuesDatas() {
      return getMyInts(true);
   }

   public int getX() {
      return IntUtils.readInt24BE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_06_TOTAL_LONERS3);
   }

   /**
    * Number of lones
    * @param chunkNumber
    * @return
    */
   public int getNumLoners(int chunkNumber) {
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      off += ITechOrderedIntToIntRun.OFFSET_CHUNK_04_NUM_LONERS_3;
      return IntUtils.readInt24BE(data, off);
   }

   public int getY() {
      return IntUtils.readInt24BE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_07_TOTAL_BIG_FRAMES3);
   }

   public int getNumFrames(int chunkNumber) {
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      off += ITechOrderedIntToIntRun.OFFSET_CHUNK_05_NUM_FRAMES_3;
      return IntUtils.readInt24BE(data, off);
   }

   public boolean hasValue(int value) {
      if (getValueOrderCount(value) != 0)
         return true;
      return false;
   }

   /**
    * Basic initialization of 1st chunk and data chunk
    *
    */
   protected void init() {
      initConstructor();
      //add the data chunk header that tracks the position of the start of the data
      createChunk(0);
      //add the default chunk for 1 to 7 bits integer values
      addChunks(1);
   }

   protected void initConstructor() {
      defDataBitSize = get1(OITI_RUN_OFFSET_02_DATA_BITSIZE1);
      dataBitSize = get1(OITI_RUN_OFFSET_03_DATABITSIZE_FLAG1);
      lastusedbit = get4(OITI_RUN_OFFSET_08_HEADER_LAST_USED_BIT4);
      if (lastusedbit == 0) {
         lastusedbit = ITechOrderedIntToIntRun.OITI_RUN_BASIC_SIZE * 8;
      }
      //by default, the flag bit is used
      setMainHeaderFlagBit(true);
   }

   /**
    * Method is called when a flagbit lone frame is expanded into a big frame
    * it inserts lengthsize bits in front of the bitflag and before the start value of the frame
    * @param chunkNumber
    * @param startCoordinate
    * @param lengthsize
    */
   private void insertLenghInFrame(int chunkNumber, BitCoordinate startCoordinate, int lengthsize) {
      // we have a lone frame with a FlagBit
      //we need to set the flag bit to 1 and expand data for the length chunk
      getBU().copyBits(data, startCoordinate, 0, 1);
      getBU().shiftBitsUp(data, lengthsize, startCoordinate.unmap() + 1, getLastUsedBitLoaded(), true);
      updateChunkCoordinates(chunkNumber + 1, getChunkBase(), lengthsize);
      lastusedbit += lengthsize;
   }

   /**
    * 
    * @param value
    * @param width
    * @param c offsetloaded
    * @param lengthsize
    */
   private void insertLone(int chunkNumber, int value, int width, BitCoordinate c, int lengthsize, boolean useFlagBit) {
      int len = useFlagBit ? width + 1 : (width + lengthsize);
      int bitnum = c.unmap() + 1;
      //System.out.println(this);
      // System.out.println("bitnum="+bitnum + " lastloadedusedbit="+getLastUsedBitLoaded() );
      getBU().shiftBitsUp(data, len, bitnum, getLastUsedBitLoaded(), true);
      //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
      int chunkNum = width - 6;
      c.tick();
      updateChunkCoordinates(chunkNum + 1, getChunkBase(), len);
      lastusedbit += len;
      //System.out.println(this);
      c.tack();
      if (useFlagBit) {
         getBU().copyBits(data, c, 1, 1);
         getBU().copyBits(data, c, value, width);
      } else {
         getBU().copyBits(data, c, 0, lengthsize);
         getBU().copyBits(data, c, value, width);
      }
      changeValues(chunkNumber, 1, 1, 0);
   }

   public boolean isInsideFrame(int value, int[] frame) {
      if (value >= frame[1] && value <= getLast(frame)) {
         return true;
      }
      return false;
   }

   public String motherDebug() {
      return "";
   }

   /**
    * Called internally when using the flagbit.
    * POST: tick-tacked start coordinate starts for reading/writing the length of the frame
    * @param chunkNumber
    * @param startCoordinate
    * @param lengthsize
    * @param frame
    */
   private void prepareStartCoordinate(int chunkNumber, BitCoordinate startCoordinate, int lengthsize, int[] frame) {
      if (frame[0] == 0) {
         //we will have a big frame. meaning the length field has to be inserted
         //between the flagbit and the value field
         startCoordinate.tick();
         insertLenghInFrame(chunkNumber, startCoordinate, lengthsize);
         //update the new
         startCoordinate.tack();
      }
      startCoordinate.forward(1);
   }

   /**
    * Read the [length:start] frame.
    * @param result write the results here
    * @param c the coordinate starting at the begining of the frame
    * @param width the bitsize of the start field
    * @param lengthsize the bitsize of the length field
    */
   private void readNextFrame(int[] result, BitCoordinate c, int width, int lengthsize, boolean useFlagBit) {
      if (useFlagBit) {
         int flagbit = getBU().readBit(data, c);
         if (flagbit == 1) {
            //lone integer
            result[0] = 0;
            result[1] = getBU().readBits(data, c, width);
            return;
         }
      }
      //framed integers
      result[0] = getBU().readBits(data, c, lengthsize);
      result[1] = getBU().readBits(data, c, width);
   }

   /**
    * Will remove the value and its data
    * @param value
    * @return -1 if value not present or if data = 0 of course
    */
   public int remove(int value) {
      return this.remove(value, false);
   }

   /**
    * Removes a value
    * @param value
    * @param ignoreData
    * @return
    */
   public int remove(int value, boolean ignoreData) {
      int width = getBU().widthInBits(value);
      if (width < 7) {
         width = 7;
      }
      int chunkNumber = width - 6;
      int count = 0;
      if (!hasValue(value))
         return 0;

      int lengthsize = getChunkLengthBitSize(chunkNumber);
      int[] frame = new int[2];
      boolean useFlagBit = useFlagBit(chunkNumber);
      BitCoordinate c = getChunkCoordinate(chunkNumber);
      BitCoordinate next = createBitCoordinate();
      int lastbit = getLastChunkBit(chunkNumber);
      next.map(lastbit);
      while (c.compare(next) == -1) {
         c.tick();
         // read frame
         readNextFrame(frame, c, width, lengthsize, useFlagBit);
         if (isInsideFrame(value, frame)) {
            //found so we leave because there is nothing to add
            int pos = getPositinInFrame(frame[1], value);
            //remove it by creating
            //CASE 1: 2 big frames, 
            //CASE 2: 2 lones
            //CASE 3: one each
            //CASE 4: make the frame disappear
            //CASE 5: big frame becomes a lone
            //frame bigger than 2. at least one big frame
            //axaa, aaxa, aaxaa/axa xaa, aax, size = frame[0]
            int[] firstFrame = new int[2];
            int[] secondFrame = new int[2];
            firstFrame[0] = pos - 2;
            secondFrame[0] = frame[0] - pos;
            firstFrame[1] = frame[1];
            secondFrame[1] = frame[1] + pos;

            //bit length of the current whole frame
            int len = width + lengthsize;
            if (useFlagBit) {
               len++;
               //case when a lone is removed
               if (frame[0] == 0)
                  len -= lengthsize;
            }

            int x = 0;
            int y = 0;
            if (firstFrame[0] < 0 && secondFrame[0] < 0) {
               //case x (lone)
               //System.out.println("case x");
               x = -1;
               y = 0;
            } else if (firstFrame[0] < 0 || secondFrame[0] < 0) {
               if (secondFrame[0] == 0 || firstFrame[0] == 0) {
                  //case xa or ax
                  //System.out.println("case ax or xa");
                  x = 1;
                  y = -1;
               } else {
                  //case xaa or aax
                  //System.out.println("case aax or xaa");
                  x = 0;
                  y = 0;
               }
            } else if (firstFrame[0] == 0 && secondFrame[0] == 0) {
               //case axa
               //System.out.println("case axa");
               x = 2;
               y = -1;
            } else if (firstFrame[0] > 0 && secondFrame[0] > 0) {
               //case aaxaa
               //System.out.println("case aaxaa");
               x = 0;
               y = 1;
            } else {
               //case aaxa or axaa
               //System.out.println("case aaxa or axaa");
               x = 1;
               y = 0;
            }
            //by default we create new big frame
            int bitChange = 0;
            int frameNum = 0;
            //if x is first, then no new frame is created
            if (firstFrame[0] >= 0) {
               bitChange += width;
               frameNum++;
            }
            //in the case of a lone both frames will be < 0
            if (secondFrame[0] >= 0) {
               bitChange += width;
               frameNum++;
            }
            if (useFlagBit) {
               bitChange += (1 * frameNum);
               if (firstFrame[0] > 0)
                  bitChange += lengthsize;
               if (secondFrame[0] > 0)
                  bitChange += lengthsize;
            } else {
               //happens when flag bit is off
               bitChange += (lengthsize * frameNum);
            }
            //count back the bits we already own with the current frame
            bitChange -= len;
            //System.out.println("bitChange ="+ bitChange + " lengthsize="+lengthsize + " len="+ len);

            //we will need bitChange
            //here bitnum will be the start of the next frame
            //if bit change is negative. bits will be removed
            int bitnum = c.unmap() + 1;
            //System.out.println("bitChange ="+ bitChange + " bitNum="+bitnum + " len="+ len);
            //System.out.println("ff[0]="+firstFrame[0] + " ff[1]="+firstFrame[1] + " sf[0]="+ secondFrame[0] + " sf[1]="+ secondFrame[1]);
            getBU().shiftBits(data, bitChange, bitnum, getLastUsedBitLoaded(), true);
            updateChunkCoordinates(chunkNumber + 1, getChunkBase(), bitChange);
            lastusedbit += bitChange;

            c.tack();
            int v = c.unmap();
            if (firstFrame[0] >= 0) {
               writeFrame(width, lengthsize, useFlagBit, c, firstFrame);
            }
            if (secondFrame[0] >= 0) {
               writeFrame(width, lengthsize, useFlagBit, c, secondFrame);
            }
            changeValues(chunkNumber, -1, x, y);
            count += pos;
            break;
            //END INSIDE FRAME
         }
         count += (frame[0] + 1);
      }
      if (!ignoreData) {
         //remove the data
         if (dataBitSize != 0) {
            c = getChunkCoordinate(0);
            count += getNumBelow(chunkNumber);
            //System.out.println(count);
            c.forward((count - 1) * defDataBitSize);
            c.tick();
            int data = getBU().readBits(this.data, c, defDataBitSize);
            c.tack();
            int bitnum = c.unmap() + defDataBitSize + 1;
            getBU().shiftBitsDown(this.data, defDataBitSize, bitnum, getLastUsedBitLoaded(), true);
            lastusedbit -= defDataBitSize;
            return data;
         }
      }
      return -1;

   }

   /**
    * Write down all the header values and sends a copy
    * of the copy array containing the data
    * @return
    */
   public byte[] serializePack() {
      //write last used bit
      IntUtils.writeInt24BE(data, index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_08_HEADER_LAST_USED_BIT4, lastusedbit);
      return (byte[]) data;
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return null;
   }

   public void setAutoDataHeader(int val) {
      dataBitSize = (byte) val;
      set1(OITI_RUN_OFFSET_03_DATABITSIZE_FLAG1, val);
   }

   public void setChunkBase(int value) {
      set1(OITI_RUN_OFFSET_00_HEADER_CHUNK_BASE1, value);
   }

   /**
    * Update the length bit size and flagbit of all chunk frames
    * PRE: there must be enough buffer room
    * @param chunkNumber
    * @param newBitsSize the total number of bits consumed by the chunk new frames
    * @param NumOfBitsToBeAdded if negative, bits must be added
    * @param flagbitOn if true, method will 
    * @param newLenbitsize writes the frame lengths with this bit size
    * on = true, currently OFF, set it ON
    * on = false, currently ON, set it OFF
    */
   private void setChunkBitsSize(int chunkNumber, int newBitsSize, int NumOfBitsToBeAdded, boolean flagbitOn, int newLenbitsize, int oldLenBitSize) {
      //switch to flagbit. easy
      //strategy. 
      // 1) create temp array of size bitsUsedWithFlag. 
      // 2) Copy values
      // 3) shrink main array of bitstoremove
      // 4) copy temp array
      //	int b = (getBitSize(chunkNumber) + NumOfBitsToBeAdded);
      //	System.out.println("" + b + "= newBitsSize=" + newBitsSize + " NumOfBitsToBeAdded=" + NumOfBitsToBeAdded + " newLenbitsize=" + newLenbitsize + " oldLenBitSize=" + oldLenBitSize + " flagon="
      //	      + flagbitOn);
      //	if (newBitsSize != b)
      //	   throw new RuntimeException(newBitsSize + " != " + b);
      //END CHECK
      byte[] tar = new byte[getBU().byteConsumed(newBitsSize)];
      //coordinate for copying to the temp array
      BitCoordinate ctar = createBitCoordinate();

      int width = 6 + chunkNumber;
      int[] frame = new int[2];
      BitCoordinate next = getComparator(chunkNumber);
      BitCoordinate c = getChunkCoordinate(chunkNumber);
      boolean useFlagBit = useFlagBit(chunkNumber);
      while (c.compare(next) == -1) {
         //no flag bit here
         readNextFrame(frame, c, width, oldLenBitSize, useFlagBit);
         if (flagbitOn) {
            //set it on
            if (frame[0] == 0) {
               getBU().copyBits(tar, ctar, 1, 1);
               getBU().copyBits(tar, ctar, frame[1], width);
            } else {
               getBU().copyBits(tar, ctar, 0, 1);
               getBU().copyBits(tar, ctar, frame[0], newLenbitsize);
               getBU().copyBits(tar, ctar, frame[1], width);
            }
         } else {
            //System.out.println("copying at " + ctar.toString() + " frame=" + frame[0] + ":" + frame[1]);
            //set if off and simply prints
            getBU().copyBits(tar, ctar, frame[0], newLenbitsize);
            getBU().copyBits(tar, ctar, frame[1], width);
         }
      }

      int start = getLastChunkBit(chunkNumber) + 1;
      getBU().shiftBits(data, NumOfBitsToBeAdded, start, getLastUsedBitLoaded(), true);
      lastusedbit += NumOfBitsToBeAdded;
      updateChunkCoordinates(chunkNumber + 1, getChunkBase(), NumOfBitsToBeAdded);
      getBU().copyBits(tar, data, createBitCoordinate(), getChunkCoordinate(chunkNumber), newBitsSize);
   }

   /**
    * Change chunk coordinate in the header of the chunk
    * @param chunk
    * @param c a loaded coordinate!!!
    */
   public void setChunkCoordinate(int chunk, BitCoordinate c) {
      BitCoordinate cc = createBitCoordinate();
      cc.setByteAndBit(getChunkHeaderOffsetLoaded(chunk), 0); 
      //System.out.println("chunk ="+chunk + " "+c.unmap() + " at " + cc.bytenum );
      getBU().copyBits(data, cc, c.getBitnum(), 4);
      getBU().copyBits(data, cc, c.getBytenum() - index, ITechOrderedIntToIntRun.DEFAULT_BYTE_POINTER_SIZE);
   }

   private void setChunkLengthBitSize(int chunkNumber, int newlenbitsize) {
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      off += ITechOrderedIntToIntRun.OFFSET_CHUNK_02_LENGTH_BITSIZE_1;
      data[off] = (byte) newlenbitsize;
   }

   /**
    * Set the data for the position "ordercount".
    * <br>
    * Removes the old data.
    * <br>
    * <br>
    * @param ordercount position of the value
    * @param data
    */
   protected void setData(int ordercount, int data) {
      if (dataBitSize == 1) {
         setDataBitSize(getBU().widthInBits(data));
      }
      BitCoordinate c = getChunkCoordinate(0);
      c.forward((ordercount - 1) * defDataBitSize);
      getBU().copyBits(this.data, c, data, defDataBitSize);
   }

   /**
    * Will be called if automatic databitSize is ON.
    * <br>
    * <br>
    * Updates the bit size of all values<br>
    * <br>
    * 
    * @param newDataBitSize
    * @param oldvalue
    */
   protected void setDataBitSize(int newDataBitSize) {
      int oldvalue = defDataBitSize;
      if (newDataBitSize > oldvalue) {
         int diff = newDataBitSize - oldvalue;
         int size = getSize();
         //expand bit size
         BitCoordinate c = getChunkCoordinate(0);
         ensureBufferCapacity(diff * size);
         //
         getBU().expandBitSize(data, c, getSize(), 0, oldvalue, 0, newDataBitSize, getLastUsedBitLoaded());
         lastusedbit += (diff * size);
         setDataHeaderBitSize(newDataBitSize);
         //update the lastusedbit
      }
   }

   /**
    * 1)remove buffer bytes at the end of the array.
    * 2)copy volatile values in the array
    * return the number of bytes trimmed
    *
    */
   //   public void trim() {
   //	 int lb = _offset + getLastUsedByte();
   //	 int size = _len - getLastUsedByte();
   //	 IntUtils.writeIntBE(_data, OFFSET_HEADER_LAST_USED_BIT, _lastusedbit);
   //	 _data = MUtils.decreaseCapacity(_data, size, lb);
   //	 _len -= size;
   //   }

   public void setDataHeaderBitSize(int val) {
      defDataBitSize = (byte) val;
      data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_02_DATA_BITSIZE1] = (byte) val;
   }

   /**
    * Update the chunk flag bit
    * @param chunkNumber
    * @param newBitsSize
    * @param bitsUsedWithOutFlag
    * @param 
    * on = true, currently OFF, set it ON
    * on = false, currently ON, set it OFF
    */
   private void setFlagBit(int chunkNumber, int newBitsSize, int bitstoremove, boolean on) {
      int ls = getChunkLengthBitSize(chunkNumber);
      this.setChunkBitsSize(chunkNumber, newBitsSize, bitstoremove, on, ls, ls);
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      if (on) {
         data[off + ITechOrderedIntToIntRun.OFFSET_CHUNK_03_FLAGBIT_1] = 1;
      } else {
         data[off + ITechOrderedIntToIntRun.OFFSET_CHUNK_03_FLAGBIT_1] = 0;
      }
   }

   /**
    * Set Main Header Switch. Does NOT change existing flag bit
    * true will enable the use of the flag bit
    * false will completely disable
    * This method only
    * @param val
    */
   public void setMainHeaderFlagBit(boolean val) {
      if (val)
         data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_05_HEADER_FLAG_BIT1] = 1;
      else
         data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_05_HEADER_FLAG_BIT1] = 0;

   }

   public void setNumBelow(int chunkNumber, int numBelow) {
      int off = getChunkHeaderOffsetLoaded(chunkNumber);
      off += ITechOrderedIntToIntRun.OFFSET_CHUNK_06_NUM_BELOW_3;
      IntUtils.writeInt24BE(data, off, numBelow);
   }

   /**
    * Replace the datas for that value
    * If value is not in the array. throw an exception
    */
   public void setValueDatas(int value, int[] datas) {
      if (datas == null || datas.length == 0)
         return;
      if (!hasValue(value)) {
         throw new RuntimeException("value " + value + " cannot be set");
      }
      int ordercount = getValueOrderCount(value);
      setData(ordercount, datas[0]);
   }
   
   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerOrderedIntToIntRun");
      toStringPrivate(dc);
      super.toString(dc.sup());
      
   }

   private void toStringPrivate(Dctx sb) {
      sb.nl();
      sb.append("data.length=" + data.length + " _len=" + getLength() + " offset=" + index);
      sb.nl();
      sb.append("lastusedbitloaded=" + getLastUsedBitLoaded() + " lastusedbit=" + lastusedbit + " lastusedbyte=" + getLastUsedByte() + " lastloadedbyte=" + getLastLoadUsedByte());
      sb.nl();
      sb.append("chunkBase=" + getChunkBase());
      sb.append(" size=" + getSize());
      sb.append(" X=" + getX());
      sb.append(" Y=" + getY());
      sb.nl();
      sb.append("orderbyte=" + data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_01_HEADER_ORDER1]);
      sb.append(" useFlagbit=" + useFlagBit() + "=" + data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_05_HEADER_FLAG_BIT1]);
      sb.append(" databitsize=" + data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_02_DATA_BITSIZE1]);
      sb.append(" autodatabyte=" + data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_03_DATABITSIZE_FLAG1]);
      sb.nl();
      BitCoordinate c = getChunkCoordinate(0);
      int start = c.unmap();
      int lastbit = getLastChunkBit(0);
      sb.append("Data Chunk coord=" + c.getBytenum() + ":" + c.getBitnum() + "\t (" + start + "-" + lastbit + "=" + (lastbit - start) + ")");
      int bytecons = getBU().byteConsumed((lastbit - start));
      StringUtils su = pdc.getUCtx().getStrU();
      sb.append("Data Size=" + su.prettyStringMem(bytecons) + " Header Size=" + su.prettyStringMem(getLastUsedByte() - bytecons));
      sb.nl();
      sb.append("Offsets :");
      sb.nl();
      sb.append("base bits :\t  chunk: p=headerOffset coord=1stValuePosition \t (bitstart-lastbitofchunk=diff ) maxlen= x=lones y=bigframes numberbelow= flag=chunkuse o flagbit" + " FLAGBIT_CHANGE_THRESHOLD=" + FLAGBIT_CHANGE_THRESHOLD);
      sb.nl();
      for (int chunkNumber = 1; chunkNumber <= getChunkBase(); chunkNumber++) {
         int width = 6 + chunkNumber;
         lastbit = getLastChunkBit(chunkNumber);
         int off = getChunkHeaderOffsetLoaded(chunkNumber);
         int belownum = getNumBelow(chunkNumber);
         int x = getNumLoners(chunkNumber);
         int y = getNumFrames(chunkNumber);
         boolean useFlagBit = useFlagBit(chunkNumber);
         int lenbitsize = getChunkLengthBitSize(chunkNumber);
         c = getChunkCoordinate(chunkNumber);
         start = c.unmap();
         sb.append(width + " bits :\t  " + chunkNumber + ": p=" + off + " coord=" + c.getBytenum() + ":" + c.getBitnum() + "\t (" + start + "-" + lastbit + "=" + (lastbit - start) + ")");
         sb.append(" len=" + lenbitsize + " x=" + x + " y=" + y + " nb=" + belownum + " flag=" + useFlagBit + "\t -- ");
         int[] frame = new int[2];
         BitCoordinate next = createBitCoordinate();
         next.map(lastbit);
         while (c.compare(next) == -1) {
            readNextFrame(frame, c, width, lenbitsize, useFlagBit);
            sb.append(frame[0] + ":" + frame[1]);
            if (c.unmap() < lastbit)
               sb.append(',');
         }
         sb.nl();
      }
      if (dataBitSize != 0) {
         BitCoordinate ca = getDataCoordinate();
         int bits = getDataBitSize();
         int size = getSize();
         sb.append("Data Values #" + size + " = ");
         for (int i = 0; i < size; i++) {
            sb.append(getBU().readBits(data, ca, bits) + ",");
         }
      }
      sb.nl();
      sb.append("Values Data : ");
      try {
         int[] vd = getValuesDatas();
         for (int i = 0; i < vd.length; i += 2) {
            sb.append(vd[i]);
            sb.append("-");
            sb.append(vd[i + 1]);
            sb.append(" ");
         }
      } catch (Exception e) {
         sb.append("Exception while reading Values and Data " + e.getClass().getName() + " " + e.getMessage());
      }
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerOrderedIntToIntRun");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

   /**
    * Method called when data is modified. Chunk coordinates changes and each chunk headers
    * are updated to reflect this.
    * <br>
    * For all chunks such that startchunk <= chunks <= endchunk
    * it will move the coordinates
    * @param startchunk
    * @param endchunk
    * @param valuechange
    * no tick
    */
   private void updateChunkCoordinates(int startchunk, int endchunk, int valuechange) {
      //always update the Data Coordinate
      if (startchunk != 0) {
         //do it anyways for datacoord
         BitCoordinate dataCoords = getDataCoordinate();
         if (valuechange < 0) {
            dataCoords.rewind(0 - valuechange);
         } else {
            dataCoords.forward(valuechange);
         }
         setChunkCoordinate(0, dataCoords);
      }
      for (int i = startchunk; i <= endchunk; i++) {
         BitCoordinate c = getChunkCoordinate(i);
         if (valuechange < 0) {
            c.rewind(0 - valuechange);
         } else {
            c.forward(valuechange);
         }
         setChunkCoordinate(i, c);
      }
   }

   /**
    * Method updates the bitsize of all the length fields for the given chunk
    * It updates the Chunk Header as well.
    * @param chunkNumber
    * @param newlenbitsize
    */
   private void updateLengthSize(int chunkNumber, int oldlengthbitsize, int newlenbitsize) {
      //compute the number of length inside
      int x = getNumLoners(chunkNumber);
      int y = getNumFrames(chunkNumber);
      boolean useFlagBit = useFlagBit(chunkNumber);

      //first compute the consumption of new length and old combined with flag bit
      int newBitSize = 0;
      int oldbitsize = 0;
      if (useFlagBit) {
         newBitSize = ((newlenbitsize) * y) + (x + y);
         oldbitsize = ((oldlengthbitsize) * y) + (x + y);
         // a =0 x=oldlength b=chunkNumber+6 y=newlength
      } else {
         newBitSize = (x + y) * newlenbitsize;
         oldbitsize = (x + y) * oldlengthbitsize;
      }
      int width = 6 + chunkNumber;
      //add the bit consumption of the value parts of the frames
      newBitSize += ((x + y) * width);
      oldbitsize += ((x + y) * width);
      //System.out.println("oldbitsize=" + oldbitsize);
      setChunkBitsSize(chunkNumber, newBitSize, newBitSize - oldbitsize, useFlagBit, newlenbitsize, oldlengthbitsize);
      //
      setChunkLengthBitSize(chunkNumber, newlenbitsize);
   }

   /**
    * Return the value of the main header flagbit switch
    * false if off, true if on
    * @return
    */
   public boolean useFlagBit() {
      return data[index + ITechOrderedIntToIntRun.OITI_RUN_OFFSET_05_HEADER_FLAG_BIT1] == 1;
   }

   /**
    * Return true if flagbit is enabled with that chunk
    * false is currently disabled
    * <br>
    * @param chunkNumber
    * @return boolean
    * @see ITechOrderedIntToIntRun#OFFSET_CHUNK_03_FLAGBIT_1
    */
   public boolean useFlagBit(int chunkNumber) {
      //check flagbit for chunk
      int offset = getChunkHeaderOffsetLoaded(chunkNumber);
      if (data[offset + ITechOrderedIntToIntRun.OFFSET_CHUNK_03_FLAGBIT_1] == 1) {
         return true;
      } else {
         return false;
      }
   }

   /**
    * 
    * @param width
    * @param lengthsize
    * @param useFlagBit
    * @param c
    * @param len length of frame
    * @param framestart
    */
   private void writeFrame(int width, int lengthsize, boolean useFlagBit, BitCoordinate c, int len, int framestart) {
      if (useFlagBit) {
         if (len == 0) {
            getBU().copyBit(data, c, 1);
            getBU().copyBits(data, c, framestart, width);
            return;
         } else {
            getBU().copyBit(data, c, 0);
         }
      }
      getBU().copyBits(data, c, len, lengthsize);
      getBU().copyBits(data, c, framestart, width);
   }

   private void writeFrame(int width, int lengthsize, boolean useFlagBit, BitCoordinate c, int[] frame) {
      writeFrame(width, lengthsize, useFlagBit, c, frame[0], frame[1]);
   }

}
