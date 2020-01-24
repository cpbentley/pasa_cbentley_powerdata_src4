package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToIntArray;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Trie of a given human.
 * <br>
 * <br>
 * <li>Collection of {@link UserTrie} for each languages by the user
 * <li> Translations
 * <li> Special tries from books or otherwise
 * 
 * <br>
 * <br>
 * Translations of a word are provided as hardcoded.
 * <br>
 * Those accepted by the Human are then inserted its their Language Brain Model
 * <br>
 * Other translation sources can be used.
 * <br>
 * Once inserted in the Language Brain Model, the person trie prioritize
 * <br>
 * 
 * @author Charles-Philip
 *
 */
public class PersonTrie extends PowerBuildBase {

   /**
    * Link words together from different {@link UserTrie}.
    * <br>
    * Might be mutable or immutable
    */
   protected IPowerLinkStringToIntArray[] translations;

   public PersonTrie(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      // TODO Auto-generated constructor stub
   }


   public static ByteObjectManaged getTechDefault() {
      // TODO Auto-generated method stub
      return null;
   }

   
   public void serializeReverse() {
      // TODO Auto-generated method stub
      
   }


   
   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

}
