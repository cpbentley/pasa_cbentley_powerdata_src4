package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.core.src4.utils.ShortUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColBuild;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColRun;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.integer.PowerIntArrayRun;

/**
 * When 
 * @author Charles Bentley
 *
 */
public class BuildToRunConverter implements ITechCharColRun {

   public static final int   CHAR_COL_RUN_0_ = 0;

   private PowerIntArrayRun  allLettersBag;

   private int               alphaSize;

   private int               alphaStartSize;

   private PowerCharColBuild build;

   private PowerCharColBuild buildCleaned;

   private char[][]          charData;

   private int               lastused        = -1;

   /**
   * the size of the biggest set of length/letter set
   */
   private int               maxNumberLetter = 0;

   private final PDCtx       pdc;

   private int               planeCollector;

   /**
    * Function to be used when remix was done.
    * <br>
    * An old pointer of duplicates collector
    */
   private int[]             pointersBuildToRun;

   /**
    * Final Map of the [old pointer] to the new pointer in the run version.
    * <br>
    * <br>
    * 
    */
   private int[]             pointersOriginalToFinalRun;

   /**
    * Pointer link when char collector was cleaned i.e. duplicates were removed.
    * <br>
    * Pointer[original] = cleanedPointer.
    * <br>
    * <br>
    * The length of this array is bigger than BuildToRun
    */
   private int[]             pointersOrignalToCleaned;

   /**
    * Set to true when {@link PowerCharColBuild} is remixed to remove duplicates.
    */
   private boolean           remixDone;

   private PowerIntArrayRun  startLetterBag;

   public BuildToRunConverter(PDCtx pdc) {
      this.pdc = pdc;
   }

   protected byte[] copyChars(int[][] sizes, int[][][] mapping) {
      int bitSizeChar = BitUtils.widthInBits(alphaSize);
      int bitsConsumed = 0;
      for (int L = 1; L < sizes.length; L++) {
         bitsConsumed += ((L - 1) * sizes[L][0] * bitSizeChar);
      }
      //length pointer table
      byte[] data = new byte[BitUtils.byteConsumed(bitsConsumed)];
      BitCoordinate c = new BitCoordinate(pdc.getUCtx(), 0, 0);
      int count = 1;
      //COPY NOW FROM MAPPING
      for (int L = 1; L < mapping.length; L++) {
         //for all length/letter mappings. copy them
         for (int i = 1; i < mapping[L].length; i++) {
            int[] idToCopy = mapping[L][i];
            int[] positionMapping = allLettersBag.getPositionMapping();
            for (int j = 1; j <= idToCopy[0]; j++) {
               //id is the old pointer
               int id = idToCopy[j];
               //save old pointer
               pointersBuildToRun[id] = count;
               //copy data
               char[] string = charData[id];
               //start at 1 because we skip the first letter
               for (int k = 1; k < string.length; k++) {
                  //remove the plane
                  int val = ((string[k] >>> 0) & 0xFF);
                  int pos = positionMapping[val];
                  BitUtils.copyBits(data, c, pos, bitSizeChar);
               }
               count++;
            }
         }
      }
      return data;
   }

   /**
    * Packs all the collected data into the {@link CharCollectorRunLight} format.
    * <br>
    * <br>
    * 
    * @param sizes [0] [0] maxLengthNum [0] [1] num of words
    * @param maxLengthNum
    * @param mapping
    * @param data
    * @return
    */
   private byte[] createDataArrayLight(int[][] sizes, int[][][] mapping, byte[] data) {
      //////////////BUILD HEADER DATA

      int maxLengthNum = sizes[0][0];
      int firstFieldsByteSize = CHAR_RUN_OFFSET_05_LETTER_FIRST2;
      int numBitNumLength = BitUtils.widthInBits(maxLengthNum);
      int letterNumBits = BitUtils.widthInBits(maxNumberLetter);
      int maxLength = sizes.length - 1;
      //bits consumed by 
      int headerRowBitSize = (alphaStartSize * letterNumBits) + numBitNumLength;
      int letterTableBytes = BitUtils.byteConsumed(maxLength * headerRowBitSize);
      int intSequencesLength = allLettersBag.getLength() + startLetterBag.getLength();
      int headerTableBytes = firstFieldsByteSize + letterTableBytes + intSequencesLength;
      byte[] header = new byte[headerTableBytes];

      int numWords = sizes[0][1];
      //COPY HEADER FIELDS
      header[CHAR_RUN_OFFSET_00_PLANE1] = (byte) planeCollector;
      header[CHAR_RUN_OFFSET_01_LENGTH_BIT1] = (byte) BitUtils.widthInBits(maxLengthNum);
      header[CHAR_RUN_OFFSET_02_LETTER_BIT1] = (byte) letterNumBits;
      ShortUtils.writeShortBEUnsigned(header, CHAR_RUN_OFFSET_03_MAX_LENGTH2, maxLength);
      IntUtils.writeInt24BE(header, CHAR_RUN_OFFSET_04_NUM_WORD3, numWords);
      ShortUtils.writeShortBEUnsigned(header, CHAR_RUN_OFFSET_08_DATA_OFFSET2, header.length);

      int startLen = startLetterBag.getLength();
      //copy the structure containing the first letters
      System.arraycopy(startLetterBag.getMemory(), 0, header, CHAR_RUN_OFFSET_05_LETTER_FIRST2, startLen);
      //add the mapping for all letters
      System.arraycopy(allLettersBag.getMemory(), 0, header, CHAR_RUN_OFFSET_05_LETTER_FIRST2 + startLen, allLettersBag.getLength());
      //////////////

      ///////// COPY HEADER TABLES
      int bytenum = CHAR_RUN_BASIC_SIZE + startLetterBag.getLength() + allLettersBag.getLength();
      BitCoordinate ccc = new BitCoordinate(pdc.getUCtx(), bytenum, 0);
      for (int L = 1; L < sizes.length; L++) {
         int num = sizes[L][0];
         BitUtils.copyBits(header, ccc, num, numBitNumLength);
         //for all length/letter mappings. copy them
         for (int i = 1; i < mapping[L].length; i++) {
            BitUtils.copyBits(header, ccc, mapping[L][i][0], letterNumBits);
         }
      }

      byte[] last = pdc.getUCtx().getMem().increaseCapacity(header, data.length);
      //copy data
      System.arraycopy(data, 0, last, header.length, data.length);
      return last;
   }

   /**
    * 
    */
   protected void finalizePointerFunction() {
      if (remixDone) {
         //iterate over the cleaning pointer table
         for (int i = 0; i < pointersOrignalToCleaned.length; i++) {
            int cleanedPointer = pointersOrignalToCleaned[i];
            int runPointer = pointersBuildToRun[cleanedPointer];
            pointersOriginalToFinalRun[i] = runPointer;
         }
      } else {
         pointersOriginalToFinalRun = pointersBuildToRun;
      }
   }

   public IPowerCharCollector getCharRun(PowerCharColBuild build) {
      this.build = build;
      this.init();
      byte[] data = this.getRunLight1();
      ByteController byc = new ByteController(pdc.getBoc(), null, data);
      CharCollectorRunLight ccrl = (CharCollectorRunLight) byc.getAgentRoot();
      build.conversionTable = this.pointersOriginalToFinalRun;
      return ccrl;
   }

   /**
    * Will create a compact CharCollector with no duplicates.
    * <br>
    * the new pointers can be retrieved with the {@link #getNewPointer(int)} method
    * <br>
    * You give the old pointer
    * <br>
    * <br>
    * 
    * Scenario is a structure used CharColBuild. Now it converts it to Run. It needs the mapping of Build pointers to Run pointers.
    * Conversion tells if those pointers have changed.
    * <br>
    * <br>
    * Once the conversion is done, User must extra the conversion table.
    * <br>
    * <br>
    * 
    * POST the state of the collector is not changed
    * <br>
    * @return
    */
   public byte[] getRunLight1() {

      if (build.isDuplicateAllowed()) {
         //we have to remove the duplicates
         ByteObjectManaged tech = (ByteObjectManaged) build.getTech().clone();
         tech.setFlag(ITechCharColBuild.CHARCOL_OFFSET_01_FLAG1, ITechCharColBuild.CHARCOL_FLAG_3_DUPLICATES, false);
         PowerCharColBuild newb = new PowerCharColBuild(pdc, tech);
         build.addAllData(newb, pointersOrignalToCleaned);
         buildCleaned = newb;
         remixDone = true;
      } else {
         buildCleaned = build;
      }

      // index 0 : zeros lengths strings
      // index 1 : pointers to 1 length strings 
      // index i : pointers to i length strings
      int[][] sizes = buildCleaned.getSizes();
      //update alphabet information
      updateAlphaInfo();

      //compute the number of bits used to store one letter. all letters used

      // map like this [length][first letter position][current pointer]
      int[][][] mapping = new int[sizes.length + 1][alphaStartSize + 1][10];
      populateMapping(sizes, mapping);

      byte[] data = copyChars(sizes, mapping);
      finalizePointerFunction();
      byte[] last = createDataArrayLight(sizes, mapping, data);
      //search
      return last;

   }

   /**
    * 
    */
   private void init() {
      lastused = build.lastused;
      pointersBuildToRun = new int[lastused + 1];
      pointersOrignalToCleaned = new int[lastused + 1];
      pointersOriginalToFinalRun = new int[lastused + 1];
      remixDone = false;
   }

   private void populateMapping(int[][] sizes, int[][][] mapping) {
      int maxLetter = 0;
      int[] startPositionMapping = startLetterBag.getPositionMapping();
      //strings are sorted by length but now we have to sort them out by first letter
      //for all lengths (Trait 1)
      for (int L = 1; L < sizes.length; L++) {
         //for all lengths regroup first letters strings together
         // for all strings of length L, map them to their first letters
         for (int i = 1; i <= sizes[L][0]; i++) {
            //from the size pointer table, get the string ID
            int id = sizes[L][i];
            char[] string = charData[id];
            int firstLetterValue = string[0] & 0xFF;
            int position = startPositionMapping[firstLetterValue];
            int[] ar = addIntToArray(mapping[L][position], id);
            mapping[L][position] = ar;
         }
         //get maximum
         for (int i = 1; i < mapping[L].length; i++) {
            //number of words starting with the ith letter
            if (mapping[L][i][0] > maxLetter) {
               maxLetter = mapping[L][i][0];
            }
         }
         maxNumberLetter = maxLetter;

      }
   }

   /**
    * Adds the Integer to the array using the first integer as length
    * @param ar
    * @param value
    * @return
    */
   public int[] addIntToArray(int[] ar, int value) {
      if (ar[0] + 1 >= ar.length) {
         ar = pdc.getUCtx().getMem().increaseCapacity(ar, ar.length);
      }
      ar[0]++;
      ar[ar[0]] = value;
      return ar;
   }

   /**
    * We need to learn the number of different starting letters
    * @return 
    */
   public void updateAlphaInfo() {
      //compute the alphabet size, all the different letters
      int[] plane = new int[1];
      int[] startLetterMapping = new int[256];
      int[] allLetterMapping = new int[256];
      buildCleaned.getAlphaInfo(plane, startLetterMapping, allLetterMapping);
      //number of different letters
      int alphasize = 0;
      //this will be the base of chunk length
      int alphastartsize = 0;
      // holds the mapping for all letters
      allLettersBag = new PowerIntArrayRun(pdc);
      for (int i = 0; i < allLetterMapping.length; i++) {
         if (allLetterMapping[i] != 0) {
            allLettersBag.addInt(i);
         }
      }
      //bag for all the starting letters
      startLetterBag = new PowerIntArrayRun(pdc);
      for (int i = 0; i < startLetterMapping.length; i++) {
         if (startLetterMapping[i] != 0) {
            alphastartsize++;
            startLetterBag.addInt(i);
         }
         if (allLetterMapping[i] != 0) {
            alphasize++;
         }
      }
      alphaSize = alphasize;
      alphaStartSize = alphastartsize;
      planeCollector = plane[0];
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "BuildToRunConverter");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "BuildToRunConverter");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return pdc.getUCtx();
   }

   //#enddebug

}
