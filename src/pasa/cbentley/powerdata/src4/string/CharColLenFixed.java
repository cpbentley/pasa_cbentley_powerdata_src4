package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColLenFixed;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Generic char collection with a given length.
 * <br>
 * <br>
 * When a char array takes too much room, it is trimmed with a pointer to another chunk.
 * <br>
 * If that's an option.
 * <br>
 * <br>
 * String are chunked into chunks of x bytes.
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class CharColLenFixed extends PowerCharCol implements IPowerCharCollector, ITechCharColLenFixed {
   private byte[] data;

   private int    length;

   private int    offset;

   int            plane;

   int            size;

   public CharColLenFixed(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      size = tech.get1(CCFIXED_OFFSET_02_SIZE1);
      plane = tech.get1(CCFIXED_OFFSET_03_BASE_PLANE1);
   }

   public int addChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int addChars(String s) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void addPointerUser(IPointerUser pointerUser) {
      // TODO Auto-generated method stub

   }

   public void appendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   public int copyChars(int pointer, char[] c, int offset) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int find(char[] str, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getBiggestWordSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public char getChar(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Enumeration on the Strings
    * @return
    */
   public IPowerEnum getCharEnum(Object param) {
      return null;
   }

   public char[] getChars(int pointer) {
      int offset = this.offset + size * pointer;
      int headerByte = data[offset];
      return StringUtils.getCharArrayPlane(data, offset + 1, plane, new int[2]);
   }

   public char[] getChars(int[] charp) {
      // TODO Auto-generated method stub
      return null;
   }

   public String getKeyStringFromPointer(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getLen(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public int[] getNewPointers() {
      // TODO Auto-generated method stub
      return null;
   }

   public int getPointer(char[] chars) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getPointer(char[] chars, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getPointer(String str) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public boolean hasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean hasChars(String str) {
      // TODO Auto-generated method stub
      return false;
   }

   protected int insideAddChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected void insideAppendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   protected int insideCopyChars(int pointer, char[] c, int offset) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideFind(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected char insideGetChar(int outsidePointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected char[] insideGetChars(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   protected char[] insideGetChars(int[] charp) {
      // TODO Auto-generated method stub
      return null;
   }

   protected String insideGetKeyStringFromPointer(int pointer) {
      return new String(this.insideGetChars(pointer));
   }

   protected int insideGetLen(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideGetPointer(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideGetSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int[][] insideGetSizes() {
      // TODO Auto-generated method stub
      return null;
   }

   protected boolean insideHasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
   }

   protected int insideRemove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideSetChars(int pointer, char[] d, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public boolean isValid(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   public int remove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void search(CharSearchSession css) {
      // TODO Auto-generated method stub

   }

   public byte[] serializePack() {
      // TODO Auto-generated method stub
      return null;
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return null;
   }

   public int setChars(int pointer, char[] d, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public String toString(String nl) {
      // TODO Auto-generated method stub
      return null;
   }

}
