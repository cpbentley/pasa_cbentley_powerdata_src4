package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechUserTrie;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.string.CharAggregateSearch;
import pasa.cbentley.powerdata.src4.string.PowerCharColAggregate;
import pasa.cbentley.powerdata.src4.string.PowerLinkStringToIntArray;

/**
 * Trie of a given language.
 * <br>
 * <br>
 * An aggregate of several Tries or simple {@link IPowerCharCollector}. A UserTrie word is identified with a pointer
 * <li>1 bit for dic/newword
 * <br>
 * <br>
 * Each pointer in the {@link IPowerCharTrie} and {@link IPowerCharCollector} interface is interpreted with 
 * the {@link UserTrie} context. 
 * Thus it will look for the user word bit.
 * <br>
 * <br>
 * 
 * @see PowerCharColAggregate
 * @author Charles Bentley
 *
 */
public class UserTrie extends PowerCharColAggregate implements IPowerCharTrie, ITechUserTrie {

   /**
    * When a word in dicTrie is selected, it is added in this trie.
    * <br>
    * <br>
    * When the feedback is notfied with {@link IPowerCharTrie#notify(TrieSearchSession, int, int)}
    * <br>
    * <br>
    * 
    */
   private IPowerCharTrie              dicFeedbackTrie;

   /**
    * Lemme dictionary.
    * <br>
    * <br>
    * read-only 
    */
   private IPowerCharTrie              dicTrie;

   /**
    * Links the words to the Brain Model.
    * <br>
    * The byte source is coming from a mutable byte store.
    */
   protected PowerLinkStringToIntArray feedback;

   /**
    * New words are added in this trie with pointer stability.
    */
   private IPowerCharTrie              newWordsTrie;

   /**
    * Controls the plurals, cases, verb conjuga
    */
   private IPowerCharTrie              variants;

   public UserTrie(PDCtx pdc) {
      super(pdc, pdc.getTechFactory().getUserTrieRoot());
   }

   /**
    * Construct a UserTrie, loads the dictionnary trie.
    * <br>
    * Creates a {@link ByteController} based on the 
    * @param tech The Tech
    */
   public UserTrie(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmpty(tech);

   }

   public int addChars(char[] c, int offset, int len) {
      int id = getNewWordsTrie().addChars(c, offset, len);
      //create word id
      id = BitUtils.setBit(id, 31, 1);
      return id;
   }

   public int addChars(String s) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void addPointerUser(IPointerUser pointerUser) {
      // TODO Auto-generated method stub

   }

   /**
    * 
    */
   public void appendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   void buildString(int[] nodes, int offset, int len, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   public int copyChars(int pointer, char[] c, int offset) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int countWords(int wordid) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int countWords(String prefix) {
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
      // TODO Auto-generated method stub
      return null;
   }

   public char[] getChars(int[] pointers) {
      // TODO Auto-generated method stub
      return null;
   }

   public IPowerCharTrie getDicTrie() {
      return dicTrie;
   }

   protected IPowerEnum getEnumOnPointers() {
      return null;
   }

   public IPowerCharTrie getFeedbackTrie() {
      return dicFeedbackTrie;
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

   public IPowerCharTrie getNewWordsTrie() {
      if (newWordsTrie == null) {
         int newWordsRef = this.get2(USERTRIE_OFFSET_04_REF_NEWWORDS_TRIE4);
         ByteObjectManaged techNewWords = this.getTechSub(USERTRIE_REF_TRIE_3_NEWWORDS);
         ByteController bc = getByteControllerCreateIfNull();
         newWordsTrie = (IPowerCharTrie) bc.getAgentFromRefOrCreate(newWordsRef, techNewWords, IPowerCharTrie.INT_ID);
      }
      return newWordsTrie;
   }

   public int getPointer(char[] chars) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * The first 4 bits are used to code
    */
   public int getPointer(char[] chars, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getPointer(String str) {
      // TODO Auto-generated method stub
      return 0;
   }

   public IntToStrings getPrefixed(String prefix) {
      return pdc.getTrieU().getPrefixedStrings(this, prefix);
   }

   public int getPrefixPointer(String str, ICharComparator tss) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public boolean hasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean hasChars(String str) {
      // TODO Auto-generated method stub
      return false;
   }

   private void initEmpty(ByteObjectManaged tech) {

      //can we load only the header without the data? yes we want fast start up times.
      ByteController bc = getByteControllerCreateIfNull();
      //pool the char collector
      int dicRef = this.get2(USERTRIE_OFFSET_02_REF_DIC_TRIE2);
      int feedbackRef = this.get2(USERTRIE_OFFSET_03_REF_FEEDBACK_TRIE3);
      int newWordsRef = this.get2(USERTRIE_OFFSET_04_REF_NEWWORDS_TRIE4);

      //find the usertrie from the tech defintion
      ByteObjectManaged techDicTrie = tech.getTechSub(USERTRIE_REF_1_DIC);
      dicTrie = (IPowerCharTrie) bc.getAgentFromRefOrCreate(dicRef, techDicTrie, IPowerCharTrie.INT_ID);

      //pre cable means the byteobjectmanaged has a preference to be at the given index.
      ByteObjectManaged techFeedbackTrie = tech.getTechSub(USERTRIE_REF_2_FEEDBACK);
      //there might a class exception because of wrong parameters
      feedback = (PowerLinkStringToIntArray) bc.getAgentFromRefOrCreate(feedbackRef, techFeedbackTrie, PowerLinkStringToIntArray.INT_ID);

      //for the sake of exercise, if newWord is used in another class? is it possible? no because its a writable
      //code must get a reference from this class.
      //obtain the data in read only mode? perfect ennemy of good. just get a reference of this object
      //but but but... shared CharCollectors at the level of ByteController??? The agent is loaded
      //the first strucuture loading, loads the shared CharCollector using ByteStoreSource.
      //When another structure loading from the ByteStoreSource??? The second ByteController must somehow
      //get the reference of the shared CharCollectro from the first ByteController. What is the Reference ID
      //to be used to identify in 2 different contexts?
      ByteObjectManaged techNewWords = tech.getTechSub(USERTRIE_REF_TRIE_3_NEWWORDS);
      newWordsTrie = (IPowerCharTrie) bc.getAgentFromRefOrCreate(newWordsRef, techNewWords, IPowerCharTrie.INT_ID);
   }

   public boolean isValid(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isWord(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   /**
    * 
    */
   public void notify(TrieSearchSession tss, int wordid, int notifyType) {
      // TODO Auto-generated method stub

   }

   public int remove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * TODO we want to tag words from feedback as blue. from dic as green.
    * How do you code for this type?
    * Status ID for a word. linked to a StyleClass array
    */
   public void search(CharAggregateSearch tss) {
      //as an aggregate, look at search settings. merge those settings with current
      //aggregate activations.
      //aggragate search sessions in the array
      //is this the first time we get this search session.
      if (tss.wasAdded(this)) {
         //look up search type activation
         int[] activations = tss.getActivations();
         ISearchSession[] sessions = new ISearchSession[activations.length];
         int offset = 0;
         //sessions are added.
         //if a char col is itself an aggregate 
         ISearchSession issNewWords = newWordsTrie.search(tss.tech);
         ISearchSession issFeed = dicFeedbackTrie.search(tss.tech);
         ISearchSession issDic = dicTrie.search(tss.tech);
         for (int i = 0; i < activations.length; i++) {
            if (activations[i] == 0) {
               //check if object is already in the search. we have to link sub searches!
               if (tss.wasAdded(types[i])) {
                  sessions[i] = types[i].search(tss.tech);
               }
            }
         }
         sessions[offset++] = issNewWords;
         //add the sessions
         tss.setSessions(sessions);
         //start the search according to the search type deepness
         //
         int[] typeDeepness = tss.getTypeDeepNess();
         for (int i = 0; i < sessions.length; i++) {
            sessions[i].setFrameSize(typeDeepness[i]);
         }

      } else {
         tss.searchAgg();
      }
      //TODO deal with the frame.

   }

   private byte[] serializeRaw() {
      //returns the ByteObjectManaged empty of data with headers and trailers
      byte[] header = toByteArray();

      return header;
   }

   //#mdebug

   /**
    * Reads the dictionnary Trie
    * <br>
    * <br>
    * 
    * @param bc
    */
   public void serializeReverse() {
      //we have 3 different tries. they have an instance ID
      int charcoRef = this.get2(USERTRIE_OFFSET_02_REF_DIC_TRIE2);
      int feedbackRef = this.get2(USERTRIE_OFFSET_03_REF_FEEDBACK_TRIE3);
      int newWordsRef = this.get2(USERTRIE_OFFSET_04_REF_NEWWORDS_TRIE4);

      //search for an Object matching the class definition and with the correct instance  ID.

      try {
         dicTrie = (IPowerCharTrie) byteCon.getAgentFromRefOrCreate(charcoRef, IPowerCharTrie.INT_ID);
      } catch (Exception e) {
         //failure. create an empty one. notify failure to load dicTrie
         e.printStackTrace();
      }
      dicFeedbackTrie = (IPowerCharTrie) byteCon.getAgent(feedbackRef);
      newWordsTrie = (IPowerCharTrie) byteCon.getAgent(newWordsRef);
   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      ByteObjectManaged bom = byteCon.serializeToUpdateAgentData(serializeRaw());

      dicTrie.serializeTo(bc);
      dicFeedbackTrie.serializeTo(bc);
      newWordsTrie.serializeTo(bc);
      bom.set2(USERTRIE_OFFSET_02_REF_DIC_TRIE2, dicTrie.getTech().get2(AGENT_OFFSET_08_REF_ID2));
      bom.set2(USERTRIE_OFFSET_03_REF_FEEDBACK_TRIE3 + 2, dicFeedbackTrie.getTech().get2(AGENT_OFFSET_08_REF_ID2));
      bom.set2(USERTRIE_OFFSET_04_REF_NEWWORDS_TRIE4 + 2, newWordsTrie.getTech().get2(AGENT_OFFSET_08_REF_ID2));
      return bom;
   }

   public int setChars(int pointer, char[] d, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "UserTrie");
      toStringPrivate(dc);
      super.toString(dc.sup());

   }

   private void toStringPrivate(Dctx sb) {
      sb.append(super.toString());
      sb.nl();
      sb.append("dicFeedbackTrie");
      sb.nl();
      sb.append(dicFeedbackTrie.toString());
      sb.nl();
      sb.append("dicTrie");
      sb.nl();
      sb.append(dicTrie.toString());
      sb.nl();
      sb.append("newWordsTrie");
      sb.nl();
      sb.append(newWordsTrie.toString());
      sb.nl();
      sb.append("feedback");
      sb.nl();
      sb.append(feedback.toString());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "UserTrie");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

}
