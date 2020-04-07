package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.byteobjects.src4.sources.MemorySource;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharTrieTable;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechPointerStruct;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.spec.src4.spec.CharColUtilz;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.string.CharAggregateSearch;
import pasa.cbentley.powerdata.src4.string.PowerCharColAggregate;
import pasa.cbentley.powerdata.src4.utils.TableTrieFunction;

/**
 * This kind of {@link IPowerCharTrie} allows to load in memory only parts of the whole data set.
 * <br>
 * <br>
 * Trie aggregates may store capital letter words in a different trie.
 * <br>
 * Special kind of {@link PowerCharColAggregate}. Because we know the inner structure of the different
 * types. It is a b c d e f.
 * <br>
 * So we override
 * <br>
 * Therefore based on the TrieID,
 * <br>
 * Tries can be loaded in chunks.
 * The {@link ByteController} loads based on the reference of the mother trie and the ID of the trie.
 * The {@link PowerCharTrieTable} header identifies the {@link MemorySource}.
 * 
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class PowerCharTrieTable extends PowerCharColAggregate implements IPowerCharTrie, ITechCharTrieTable, IPointerUser {
   /**
    * Enumeration on the Strings
    * @return
    */
   public IPowerEnum getCharEnum(Object param) {
      return null;
   }

   protected IPowerEnum getEnumOnPointers() {
      return null;
   }



   /**
    * Could be Build or Run
    */
   public IPowerCharCollector  charco;

   /**
    * Each instance 
    * <br>
    * <br>
    * The maximum bit size {@link ITechPointerStruct#PS_OFFSET_03_BITSIZE1}
    * is used as the bitsize for this class.
    */
   protected IPowerCharTrie[]  charTrieTable;

   /**
    * maps an incoming word with a Trie ID
    * if null, the mapping cache is used instead. 
    * By default, TrieID is character's first 8 bits
    */
   protected TableTrieFunction mappingFunct;

   /**
    * the number of bits used to code the Trie ID in the Modded TrieNodes
    */
   protected int               trieIDBitLength;

   public PowerCharTrieTable(PDCtx pdc, ByteObjectManaged tech) {
      this(pdc, tech, null);
   }

   public PowerCharTrieTable(PDCtx pdc, ByteObjectManaged tech, TableTrieFunction func) {
      super(pdc, tech);
      mappingFunct = func;
      initConstr();
   }

   private void initConstr() {
      ByteObjectManaged charColTech = this.getTechSub(1);
      ByteController bc = getByteControllerCreateIfNull();
      ByteObjectManaged baseTrieTech = this.getTechSub(0);

      int groupRef = get2(AGENT_OFFSET_08_REF_ID2);

      int refCharCo = get2(CTRIE_OFFSET_06_REF_CHARCOL2);
      charco = (IPowerCharCollector) bc.getAgentFromRefOrCreate(refCharCo, charColTech, IPowerCharCollector.INT_ID);

      int numArray = get2(CTRIETABLE_OFFSET_03_NUM_TRIES2);
      charTrieTable = new IPowerCharTrie[numArray];
      int offsetRef = getDataOffsetStartLoaded();
      //if loaded them all
      if (hasFlag(CTRIETABLE_OFFSET_01_FLAG, CTRIETABLE_FLAG_2_LOAD_ALL)) {
         for (int i = 0; i < charTrieTable.length; i++) {
            int redID = get2(offsetRef);
            if (redID != 0) {
               offsetRef += 2;
               ByteObjectManaged tech = new ByteObjectManaged(boc, baseTrieTech);
               tech.set2(AGENT_OFFSET_06_GSOURCE_ID2, groupRef);
               tech.set2(AGENT_OFFSET_07_INSTANCE_ID2, i + 1);
               tech.setFlag(AGENT_OFFSET_01_FLAG_1, AGENT_FLAG_CTRL_8_DATA_ON_DEMAND, true);
               //we want to agent to be created but data will be loaded only demand
               charTrieTable[i] = (IPowerCharTrie) bc.getAgentFromRefOrCreate(redID, tech, IPowerCharTrie.INT_ID);
            }
         }
      }
   }

   public int addChars(char[] c, int offset, int len) {
      int trieid = getMapTrieID(c, offset, len);
      IPowerCharTrie ct = getCharTrie(trieid);
      int subid = ct.addChars(c, offset, len);
      return getModdedNode(subid, trieid);
   }

   public int addChars(String s) {
      return this.addChars(s.toCharArray(), 0, s.length());
   }

   public void appendChars(int pointer, StringBBuilder sb) {
      char[] cs = getChars(pointer);
      sb.append(cs);
   }

   public int copyChars(int pointer, char[] c, int offset) {
      char[] cs = getChars(pointer);
      for (int i = 0; i < cs.length; i++) {
         c[offset + i] = cs[i];
      }
      return cs.length;
   }

   public int countWords(int wordid) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int countWords(String prefix) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Run creation does not create data. It loads itself from the Memory Controller
    */
   public IPowerCharTrie createCharTrie(int id) {
      if (charTrieTable[id] == null) {
         IPowerCharTrie pct = null;
         //gives id as groupid
         int gid = get2(AGENT_OFFSET_06_GSOURCE_ID2);
         ByteObjectManaged baseTrieTech = this.getTechSub(0);
         ByteObjectManaged tech = new ByteObjectManaged(boc, baseTrieTech);

         pct = (IPowerCharTrie) byteCon.getAgentFromGroupInstance(gid, id, tech, IPowerCharTrie.INT_ID);
         return pct;
      }
      return charTrieTable[id];
   }

   public int find(char[] look, int offset, int len) {
      return getPointer(look, offset, len);
   }

   public int getBiggestWordSize() {
      //store it in the header ?
      int max = 0;
      for (int i = 0; i < charTrieTable.length; i++) {
         if (charTrieTable[i] != null) {
            int val = charTrieTable[i].getBiggestWordSize();
            if (val > max)
               max = val;
         }
      }
      return max;
   }

   public char getChar(int pointer) {
      int trieid = getTrieID(pointer);
      IPowerCharTrie ct = createCharTrie(trieid);

      //    String word = ct.getWord(getNode(pointer));
      //    if (trieid == 1) {
      //       return StringUtils.getUpperCaseFirstLetter(word);
      //    }
      return ct.getChar(pointer);
   }

   /**
    * 
    */
   public char[] getChars(int pointer) {
      int trieid = getTrieID(pointer);
      IPowerCharTrie ct = createCharTrie(trieid);

      //      String word = ct.getWord(getNode(pointer));
      //      if (trieid == 1) {
      //         return StringUtils.getUpperCaseFirstLetter(word);
      //      }
      return ct.getChars(pointer);
   }

   public String getCharsString(int pointer) {
      return new String(getChars(pointer));
   }
   /**
    * 
    */
   public char[] getChars(int[] pointers) {
      return CharColUtilz.get(this, pointers);
   }

   /**
    * Depending on the settings of the {@link PowerCharTrieTable}, we have
    * the trie kept in memory, or every time a new char trie is requested, the old one
    * is disposed.
    * <br>
    *  by default keep it in memory.
    * <br>
    * It should be a decision of the {@link ByteController} who will be notified of a low memory alert
    * It should not be a decision of the builder. But anyways, this flag of LOW MEMORY is set by the 
    * @param trieid
    * @return
    * @throws RuntimeException if bad trieid
    */
   public IPowerCharTrie getCharTrie(int trieid) {
      if (trieid < 0 || trieid >= charTrieTable.length) {
         System.out.println("Bad Trie ID" + trieid + " Table Size = " + charTrieTable.length);
         throw new RuntimeException();
      }
      if (charTrieTable[trieid] != null) {
         return charTrieTable[trieid];
      } else {
         //
         if (isMemoryConstrained()) {
            //free up memory
            for (int i = 0; i < charTrieTable.length; i++) {
               if (charTrieTable[trieid] != null) {
                  //
                  charTrieTable[trieid].getTech().memoryClear();
                  charTrieTable[trieid] = null;
               }
            }
         }
         charTrieTable[trieid] = createCharTrie(trieid);
         //which tech for the trie?
         return charTrieTable[trieid];
      }
   }

   public int getLen(int pointer) {
      char[] cs = getChars(pointer);
      return cs.length;
   }

   /**
    * We have to clear the memory used
    */
   public void memoryClearSub() {
   }

   public int getMapMax() {
      return 256;
   }

   public int getMapTrieID(char c) {
      if (StringUtils.isLowerCase(c)) {
         return c & 0xFF;
      } else {
         return 1;
      }
   }

   /**
    * maps an incoming word with a Trie ID
    * if null, the mapping cache is used instead. 
    * <br>
    * <br>
    * By default, TrieID is character's first 8 bits
    */
   public int getMapTrieID(char[] c, int offset, int len) {
      return getTrieID(c[offset]);
   }

   public int getMapTrieID(String s) {
      return getTrieID(s.charAt(0));
   }

   public int getModdedNode(int node, int trieid) {
      return (node << trieIDBitLength) + trieid;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getPointer(char[] chars) {
      return getPointer(chars, 0, chars.length);
   }

   public int getPointer(char[] chars, int offset, int len) {
      int trieid = mappingFunct.getTrieID(chars, offset, len);
      IPowerCharTrie ct = getCharTrie(trieid);
      return ct.getPointer(chars, offset, len);
   }

   public int getSize() {
      int totalSize = 0;
      for (int i = 0; i < charTrieTable.length; i++) {
         if (charTrieTable[i] != null) {
            totalSize += charTrieTable[i].getSize();
         }
      }
      return totalSize;
   }

   public String getKeyStringFromPointer(int pointer) {
      int trieid = getTrieID(pointer);
      IPowerCharTrie ct = createCharTrie(trieid);
      return ct.getKeyStringFromPointer(pointer);
   }

   public int getTrieID(int moddednode) {
      return BitUtils.getData(moddednode, 0, trieIDBitLength);
   }

   public boolean hasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean hasChars(String str) {
      return hasChars(str.toCharArray(), 0, str.length());
   }

   public boolean isValid(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   public void notify(TrieSearchSession tss, int wordid, int notifyType) {
      // TODO Auto-generated method stub

   }

   
   public void pointerSwap(Object struct, int newPointer, int oldPointer) {
      // TODO Auto-generated method stub

   }

   public int remove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }


   /**
    * We use a Aggregate Search
    */
   public void search(CharAggregateSearch tss) {
      //init the algo
      if (tss.isFirstFrame()) {
         if (tss.isAll()) {
            //decide the order

         } else {
            int id = getMapTrieID(tss.str);
            IPowerCharTrie tc = getCharTrie(id);
            ISearchSession sess = tc.search(tss.tech);
            
         }
      } else {
         
      }
   }

   /**
    * 
    * @param bc
    */
   public void serializeReverse(ByteController bc) {

      //we don't know where we are. params array might contain a Template with a Reference ID
      byteCon = bc;

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      //check if we are already serialized
      ByteObjectManaged bom = bc.serializeToUpdateAgentData(data);
      charco.serializeTo(bc);
      bc.setRefFromTo(charco.getTech(), bom, ITechCharTrieTable.CTRIE_OFFSET_06_REF_CHARCOL2);
      bom.set2(ITechCharTrieTable.CTRIETABLE_OFFSET_03_NUM_TRIES2, charTrieTable.length);
      int offset = bom.getDataOffsetStartLoaded();
      for (int i = 0; i < charTrieTable.length; i++) {
         if (charTrieTable[i] != null) {
            charTrieTable[i].serializeTo(bc);
            bom.set2(offset, charTrieTable[i].getTech().get2(AGENT_OFFSET_08_REF_ID2));
         }
      }
      return bom;
   }

   public int setChars(int pointer, char[] d, int offset, int len) {
      return -1;
   }

   public void addPointerUser(IPointerUser pointerUser) {

   }

   
   public void updatePointers(Object struct, Object mapping) {
      for (int i = 0; i < charTrieTable.length; i++) {
         if (charTrieTable[i] != null) {
            ((IPointerUser) charTrieTable[i]).updatePointers(struct, mapping);
         }
      }
   }

   
   public int getPointer(String str) {
      return getPointer(str.toCharArray());
   }

   
   public IntToStrings getPrefixed(String prefix) {
      return pdc.getTrieU().getPrefixedStrings(this, prefix);
   }

   
   public boolean isWord(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   
   public int getPrefixPointer(String str, ICharComparator tss) {
      // TODO Auto-generated method stub
      return 0;
   }

   void buildString(int[] nodes, int offset, int len, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

}
