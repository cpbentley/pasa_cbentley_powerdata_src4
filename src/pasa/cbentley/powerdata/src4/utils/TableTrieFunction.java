package pasa.cbentley.powerdata.src4.utils;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Function to c -> talbe ID.
 * <br>
 * First divide and conquer. Give a Trie for each category. Thus the Trie ID.
 * <br>
 * {@link ICharTrie#getWordData(String)} for example uses {@link TableTrieFunction#getTrieID(String)} to know
 * which {@link ICharTrie} to use.
 * <br>
 * Most of the time, for char tries, they might be a Trie for each letter.
 * <br>
 * @author Charles Bentley
 *
 * @see IntSequenceMap
 * @see LetterIndex256Map
 */
public abstract class TableTrieFunction extends ByteObjectManaged {

   protected final PDCtx pdc;

   public TableTrieFunction(PDCtx pdc) {
      super(pdc.getBoc());
      this.pdc = pdc;
   }

   /**
    * Compute the TrieID for that word
    * @param c
    * @param offset
    * @param len
    */
   public abstract int getTrieID(char[] word, int offset, int len);

   /**
    * Compute the TrieID for that word
    * @param s
    * @return
    */
   public abstract int getTrieID(String s);

   public abstract int getMax();

}
