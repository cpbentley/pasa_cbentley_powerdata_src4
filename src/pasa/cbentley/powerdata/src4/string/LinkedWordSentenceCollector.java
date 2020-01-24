package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.powerdata.spec.src4.guicontrols.IPrefixSearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharCol;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;


/**
 * Collects Strings and cut the strings into chunks and store the chunk IDs.
 * <br>
 * For storing long sentences, this is a good deal.
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class LinkedWordSentenceCollector extends PowerCharCol implements ITechCharCol, IPowerCharCollector {

   /**
    * Cutted words
    */
   IPowerCharCollector words;
   
   public LinkedWordSentenceCollector(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   
   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   
   public int addChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public IPrefixSearchSession searchPrefix(int frame) {
      // TODO Auto-generated method stub
      return null;
   }


   
   public int addChars(String s) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int copyChars(int pointer, char[] c, int offset) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int find(char[] str, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public char getChar(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public char[] getChars(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   
   public void appendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub
      
   }

   
   public char[] getChars(int[] pointers) {
      // TODO Auto-generated method stub
      return null;
   }

   
   public int getLen(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int getBiggestWordSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int getPointer(char[] chars) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int getPointer(String str) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int getPointer(char[] chars, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int getSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public String getKeyStringFromPointer(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   
   public IPowerEnum getEnumOnCharCol(int type, Object param) {
      // TODO Auto-generated method stub
      return null;
   }

   
   public boolean hasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
   }

   
   public boolean hasChars(String str) {
      // TODO Auto-generated method stub
      return false;
   }

   
   public int[][] getSizes() {
      // TODO Auto-generated method stub
      return null;
   }

   
   public boolean isValid(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   
   public int remove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int setChars(int pointer, char[] d, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public void addPointerUser(IPointerUser pointerUser) {
      // TODO Auto-generated method stub
      
   }

   
   protected String insideGetKeyStringFromPointer(int pointer) {
      // TODO Auto-generated method stub
      return null;
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

   
   protected int insideGetLen(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected int insideGetPointer(char[] c, int offset, int len) {
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

}
