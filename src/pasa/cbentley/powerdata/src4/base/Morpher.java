package pasa.cbentley.powerdata.src4.base;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsRun;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkTrieData;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.integer.PowerIntToIntsRun;
import pasa.cbentley.powerdata.src4.trie.PowerCharTrie;
import pasa.cbentley.powerdata.src4.trie.PowerTrieLink;

public class Morpher {

   private PDCtx pdc;

   public Morpher(PDCtx pdc) {
      this.pdc = pdc;
   }
   

   /**
    * From a normal Trie, create another Trie using the T9 function
    * A T9 string has a number of integers who are node ids in the original Trie
    * 
    * Trie is in Running  MODE
    */
   public IPowerLinkTrieData getT9Trie(PowerCharTrie ct) {
      //IntToStrings its = ct.getAllStringsPerf();
      IntToStrings its = pdc.getTrieU().getPrefixedStrings(ct, "");
      PowerCharTrie t9 = new PowerCharTrie(pdc);
      ByteObjectManaged ltdTech = pdc.getTechFactory().getPowerTrieLinkTechFromTrie(t9);
      IPowerLinkTrieData link = new PowerTrieLink(pdc, ltdTech);

      System.out.println("Building T9 in Build Mode for " + its.nextempty + " words");
      for (int i = 0; i < its.nextempty; i++) {
         String my = its.strings[i];
         char[] c = StringUtils.getT9String(my);
         link.addIntToKeyString(its.ints[i], c, 0, c.length);

      }
      //     for (int i = 0; i < its.nextempty; i++) {
      //        char[] c = StringUtils.getT9String(its.strings[i]);
      //        t9.addData(c,0,c.length, its.ints[i]);
      //      }
      return link;
   }


   /**
    * Compression goal:
    * Allow for different configurations to compress effectively
    * 
    * 1: many small values (between 1 and 32) : IntBag for rows
    * 2: few small values
    * 
    * [Pointer Table]
    * 
    * @param ints
    * @param firstEmpties
    * @param offset
    * @param lastused
    * @return
    */
   public PowerIntToIntsRun createPowerIntToIntsRun(int[][] ints, int[] firstEmpties, int offset, int lastused) {
      int numChunks = lastused - offset + 1;
      if (lastused < offset) {
         numChunks = 0;
      }
      //arraySizes is at least one
      int arraySizes = numChunks;
      int numOfInts = 0;
      int maxInt = 0;
      int chunkwithNegatives = 0;
      boolean negativeValue = false;
      //maximum number of integer in a row
      int maxChunkIntSerie = 0;
      int[] maxIntSeries = new int[arraySizes];
      boolean[] negatives = new boolean[arraySizes];
      boolean useSingleValueFlag = false;
      //counting episode
      for (int i = 0; i < arraySizes; i++) {
         if (ints[i] == null)
            continue;
         //for each row
         boolean chunkNegativeValue = false;
         //compute the maximum chunkLength
         if (firstEmpties[i] > maxChunkIntSerie) {
            maxChunkIntSerie = firstEmpties[i];
         }
         //compute the maximum and the total number
         int max = firstEmpties[i];
         if (max == 1)
            useSingleValueFlag = true;
         int maxIntRow = 0;
         for (int j = 0; j < max; j++) {
            int test = Math.abs(ints[i][j]);
            if (test > maxInt)
               maxInt = test;
            if (test > maxIntRow)
               maxIntRow = test;
            if (ints[i][j] < 0)
               chunkNegativeValue = true;
            numOfInts++;
         }
         maxIntSeries[i] = maxIntRow;
         //keep track of negatives
         if (chunkNegativeValue) {
            negativeValue = true;
            negatives[i] = true;
            chunkwithNegatives++;
         }
      }
      //compute the maximum number of bits needed to store all ints
      int bitIntSize = BitUtils.widthInBits(maxInt);
      // if zero value is alone, we need at least a length of 1
      if (bitIntSize == 0)
         bitIntSize = 1;
      if (negativeValue) {
         bitIntSize++;
      }
      int dataBitSize = (bitIntSize * numOfInts);
      //total size of data
      int dataSize = (dataBitSize / 8) + 1;

      //compute the cheapest
      // CASE 1a: pointers first bits
      // CASE 1b: pointers first bytes
      // CASE 2a: data first bits
      // CASE 2b: data first bytes

      // in the B case, integers are stored in bytes 1,2,3 or 4

      //cases b becomes better when pointer
      byte[] data = new byte[dataSize];
      BitCoordinate c = new BitCoordinate(pdc.getUC());
      int[] chunkOffsetsByte = new int[arraySizes];
      int[] chunkOffsetsBit = new int[arraySizes];
      //compute where each chunks will start
      for (int i = 0; i < arraySizes; i++) {
         chunkOffsetsByte[i] = c.getBytenum();
         chunkOffsetsBit[i] = c.getBitnum();
         if (ints[i] == null)
            continue;
         int max = firstEmpties[i];
         //we do not copy the number of elements because this value can be deduced by knowing next pointer's offset
         // Data protocol
         // Two flag bit: Bit Flag 0 is for the presence of intSize length header
         // this header is written if the gain in bits is positive a total maxIntSize = 16
         // and we have 3 integers of maxIntSize of 15 written on 5 bits + 3 * 15 = 50bits, bigger than 3*16=48bits
         // FlagBit Sequential for the presence of Sequential int dataprotocol
         // FlagBit for Long values: in this case, the bitlength is computed thanks to the end of chunk pointer
         // if bits available are smaller than maxIntBitSize
         for (int j = 0; j < max; j++) {
            if (negativeValue) {
               //copy the sign bit then the size of the integer minus the sign bit which has been incr above
               c.tick();
               BitUtils.copyBit(data, c, ints[i][j], 32);
               BitUtils.copyBits(data, c, Math.abs(ints[i][j]), bitIntSize - 1);
               c.tack();
               int signbit = BitUtils.readBit(data, c);
               int val = BitUtils.readBits(data, c, bitIntSize - 1);
               if (signbit == 1) {
                  val = (0 - val);
               }
               if (ints[i][j] != val) {
                  throw new RuntimeException("bitIntSize=" + bitIntSize + " " + +ints[i][j] + " != " + val);
               }
            } else {
               BitUtils.copyBits(data, c, ints[i][j], bitIntSize);
            }
         }
      }
      int headerSize = ITechIntToIntsRun.ITIS_RUN_BASIC_SIZE;
      int lastdatabitposition = (headerSize * 8) + (c.getBytenum() * 8 + c.getBitnum());
      //now create the PointerTable
      //compute the size of a pointer
      int maxDataOffset = headerSize;
      if (numChunks > 0) {
         maxDataOffset += chunkOffsetsByte[chunkOffsetsByte.length - 1];
      }
      int bitPointerSize = BitUtils.widthInBits(maxDataOffset * 8);
      // 4 is the number of bits to code for 8 possibilites i.e. the 8 bits in a byte
      int pointerChunk = bitPointerSize;
      //total number of bits
      int bitsNumber = pointerChunk * numChunks;
      int byteNumber = (bitsNumber / 8) + 1;
      byte[] pointers = new byte[byteNumber];
      //build pointer data
      for (int i = 0; i < arraySizes; i++) {
         BitUtils.copyBits(pointers, c, chunkOffsetsByte[i] * 8 + chunkOffsetsBit[i], bitPointerSize);
      }
      int lastbitpostion = (headerSize * 8) + (data.length * 8) + (c.getBytenum() * 8 + c.getBitnum());
      //aggregate all the data
      //Headers
      int sdataSize = +pointers.length + data.length;
      ByteObjectManaged bo = pdc.getBOC().getByteObjectManagedFactory().create(headerSize, sdataSize);
      byte[] fullData = bo.getByteObjectData();

      int pointerTableOffSet = headerSize + data.length;

      bo.set1(ITechIntToIntsRun.ITIS_RUN_OFFSET_02_POINTER_BIT_SIZE1, bitPointerSize);
      bo.set1(ITechIntToIntsRun.ITIS_RUN_OFFSET_03_INT_BIT_SIZE1, bitIntSize);
      bo.set2(ITechIntToIntsRun.ITIS_RUN_OFFSET_04_NUM_CHUNKS2, numChunks);
      bo.set2(ITechIntToIntsRun.ITIS_RUN_OFFSET_05_TABLE4, pointerTableOffSet);
      bo.set2(ITechIntToIntsRun.ITIS_RUN_OFFSET_06_LAST_POINTER4, lastbitpostion);
      bo.set2(ITechIntToIntsRun.ITIS_RUN_OFFSET_07_LAST_DATA_BIT4, lastdatabitposition);

      if (negativeValue) {
         bo.setFlag(ITechIntToIntsRun.ITIS_RUN_OFFSET_01_FLAG1, ITechIntToIntsRun.ITIS_RUN_FLAG_1_NEGATIVE, true);
      }
      // then copy data
      System.arraycopy(data, 0, fullData, headerSize, data.length);
      // then copy pointers
      System.arraycopy(pointers, 0, fullData, pointerTableOffSet, pointers.length);

      PowerIntToIntsRun dia = new PowerIntToIntsRun(pdc, bo);

      return dia;
   }

}
