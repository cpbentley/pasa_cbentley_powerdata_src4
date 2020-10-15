package pasa.cbentley.powerdata.src4.trie;

import java.util.NoSuchElementException;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.powerdata.spec.src4.guicontrols.IPrefixSearchSession;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharCol;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public abstract class PowerCharTrieRoot extends PowerBuildBase implements IPowerCharTrie {

   public PowerCharTrieRoot(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public int[][] getSizes() {
      // TODO Auto-generated method stub
      return null;
   }

   public IPrefixSearchSession searchPrefix(int frame) {
      ByteObject bo = pdc.getTechFactory().getTrieSearchSessionTechFramePrefix(frame, ITechCharCol.SEARCH_0_PREFIX);
      return (IPrefixSearchSession) search(bo);
   }

   public ISearchSession searchIndexOf(int frame) {
      ByteObject bo = pdc.getTechFactory().getTrieSearchSessionTechFramePrefix(frame, ITechCharCol.SEARCH_1_INDEXOF);
      return search(bo);
   }

   abstract void buildString(int[] nodes, int offset, int len, StringBBuilder sb);

   /**
    * Generic Open Search
    * @param param
    * @return
    */
   public ISearchSession search(ByteObject param) {
      TrieSearchSession tss = new TrieSearchSession(pdc, this, param);
      return tss;
   }

   /**
    * Implement Trie search from the settings given in the {@link TrieSearchSession} object
    * @param tss
    */
   protected abstract void search(TrieSearchSession tss);

   /**
    * Do we need a read lock? preventing any update?
    * <br>
    * <li> {@link IPowerCharCollector#ENUM_TYPE_0_STRING}
    * <li> {@link IPowerCharCollector#ENUM_TYPE_1_POINTER}
    * <li> {@link IPowerCharCollector#ENUM_TYPE_2_CHARS}
    * <li> {@link IPowerCharCollector#ENUM_TYPE_3_INTSTRINGS}
    * 
    * @param param
    * @return
    */
   public IPowerEnum getEnumOnCharCol(final int type, final Object param) {
      //use a search on the all prefix. return elements
      if (type == ENUM_TYPE_0_STRING || type == ENUM_TYPE_2_CHARS) {
         final ISearchSession tss = searchPrefix(1);
         tss.reset("");
         return new IPowerEnum() {

            /**
             * Number of times getNext has been called
             */
            int          counter = 0;

            boolean      hasNext;

            /**
             * Previous search Results
             */
            IntToStrings prev    = new IntToStrings(getUCtx());

            public Object getNext() {
               doCheck();
               if (counter >= prev.nextempty) {
                  throw new NoSuchElementException();
               }
               String str = prev.strings[counter];
               counter++;
               if (type == ENUM_TYPE_3_INTSTRINGS) {
                  IntToStrings its = new IntToStrings(getUCtx(), 1);
                  its.ints[0] = prev.ints[counter];
                  its.strings[0] = prev.strings[counter];
                  return its;
               } else if (type == ENUM_TYPE_2_CHARS) {
                  return str.toCharArray();
               } else {
                  return str;
               }
            }

            private void doCheck() {
               if (counter >= prev.nextempty) {
                  //do a search clearing previous results.
                  prev = tss.searchWait();
                  hasNext = prev.nextempty != 0;
               }
            }

            /**
             * 
             */
            public boolean hasNext() {
               doCheck();
               return hasNext;
            }
         };
      } else if (type == ENUM_TYPE_1_POINTER) {
         return getEnumOnPointers();
      } else {
         throw new IllegalArgumentException();
      }
   }

   protected abstract IPowerEnum getEnumOnPointers();

   //#mdebug

   public void toString(Dctx dc) {
      dc.root(this, "PowerCharTrieRoot");
      super.toString(dc.newLevel());
   }

   public void toString1Line(Dctx dc) {
      dc.root(this, "PowerCharTrieRoot");
   }
   //#enddebug
}
