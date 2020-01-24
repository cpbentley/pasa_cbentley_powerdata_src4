package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * The main change is an additional trie
 * <br>
 * <br>
 * When adding a word, suffixes are looked into the grappe Trie.
 * <br>
 * <br>
 * When building, suffixes are computed. Once a suffix has enough occurences, it is grapped.
 * <br>
 * But this will break the pointers.
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class PowerCharTrieGrap extends PowerCharTrie {

   private PowerCharTrie grappesTrie;

   public PowerCharTrieGrap(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   /**
    * Basic search. When ending with a leaf. check if it is a grappe leaf.
    * if yes, continue looking in the grappe Trie.
    */
   public int getWordID(char[] look, int offset, int len) {
      //gets the matching leaf
      return IPowerCharCollector.CHARS_NOT_FOUND;
   }

}
