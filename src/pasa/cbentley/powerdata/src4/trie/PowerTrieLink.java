package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.powerdata.spec.src4.guicontrols.IPrefixSearchSession;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechStringLinker;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechTrieLink;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToIntArray;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkTrieData;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.string.PowerLinkStringToIntArray;

/**
 * 
 * @author Charles Bentley
 *
 */
public class PowerTrieLink extends PowerBuildBase implements IPowerLinkTrieData, ITechTrieLink {


   /**
    * For each data type, we have an {@link IPowerLinkStringToIntArray}
    * <br>
    * They will load the {@link IPowerCharTrie} by loading the reference ID.
    * <li> {@link PowerTrieLink}
    * <li> Create {@link IPowerCharTrie}
    * <li> Each {@link IPowerLinkStringToIntArray} loads the Trie
    * <br>
    * <br>
    * Defs to {@link PowerLinkStringToIntArray}
    */
   protected IPowerLinkStringToIntArray[] datas;

   protected IPowerCharTrie               trie;

   public PowerTrieLink(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerTrieLinkTechDefault());
   }

   public PowerTrieLink(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmptyConstructor();
   }

   public int addChars(char[] c, int offset, int len) {
      return trie.addChars(c, offset, len);
   }

   public int addChars(String s) {
      return trie.addChars(s);
   }

   public void addIntToKeyString(int data, char[] chars, int offset, int len) {
      datas[0].addIntToKeyString(data, chars, offset, len);
   }

   public void addIntToKeyString(int data, String str) {
      datas[0].addIntToKeyString(data, str);
   }

   public void addIntToKeyStringType(int data, char[] chars, int offset, int len, int type) {
      datas[type].addIntToKeyString(data, chars, offset, len);
   }

   public void addIntToKeyStringType(int data, String str, int type) {
      datas[type].addIntToKeyString(data, str);
   }

   public void addPointerUser(IPointerUser pointerUser) {
      trie.addPointerUser(pointerUser);
   }

   public void appendChars(int pointer, StringBBuilder sb) {
      trie.appendChars(pointer, sb);
   }

   public int copyChars(int pointer, char[] c, int offset) {
      return trie.copyChars(pointer, c, offset);
   }

   public int countWords(int wordid) {
      return trie.countWords(wordid);
   }

   public int countWords(String prefix) {
      return trie.countWords(prefix);
   }

   public int find(char[] str, int offset, int len) {
      return trie.find(str, offset, len);
   }

   public int getBiggestWordSize() {
      return trie.getBiggestWordSize();
   }

   public char getChar(int pointer) {
      return trie.getChar(pointer);
   }

   public char[] getChars(int pointer) {
      return trie.getChars(pointer);
   }

   public char[] getChars(int[] pointers) {
      return trie.getChars(pointers);
   }

   /**
    * Enumeration on the Strings
    * @return
    */
   public IPowerEnum getEnumOnCharCol(int type, Object param) {
      return trie.getEnumOnCharCol(type, param);
   }

   public IPowerEnum getEnumOnLinkStringToInt(int type, Object param) {
      return trie.getEnumOnCharCol(type, param);
   }

   public int getIntFromKeyString(char[] c, int offset, int len) {
      return datas[0].getIntFromKeyString(c, offset, len);
   }

   public int getIntFromKeyString(String s) {
      return datas[0].getIntFromKeyString(s);
   }

   public int getIntFromPointer(int pointer) {
      return datas[0].getIntFromPointer(pointer);
   }

   public int[] getIntsFromKeyString(String str) {
      return datas[0].getIntsFromKeyString(str);
   }

   public int[] getIntsFromKeyStringTyped(String str, int type) {
      return datas[type].getIntsFromKeyString(str);
   }

   public int[] getIntsFromPointer(int pointer) {
      return datas[0].getIntsFromPointer(pointer);
   }

   public int[] getIntsFromPointerType(int pointer, int type) {
      return datas[type].getIntsFromPointer(pointer);
   }

   public String getKeyStringFromPointer(int pointer) {
      return trie.getKeyStringFromPointer(pointer);
   }

   public int getLen(int pointer) {
      return trie.getLen(pointer);
   }

   /**
    * This object can morph without changing its reference....
    * <br>
    */
   public Object getMorph(MorphParams p) {
      trie = (IPowerCharTrie) trie.getMorph(p);
      for (int i = 0; i < datas.length; i++) {
         datas[i] = (IPowerLinkStringToIntArray) datas[i].getMorph(p);
      }
      return this;
   }

   public int getPointer(char[] chars) {
      return trie.getPointer(chars);
   }

   public int getPointer(char[] chars, int offset, int len) {
      return trie.getPointer(chars, offset, len);
   }

   public int getPointer(String str) {
      return trie.getPointer(str);
   }

   public int getPointerFromKeyString(String str) {
      return datas[0].getPointerFromKeyString(str);
   }

   public IntToStrings getPrefixed(String prefix) {
      return trie.getPrefixed(prefix);
   }

   public int getPrefixPointer(String str, ICharComparator tss) {
      return trie.getPrefixPointer(str, tss);
   }

   public int getSize() {
      return trie.getSize();
   }

   public int[][] getSizes() {
      return trie.getSizes();
   }

   public Object getStringStructure() {
      return trie;
   }

   public boolean hasChars(char[] c, int offset, int len) {
      return trie.hasChars(c, offset, len);
   }

   public boolean hasChars(String str) {
      return trie.hasChars(str);
   }

   public boolean hasData(String str, int data, int type) {
      return datas[type].isIntLinkedToKeyString(data, str);
   }

   public boolean hasKeyString(String str) {
      return trie.hasChars(str);
   }

   public int incrementIntFromKeyString(int incr, char[] c, int offset, int len) {
      return datas[0].incrementIntFromKeyString(incr, c, offset, len);
   }

   public int incrementIntFromKeyString(int incr, String s) {
      return datas[0].incrementIntFromKeyString(incr, s);
   }

   private void initEmptyConstructor() {
      int trieID = get2(TRIEDATA_OFFSET_03_REF_TRIE2);
      trie = (IPowerCharTrie) byteCon.getAgentFromRefOrCreate(trieID, IPowerCharTrie.INT_ID);
      int nums = get2(TRIEDATA_OFFSET_02_NUMTYPES2);
      datas = new IPowerLinkStringToIntArray[nums];
      int pointer = get2(TRIEDATA_OFFSET_04_REF_LINKER2);
      for (int i = 0; i < nums; i++) {
         int lstID = get2(pointer + i * 2);
         IPowerLinkStringToIntArray lst = (IPowerLinkStringToIntArray) byteCon.getAgentFromRefOrCreate(lstID, IPowerLinkStringToIntArray.INT_ID);
         datas[i] = lst;
      }
   }

   public boolean isIntLinkedToKeyString(int data, String str) {
      return hasData(str, data, 0);
   }

   public boolean isIntLinkedToKeyStringTyped(int data, String str, int type) {
      return datas[type].isIntLinkedToKeyString(data, str);
   }

   public boolean isValid(int pointer) {
      return trie.isValid(pointer);
   }

   public boolean isWord(int pointer) {
      return trie.isWord(pointer);
   }

   public int linkIntToKeyString(int data, char[] c, int offset, int len) {
      return datas[0].linkIntToKeyString(data, c, offset, len);
   }

   public int linkIntToKeyString(int data, String s) {
      return datas[0].linkIntToKeyString(data, s);
   }

   public int remove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void removeKeyString(String str) {
      for (int i = 0; i < datas.length; i++) {
         datas[i].removeKeyString(str);
      }
   }

   public ISearchSession search(ByteObject param) {
      return trie.search(param);
   }

   public ISearchSession searchIndexOf(int frame) {
      return trie.searchIndexOf(frame);
   }

   public IPrefixSearchSession searchPrefix(int frame) {
      return trie.searchPrefix(frame);
   }

   public void serializeReverse() {
      if (hasData()) {
         initEmptyConstructor();
         int offset = TRIEDATA_BASIC_SIZE;
         for (int i = 0; i < datas.length; i++) {
            int refid = get2(offset);
            offset += 2;
            datas[i] = (IPowerLinkStringToIntArray) byteCon.getAgentFromRefOrCreate(refid, IPowerLinkStringToIntArray.INT_ID);
         }
      } else {
         initEmptyConstructor();
      }
   }

   /**
    */
   public ByteObjectManaged serializeTo(ByteController byteCon) {
      byte[] data = new byte[2 * datas.length];
      ByteObjectManaged bom = byteCon.serializeToUpdateAgentData(serializeRawHelper(data));
      trie.serializeTo(byteCon);
      bom.set2(TRIEDATA_OFFSET_03_REF_TRIE2, trie.getTech().getIDRef());
      int offset = TRIEDATA_BASIC_SIZE;
      for (int i = 0; i < datas.length; i++) {
         datas[i].serializeTo(byteCon);
         bom.set2(offset, datas[i].getTech().getIDRef());
         offset += 2;
      }
      return bom;
   }

   public int setChars(int pointer, char[] d, int offset, int len) {
      return trie.setChars(pointer, d, offset, len);
   }
}
