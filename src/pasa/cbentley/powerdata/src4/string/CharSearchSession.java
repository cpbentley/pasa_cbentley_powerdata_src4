package pasa.cbentley.powerdata.src4.string;

import java.util.Enumeration;
import java.util.Vector;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.guicontrols.IPrefixSearchSession;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchCallBack;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharCol;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchChar;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.trie.TrieSearchSession;

/**
 * Char Session used in the context of {@link IPowerCharCollector} search
 * <br>
 * <br>
 * What is the relation to the {@link TrieSearchSession} ?
 * <br>
 * <br>
 * It is the mother class. Because we want to be able to search any {@link IPowerCharCollector}.
 * A Trie will be faster for prefix search of course.
 * <br>
 * <br>
 * History. At the beginning, we had the getPrefix(String) on a CharTrie. Then we had {@link TrieSearchSession} on the {@link ICharTrie}. But since the decision that CharTries are {@link IPowerCharCollector}
 * one application uses {@link IPowerCharCollector} without knowing if it is a Trie or not.
 * Thus a search  mechanism that does not rely on {@link TrieSearchSession} had to be found.
 * <br>
 * <br>
 * 
 * Complete search:
 * First returns results from prefix,
 * then returns results from substring search.
 * {@link SubStrinxIndex} allows for fast substring searches.
 * 
 * @see ISearchSession
 * @author Charles Bentley
 *
 */
public class CharSearchSession implements ITechSearchChar, ICharComparator, ISearchSession, IPrefixSearchSession {

   private static final int      COMP_0_BASE        = 0;

   private static final int      COMP_1_ACCENT      = 1;

   private static final int      COMP_2_ACCENT_CAPS = 2;

   private static final int      COMP_3_CAPS        = 3;

   /**
    * 
    */
   public Vector                 callbacks          = new Vector();

   protected IPowerCharCollector charcol;

   public char[]                 chars;

   public int                    charsLen;

   public int                    charsOffset;

   public ICharComparator        compa;

   protected IPowerEnum e;

   private int          frameSizeDyn;

   private int          id;

   protected boolean             isFinished         = false;

   protected final PDCtx         pdc;

   /**
    * String mapped to their pointer back to the structure.
    */
   public IntToStrings           result;

   /**
    * Root session which controls the whole results
    */
   public ISearchSession         root;

   protected int                 searchCount;

   protected int                 searchFrameCount;

   public String                 str;

   /**
    * 
    */
   public ByteObject             tech;

   /**
    * 
    */
   private int                   type;

   /**
    * 
    */
   private int                   typeComp;

   public CharSearchSession(PDCtx pdc, IPowerCharCollector co, ByteObject tech) {
      this.pdc = pdc;
      if (co == null) {
         throw new NullPointerException();
      }
      this.tech = tech;
      this.charcol = co;
      result = new IntToStrings(pdc.getUCtx(), 5);
      type = tech.get1(SEARCH_CHAR_OFFSET_03_SEARCH_TYPE1);
      resetTypeComp();
   }

   public void addCallBack(ISearchCallBack cb) {
      callbacks.addElement(cb);
   }

   protected boolean checkAccent(char prefixChar, char trieChar) {
      if (prefixChar == 'e') {
         if (trieChar == 'e' || trieChar == 'è' || trieChar == 'ê' || trieChar == 'é') {
            return true;
         }
         return false;
      } else if (prefixChar == 'a') {
         if (trieChar == 'a' || trieChar == 'â') {
            return true;
         }
         return false;
      } else if (prefixChar == 'o') {
         if (trieChar == 'o' || trieChar == 'ô') {
            return true;
         }
         return false;
      } else if (prefixChar == 'i') {
         if (trieChar == 'i' || trieChar == 'î') {
            return true;
         }
         return false;
      } else {
         return prefixChar == trieChar;
      }
   }

   protected boolean checkAccentCaps(char prefixChar, char trieChar) {
      boolean b = StringUtils.isEqualIgnoreCap(prefixChar, trieChar);
      if (!b) {
         b = checkAccent(prefixChar, trieChar);
      }
      return b;
   }

   protected boolean checkCaps(char prefixChar, char trieChar) {
      return StringUtils.isEqualIgnoreCap(prefixChar, trieChar);
   }

   public int getFrameCount() {
      return searchFrameCount;
   }

   public int getFrameSize() {
      return tech.get2(SEARCH_CHAR_OFFSET_02_FRAME_SIZE2);
   }

   public int getFrameSizeDyn() {
      return frameSizeDyn;
   }

   public int getID() {
      return id;
   }

   public IntToStrings getResults() {
      return result;
   }

   public IPowerCharCollector getSearched() {
      return charcol;
   }

   public void incrementFrameCount() {
      searchFrameCount++;
   }

   /**
    * True when prefix is empty and we want to return all Strings
    * @return
    */
   public boolean isAll() {
      return charsLen == 0;
   }

   /**
    * 
    */
   public boolean isEqual(char prefixChar, char trieChar) {
      switch (typeComp) {
         case COMP_0_BASE:
            return prefixChar == trieChar;
         case COMP_1_ACCENT:
            return checkAccent(prefixChar, trieChar);
         case COMP_2_ACCENT_CAPS:
            return checkAccentCaps(prefixChar, trieChar);
         case COMP_3_CAPS:
            return checkCaps(prefixChar, trieChar);
         default:
            return prefixChar == trieChar;
      }
   }

   public boolean isFinished() {
      return isFinished;
   }

   public boolean isFirstFrame() {
      return searchFrameCount == 0;
   }

   public boolean isReverse() {
      return tech.hasFlag(ITechSearchChar.SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_5_REVERSE_ORDER);
   }

   public boolean isSearched(Object o) {
      return o == charcol;
   }

   /**
    * This method will call the full search and search for possible chars
    * 
    */
   public char[] nextPossibleChars() {
      //
      if (getFrameSize() != 0) {
         setFrameSize(0);
         searchWait();
      }
      int prefixSize = str.length();
      IntBuffer ib = new IntBuffer(pdc.getUCtx());
      for (int i = 0; i < result.nextempty; i++) {
         char c = result.strings[i].charAt(prefixSize);
         if (!ib.contains(c)) {
            ib.addInt(c);
         }
      }
      char[] cs = new char[ib.getSize()];
      for (int i = 0; i < cs.length; i++) {
         char c = (char) ib.get(i);
         cs[i] = c;
      }
      return cs;
   }

   public void notifyAction(int wordid, String word, int actionType) {

   }

   protected void notifyCallBacks() {
      Enumeration en = callbacks.elements();
      while (en.hasMoreElements()) {
         ISearchCallBack scb = (ISearchCallBack) en.nextElement();
         scb.searchResult(0, this);
      }
   }

   protected void reset() {

   }

   public void reset(char[] c, int offset, int len) {
      chars = c;
      charsLen = len;
      charsOffset = offset;
      str = new String(c, offset, len);
      searchFrameCount = 0;
      searchCount = 0;
      result.nextempty = 0;
      reset();
   }

   public void reset(String str) {
      reset(str.toCharArray(), 0, str.length());
   }

   private void resetTypeComp() {
      boolean acc = tech.hasFlag(SEARCH_CHAR_OFFSET_05_FLAGZ1, SEARCH_CHAR_FLAGZ_1_SOFT_ACCENT);
      boolean cap = tech.hasFlag(SEARCH_CHAR_OFFSET_05_FLAGZ1, SEARCH_CHAR_FLAGZ_2_SOFT_CAP);

      if (acc && cap) {
         typeComp = COMP_2_ACCENT_CAPS;
      } else if (acc) {
         typeComp = COMP_1_ACCENT;
      } else if (cap) {
         typeComp = COMP_3_CAPS;
      } else {
         typeComp = COMP_0_BASE;
      }
   }

   /**
    * Delegates to the {@link IPowerCharCollector} the search.
    * <br>
    * This class provides a default implementation with an {@link IPowerEnum}.
    * <br>
    * Default search enumerates and does the check based on type.
    */
   public void search() {
      if (e == null) {
         e = charcol.getEnumOnCharCol(IPowerCharCollector.ENUM_TYPE_3_INTSTRINGS, null);
      }
      boolean isContinue = true;
      while (e.hasNext() && isContinue) {
         IntToStrings its = (IntToStrings) e.getNext();
         if (type == ITechCharCol.SEARCH_0_PREFIX) {
            if (its.strings[0].startsWith(str)) {
               result.add(its.ints[0], its.strings[0]);
               searchCount++;
            }
         } else if (type == ITechCharCol.SEARCH_1_INDEXOF) {
            if (its.strings[0].indexOf(str) != -1) {
               result.add(its.ints[0], its.strings[0]);
               searchCount++;
            }
         }
         if (searchCount >= getFrameSize()) {
            isContinue = false;
         }
      }
   }

   /**
    * Updates the search prefix. no optimization
    */
   public IntToStrings searchAdd(String c) {
      String nstr = str + c;
      return searchWait(nstr);
   }

   public IntToStrings searchRemove(int v) {
      int in = Math.max(0, str.length() - v);
      String nstr = str.substring(0, in);
      return searchWait(nstr);
   }

   public IntToStrings searchWait() {
      search();
      return result;
   }

   public IntToStrings searchWait(char[] c, int offset, int len) {
      reset(c, offset, len);
      return searchWait();
   }

   public IntToStrings searchWait(String str) {
      reset(str);
      return searchWait();
   }

   public void setAppend(boolean b) {
      tech.setFlag(SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_3_IS_APPEND, b);

   }

   public void setFirstCap(boolean v) {
      tech.setFlag(SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_1_RETURN_FIRST_CAP, v);

   }

   public void setFrameSize(int frameSize) {
      tech.set2(SEARCH_CHAR_OFFSET_02_FRAME_SIZE2, frameSize);
   }

   public void setFrameSizeDyn(int frameSize) {
      frameSizeDyn = frameSize;
   }

   public void setID(int id) {
      this.id = id;
   }

   public void setReverseOrder(boolean b) {
      tech.setFlag(SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_5_REVERSE_ORDER, b);
   }

   public void setSoftAccent(boolean b) {
      tech.setFlag(SEARCH_CHAR_OFFSET_05_FLAGZ1, SEARCH_CHAR_FLAGZ_1_SOFT_ACCENT, b);
      resetTypeComp();
   }

   public void setSoftCap(boolean b) {
      tech.setFlag(SEARCH_CHAR_OFFSET_05_FLAGZ1, SEARCH_CHAR_FLAGZ_2_SOFT_CAP, b);
      resetTypeComp();
   }

   public void setThreaded(boolean b) {
      tech.setFlag(SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_2_IS_THREADED, b);
   }

   public void subordinateTo(ISearchSession parent) {
      root = parent;
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "CharSearchSession");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "CharSearchSession");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return pdc.getUCtx();
   }

   //#enddebug
   

}
