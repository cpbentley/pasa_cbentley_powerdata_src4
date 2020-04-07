package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.FiFoQueue;
import pasa.cbentley.core.src4.structs.IntToInts;
import pasa.cbentley.core.src4.structs.IntToObjects;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public class CharAggregateSearch extends CharSearchSession {

   PowerCharColAggregate    ca;

   private int              currentDeepNess;

   private int              currentSessCount;

   private ISearchSession   currentSession;

   protected IntToInts      extra;

   FiFoQueue                fifo;

   private IntToObjects     insides;

   private ISearchSession[] sess;

   public CharAggregateSearch(PDCtx pdc, PowerCharColAggregate co, ByteObject tech) {
      super(pdc, co, tech);
      fifo = new FiFoQueue(pdc.getUCtx());
   }

   public int[] getActivations() {
      return null;
   }

   public int[] getTypeDeepNess() {
      return null;
   }

   /**
    * Maybe this must be kept at the level of aggregates? Test is made only if ISearchSession
    * is an instance of aggregate. Otherwise
    * Also aggregates of sessions to poll if an object is being searched
    * @param o
    * @return
    */
   public boolean isSearched(Object o) {
      if (insides == null) {
         insides = new IntToObjects(pdc.getUCtx());
      }
      boolean b = insides.hasObject(o);
      for (int i = 0; i < sess.length; i++) {
         ISearchSession s = sess[i];

         if (sess[i].getSearched() == o) {
            return true;
         }
      }
      return b;
   }

   /**
    * Search the {@link ISearchSession}s, returns when the framesize is reached and save state
    * to continue
    */
   public void searchAgg() {
      //continue where we left off. 
      int maxSize = getFrameSize();
      if (maxSize <= 0) {
         searchAgg0();
      } else {
         searchAggX();
      }
   }

   /**
    * When our framesize is 0
    */
   private void searchAgg0() {
      //init algo
      boolean isContinue = true;
      int maxSize = ca.getSize(); //safety latch
      int totalCount = 0;
      for (int i = 0; i < sess.length; i++) {
         fifo.put(sess[i]);
      }
      //algo loop
      while (isContinue && totalCount < maxSize) {
         ISearchSession ses = (ISearchSession) fifo.getHead();
         if (ses == null) {
            isContinue = false;
         } else {
            //select next session to search
            IntToStrings its = currentSession.searchWait();
            int growth = result.addReturn(its);
            if (growth != 0) {
               totalCount += growth;
               //add the session back to our queue.
               fifo.put(currentSession);
            }
         }
      }
   }

   /**
    * Search in several sessions
    */
   private void searchAggX() {
      //init algo
      boolean isContinue = true;
      int maxSize = getFrameSize(); //safety latch
      for (int i = 0; i < sess.length; i++) {
         fifo.put(sess[i]);
      }
      int left = maxSize;
      //algo loop
      while (isContinue && left > 0) {
         ISearchSession ses = (ISearchSession) fifo.getHead();
         if (ses == null) {
            isContinue = false;
         } else {
            //select next session to search
            int frameSize = Math.min(left, currentSession.getFrameSize());
            currentSession.setFrameSizeDyn(frameSize);
            IntToStrings its = currentSession.searchWait();
            int growth = result.addReturn(its);
            for (int i = 0; i < its.nextempty; i++) {
               extra.add(currentSession.getID(), 0);
            }
            if (growth != 0) {
               left -= growth;
               //add the session back to our queue.
               fifo.put(currentSession);
            }
         }
      }
   }

   /**
    * Sets the frame sizes.
    * <br>
    * When master frame size is defined as 3, the frame size is set as the minimum.
    * <br>
    * The method will track the count
    * {@link CharAggregateSearch#searchAgg()}
    * 
    * @param sessions
    */
   public void setSessions(ISearchSession[] sessions) {
      sess = sessions;
      int[] typeDeepness = getTypeDeepNess();
      int mainFrameSize = getFrameSize();
      for (int i = 0; i < sess.length; i++) {
         //
         sess[i].setID(i);
         if (mainFrameSize == 0) {
            sessions[i].setFrameSize(typeDeepness[i]);
         } else {
            //dynamic frame sizes
            if (typeDeepness[i] == 0) {
               sessions[i].setFrameSize(mainFrameSize);
            } else {
               int min = Math.min(mainFrameSize, typeDeepness[i]);
               sessions[i].setFrameSize(min);
            }
         }
      }

   }

   /**
    * When searching aggregates, we need to make sure we don't search the same {@link IPowerCharCollector}
    * twice.
    * @param o
    * @return
    */
   public boolean wasAdded(Object o) {
      if (insides == null) {
         insides = new IntToObjects(pdc.getUCtx());
      }
      boolean b = insides.hasObject(o);

      if (!b) {
         insides.add(o);
      }
      return b;
   }
   
   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "CharAggregateSearch");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "CharAggregateSearch");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
