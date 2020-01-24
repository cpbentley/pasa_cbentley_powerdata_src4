package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.IEventsCore;
import pasa.cbentley.core.src4.event.BusEvent;
import pasa.cbentley.core.src4.event.IEventBus;
import pasa.cbentley.core.src4.event.IEventConsumer;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.i8n.IStringProducer;
import pasa.cbentley.core.src4.i8n.LocaleID;
import pasa.cbentley.powerdata.spec.src4.guicontrols.IPrefixSearchSession;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColLocale;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Multi language char collector.
 * <br>
 * 
 * One root {@link IPowerCharCollector} for the base language.
 * <br>
 * <br>
 * It is used as a reference when words don't have a translation.
 * <br>
 * To modify the translation, use set chars?
 * 
 * @author Charles Bentley
 *
 */
public class PowerCharColLocale extends PowerBuildBase implements IEventConsumer, IPowerCharCollector, ITechCharColLocale {

   protected IPowerCharCollector[] all;

   protected LocaleID              lid;

   /**
    * Stable pointers only
    */
   protected IPowerCharCollector   root;

   protected IPowerCharCollector   translated;

   public PowerCharColLocale(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public int addChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * When adding a char, it must be done for all 
    * @param s
    * @return
    */
   public int addChars(String s) {
      loadAll();
      int p = all[0].addChars(s);
      for (int i = 1; i < all.length; i++) {
         int p2 = all[i].addChars(s);
         if (p != p2) {
            throw new IllegalStateException();
         }
      }
      return p;
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

   public char[] getChars(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   public char[] getChars(int[] pointers) {
      // TODO Auto-generated method stub
      return null;
   }

   public IPowerEnum getEnumOnCharCol(int type, Object param) {
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

   public int getPointer(char[] chars) {
      return translated.getPointer(chars);
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
      return translated.getSize();
   }

   public int[][] getSizes() {
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

   public void initEmpty() {
      IStringProducer isp = null;
      IEventBus eb = pdc.getUCtx().getEventBusRoot();
      eb.addConsumer(this, IEventsCore.PID_1_FRAMEWORK, IEventsCore.EID_FRAMEWORK_2_LANGUAGE_CHANGED);
      LocaleID lid = isp.getLocaleID();
      LocaleID rootlid = isp.getLocaleIDRoot();
      //based on the local load the
      int rootID = get2(LOCALE_CC_OFFSET_03_ROOT_REF2);
      root = (IPowerCharCollector) byteCon.getAgentFromRefOrCreate(rootID, IPowerCharCollector.INT_ID);

      if (lid != rootlid) {

         int offset = get2(LOCALE_CC_OFFSET_05_DYNAMIC_ID2);
         int num = get2(LOCALE_CC_OFFSET_02_NUM2);
         int[] vals = getValues(offset, 2, num);
         int localeID = 0;
         int tRefid = vals[localeID];

         translated = (IPowerCharCollector) byteCon.getAgentFromRefOrCreate(tRefid, IPowerCharCollector.INT_ID);
      } else {
         translated = root;
      }

   }

   protected boolean isTrans() {
      return root != translated;
   }

   public boolean isValid(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   private void loadAll() {

   }

   public int remove(int pointer, boolean useForce) {
      loadAll();
      int p = all[0].remove(pointer, useForce);
      for (int i = 1; i < all.length; i++) {
         int p2 = all[i].remove(pointer, useForce);
         if (p != p2) {
            throw new IllegalStateException();
         }
      }
      return p;
   }

   public ISearchSession search(ByteObject param) {
      // TODO Auto-generated method stub
      return null;
   }

   public ISearchSession searchIndexOf(int frame) {
      // TODO Auto-generated method stub
      return null;
   }

   public IPrefixSearchSession searchPrefix(int frame) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * The trie must be stable
    * @param pointer
    * @param d
    * @param offset
    * @param len
    * @return
    */
   public int setChars(int pointer, char[] d, int offset, int len) {
      return translated.setChars(pointer, d, offset, len);
   }

   public void consumeEvent(BusEvent e) {
      // TODO Auto-generated method stub

   }

}
