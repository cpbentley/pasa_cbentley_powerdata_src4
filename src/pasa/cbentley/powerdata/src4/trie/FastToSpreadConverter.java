package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.core.src4.utils.ShortUtils;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechNodeData;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSpreadNodeData;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 */
public class FastToSpreadConverter {

   public static double  minimumTopologyRatio = 2.0;

   private int           _newrootposition;

   private int           _maxFamilySize;

   private int[][]       _tempVirtualStore;

   /**
    * For non patterned nodes, tells the number of i size families
    */
   private int[]         _positions;

   private FastNodeData  srcNodeData;

   private PowerCharTrie chatTrie;

   private PDCtx pdc;

   
   public FastToSpreadConverter(PDCtx pdc) {
      this.pdc = pdc;
   }
   
   private boolean isUsingParentPointers() {
      return srcNodeData.getTech().hasFlag(ITechNodeData.NODEDATA_OFFSET_01_FLAG, ITechNodeData.NODEDATA_FLAG_1_PARENT);
   }

   /**
    * Counts the number of family topologies
    * @param minimum
    * @return
    */
   private IntToStrings getTopologyValues(double minimum) {
      IntToStrings its = null;
      return its;
   }

   /**
    * Create a structure for re-arranging the nodes' sequence according to the family pattern.
    * <br>
    * <br>
    * 
    * The first nodes will have the 0 family pattern, then the 00, etc.
    * <br>
    * <br>
    * Structure of array returned
    * an int[][] where 
    * <li>column 0 = new node (virtual
    * <li>column 1 = new node to papa's offset
    * <li>column 2 = new node to empty family(0) or not (1) 
    * <li>column 3 = old node to new position 
    * <br>
    * <br>
    * pattern data is filled.
    * Columns equal a pattern
    * <br>
    * <li>index 0 = the pattern as an integer
    * <li>index 1 = the number of pattern occurences
    * <li>index 2 = the number of zeros in the pattern
    * <li>index 3 = the size of the pattern
    * <li>index 4 = double check
    * <li>index 5 = the number of nodes in the pattern
    * 
    * <br>
    * <br>
    * Strings are pattern such as 110, 1111, 1, 0010
    * They will have to be sorted if user wants in first place the 0, 00, 000, ....
    * <br>
    * <br>
    * @param its Topology Trie Values. 
    * @param patternsdata a non null base array of size 6
    * @return

    */
   private int[][] buildVirtualStore(IntToStrings its, int[][] patternsdata) {
      // the pattern as an integer
      int[] patterns = new int[its.nextempty];
      //the number of times the pattern is repeated. i.e the number of families
      int[] patternsNum = new int[its.nextempty];
      // the number of zeros in the pattern
      int[] patternZeros = new int[its.nextempty];
      int[] patternSize = new int[its.nextempty];
      int[] numNodesInPattern = new int[its.nextempty];
      int[] pcheck = new int[its.nextempty];
      // [newnode:parent=oldnode] newnode is implicit
      //in the virtual store index 0 is not used. 
      int[][] virtualstore = new int[5][srcNodeData.getNumberOfNodes() + 1];
      //with the good topologies
      int virtualnode = 1;
      //root node will be tracked in a header field 
      //but still starts at 1 because 0 is has a special meaning for childpointers
      int patterncount = 0;
      // used to mark node once inside the virtualstore, so a node is worked on only once.
      int[] marker = new int[srcNodeData.getNumberOfNodes()];
      // 
      //for all patterns, we are going over all nodes
      //when a node has a family of that pattern
      for (int i = 0; i < its.nextempty; i++) {
         //the pattern = the family topology
         String patternstring = its.strings[i];
         int patternsize = patternstring.length();
         if (patternsize == 0)
            continue;
         patternSize[patterncount] = patternsize;
         patternZeros[patterncount] = StringUtils.countChars(patternstring, '0');
         int pattern = BitUtils.toInteger(patternstring);
         //the pattern's data
         patterns[patterncount] = pattern;
         patternsNum[patterncount] = its.ints[i];
         int startNode = srcNodeData.getFirstNode();
         int lastNode = srcNodeData.getNodeLast();
         //for all old nodes. start at 0 for the old root
         for (int oldrealnode = startNode; oldrealnode <= lastNode; oldrealnode++) {
            //node is the grand father
            //true by default. trying to invalidate
            boolean rightPattern = true;
            int familySizeOldReal = srcNodeData.getNodeFamilySize(oldrealnode);
            if (familySizeOldReal != patternsize)
               continue;
            //good pattern size, so now check the topology e.g. 01010 leaf/family/leaf/family/leaf
            // for that need to check every grandchildren to see if they match
            int childPointerOldReal = srcNodeData.getNodeChildFirst(oldrealnode);
            //for every children of node, we
            for (int k = 0; k < familySizeOldReal; k++) {
               //trying to match the pattern with grandchildren
               //first get the number of grand-children for each child
               int childFamilySize = srcNodeData.getNodeFamilySize(childPointerOldReal + k);
               // 0001. chart(0) is for node k = 0; charAt(3) is for node k = 3
               // 4 - 0 -1 = 3
               int letterindex = k;
               // to match 0, we need 0 family size and a '0'
               if (childFamilySize == 0 && patternstring.charAt(letterindex) != '0') {
                  rightPattern = false;
                  break;
               }
               if (childFamilySize != 0 && patternstring.charAt(letterindex) == '0') {
                  rightPattern = false;
                  break;
               }
            }
            //the family size matches the pattern size 
            //now we have to tell our parent node where their children will emigrate
            if (rightPattern) {
               pcheck[patterncount]++;
               //test for pattern 8
               //         if (patterncount == 4) {
               //          TrieNodeData td = getNode(oldrealnode);
               //          //
               //          System.out.println(pcheck[patterncount] + " " + oldrealnode + " f=" + td.familySize + " c=" + td.childPointer);
               //          for (int k = 0; k < familySizeOldReal; k++) {
               //             int childFamilySize = getFamilySize(childPointerOldReal + k);
               //             //System.out.println(childFamilySize);
               //          }
               //         }
               if (oldrealnode == 0) {
                  //in the few cases where the root is itself in a pattern
                  _newrootposition = virtualnode;
                  //throw new RuntimeException();
               }
               //tells that the new child node for node is the count. emigration position
               //with old address, gives ability to find new
               //130 with 150 151 children go to 140 to 2 3... this allows the 150 to 2 link 

               //for each member of the family, emigration under way
               for (int k = 0; k < familySizeOldReal; k++) {
                  //mark child node as inside the virtual store
                  marker[childPointerOldReal + k] = 1;
                  if (isUsingParentPointers() && oldrealnode != srcNodeData.getParentPointer(childPointerOldReal + k)) {
                     throw new RuntimeException();
                  }
                  //           //copy it. first the child (oldnode) then the parentoffset
                  //           //emigration done. storecount will be the new nodeid for first child of our parent node
                  //           // case parent virtualnode=140 childpointeroldreal=150
                  //           // case child  virtualnode=2 childPointerOldReal=0+k
                  //           virtualstore[0][virtualnode] = childPointerOldReal + k;
                  //           //allows to directly find the newChildpointer
                  //           //case parent 150 = 140 
                  //           //case child 0 = 2 (leaf so no childrenpointer
                  //           virtualstore[1][getChildrenPointer(childPointerOldReal + k)] = virtualnode;
                  //           int nfamsize = getFamilySize(childPointerOldReal + k);
                  //           //emigration stamps if emigrate has children. i.e if that child of parent node has children
                  //           if (nfamsize == 0)
                  //              virtualstore[2][virtualnode] = 0;
                  //           else
                  //              virtualstore[2][virtualnode] = 1;
                  //           //keep track of our parent node to link the parent data
                  //           //case parebt vt=140 oldrealnode = 130
                  //           //case cgukd vt=2 oldrealnode = 150
                  //           virtualstore[3][virtualnode] = oldrealnode;
                  if (oldrealnode == 91113) {
                     fillStore(virtualstore, virtualnode, childPointerOldReal + k);
                  } else {
                     fillStore(virtualstore, virtualnode, childPointerOldReal + k);
                  }
                  virtualnode++;
               }

            }
         }
         patterncount++;
      }
      for (int i = 0; i < patternsNum.length; i++) {
         numNodesInPattern[i] = patternsNum[i] * patternSize[i];
      }
      patternsdata[0] = patterns;
      patternsdata[1] = patternsNum;
      patternsdata[2] = patternZeros;
      patternsdata[3] = patternSize;
      patternsdata[4] = pcheck;
      patternsdata[5] = numNodesInPattern;

      //check
      for (int i = 0; i < pcheck.length; i++) {
         //System.out.println(pcheck[i] + " : " + patternsNum[i]);
         if (pcheck[i] != patternsNum[i])
            throw new RuntimeException("Bad Pattern Count for pattern #" + i + " " + pcheck[i] + " != " + patternsNum[i]);
      }
      //we know now the # of nodes in a pattern
      //System.out.println(" # of patterned nodes = " + (virtualnode - 1) + " number of nodes " + (virtualstore[0].length - 1));
      //go over unmarked nodes... i.e non patterned nodes
      //sort them out in family sizes first all single childs then all 2 size brotherhood, than 3
      for (int i = 0; i < marker.length; i++) {
         int fs = srcNodeData.getNodeFamilySize(i);
         if (fs > _maxFamilySize)
            _maxFamilySize = fs;
      }
      //will enable us to sort the non patterned nodes according to their sisterhood size
      int[] sisterHoodPositions = new int[_maxFamilySize + 1];
      int count = 0;
      //for each size of sisterhood sizes (i.e family sizes for the parents)
      for (int i = 1; i <= _maxFamilySize; i++) {
         for (int node = 0; node < marker.length; node++) {
            int famSize = srcNodeData.getNodeFamilySize(node);
            if (famSize == i) {
               int childp = srcNodeData.getNodeChildFirst(node);
               //at this stage nodes with 0 marker are non patterned nodes
               if (marker[childp] == 0) {
                  for (int j = 0; j < famSize; j++) {
                     fillStore(virtualstore, virtualnode, childp + j);
                     virtualnode++;
                     count++;
                     marker[childp + j] = 1;
                     if (childp + j == 0) {
                        System.out.println(j + "-- " + node + " chilp=" + childp);
                     }
                  }
               }
            }
         }
         sisterHoodPositions[i] = count;
         count = 0;
      }
      _positions = sisterHoodPositions;
      //if 
      //root node is the child of none. therefore, it will be the last entry.
      //since it is the child of nobody
      if (marker[0] == 0) {
         //last node will be the root node
         fillStore(virtualstore, virtualnode, 0);
         marker[0] = 1;
      } else {
         //CHECKER
         throw new RuntimeException();
      }

      //CHECKER
      int count1 = 0;
      for (int node = 0; node < marker.length; node++) {
         if (marker[node] == 0) {
            System.out.println(node + "_" + srcNodeData.getNodeFamilySize(node) + " " + srcNodeData.getNodeChildFirst(node));
            count1++;
         }
      }
      if (count1 != 0)
         throw new RuntimeException("" + count1);
      //keep the marker to know if an oldnode is patterned or not
      virtualstore[4] = marker;
      return virtualstore;
   }

   /**
    * Method reads relevant node information about the old node.
    * Virtual Store is the new node positions.
    * <br>
    * <br>
    * At index 1, links oldnode to virtual node
    * <br>
    * <br>
    * 
    * @param virtualstore the store
    * @param virtualnode the new node
    * @param oldnode the old node
    */
   private void fillStore(int[][] virtualstore, int virtualnode, int oldnode) {
      virtualstore[0][virtualnode] = oldnode;
      /** oldparent to virtual node allows a straight link to old children
       * virtualnode=1497    oldrealnode 4009    oldchild 4010   newChild 564    newChildPointer 1497
       * virtualnode=564 oldrealnode 4010    oldchild 0        newChild 0          newChildPointer 0 
       * 4009 to 564 = oldparent to virtual
       */
      //position 1 
      // OldNode -> P
      virtualstore[1][oldnode] = virtualnode;
      int nfamsize = srcNodeData.getNodeFamilySize(oldnode);
      //position 2 says if node has family
      if (nfamsize == 0) {
         virtualstore[2][virtualnode] = 0;
      } else {
         virtualstore[2][virtualnode] = 1;
      }
      //position is the parent pointer of the old node
      // P -> OldParent
      virtualstore[3][virtualnode] = srcNodeData.getParentPointer(oldnode);
      if (oldnode == 0) {
         _newrootposition = virtualnode;
      }
   }

   public byte[] getRunData() {

      //---- Trie Topology to dynamically finds the best patterns
      // the percentage to be reached by a topology to be eligible as a worthwhile pattern
      // Topologies are capped to TrieCounter.TOPOLOGY_FAMILY_SIZE_MAX 
      IntToStrings its = getTopologyValues(minimumTopologyRatio);
      //---- End of Trie Topology

      //compute the pattern data sizes 
      // index 0 = the pattern as an integer
      // index 1 = the number of pattern occurences
      // index 2 = the number of zeros in the pattern
      // index 3 = the size of the pattern
      // index 4 = checker
      // index 5 = the number of nodes in the pattern (index1 * index3)
      int[][] patterndata = new int[6][];
      // Before building the virtual store, we want to put first 0, 00, 000, 0000, ... patterns
      // they are the easiest for look ups. we do that by sorting the strings of the Topolgy Trie Values
      its.sort(true);
      //System.out.println("Contents of IntToStrings");
      //System.out.println(its.toString());
      // fill in the pattern data array and returns 
      int[][] virtualstore = buildVirtualStore(its, patterndata);
      //number of different patterns
      int patternsNum = patterndata[0].length;

      ////////// CoMPUTE BIT SIZE FOR EACH FIELD
      //get maximum starting fron root node 0. must be computed on the new childpointer
      int bitChildrenPointerSize = BitUtils.widthInBits(IntUtils.getMax(virtualstore[1], 0, virtualstore[1].length));

      //debug pattern data
      // int s = patterndata[0].length;
      // for (int j = 0; j < s; j++) {
      //    String p = patterndata[0][j] + "\t" + patterndata[1][j] + "\t" + patterndata[2][j] + "\t" + patterndata[3][j] + "\t" + patterndata[4][j];
      //    System.out.println(p);
      // }

      //// COMPUTE ZEROS COMPRESSION HEADER
      //initialization:the last node 
      int lastzeronode = 0;
      //initialization:
      int numofzerospatterned = 0;
      //for each pattern, we want to keep track of the start node
      int[] startnodes = new int[patternsNum];
      // and the number of zero family nodes as well.
      int[] zerobelows = new int[patternsNum];

      //start at 8 for the header
      int bitTotalChildrenSize = 8;
      int bitTotalParentSize = 8;

      boolean foundlastzeronode = false;

      //variable will keep track of the starting node
      int startnode = 1;
      int patternedNodes = 0;
      //for all patterns
      for (int i = 0; i < patternsNum; i++) {
         zerobelows[i] = numofzerospatterned;
         int patternrepeats = patterndata[1][i];
         int patternSize = patterndata[3][i];
         int numofZeros = patterndata[2][i];
         patternedNodes += (patternrepeats * patternSize);
         //   occurence * numberofzeros = number of zeros in the whole pattern
         numofzerospatterned += (patternrepeats * numofZeros);
         int numOfOnes = (patternSize - numofZeros);
         bitTotalChildrenSize += (numOfOnes * bitChildrenPointerSize * patternrepeats);
         startnodes[i] = startnode;
         //code to find the last full zero 
         if (!foundlastzeronode) {
            // numberofzeros != pattern size
            if (patternSize != numofZeros) {
               //we found the first pattern with non zero nodes
               foundlastzeronode = true;
               lastzeronode = startnode - 1;
            }
         }
         // occurence * size = number of nodes in the pattern
         startnode += (patternrepeats * patternSize);
      }
      // System.out.println("patterned nodes = " + patternedNodes);
      int lastpatternednode = startnode - 1;
      int onesPatterned = lastpatternednode - numofzerospatterned;
      int numNonPatternedNodes = (srcNodeData.getNumberOfNodes() - lastpatternednode);
      int albit = 8 + (onesPatterned * bitChildrenPointerSize);
      if (albit != bitTotalChildrenSize)
         throw new RuntimeException();
      int numberOfNonPatternedNodes = srcNodeData.getNumberOfNodes() - lastpatternednode;
      bitTotalChildrenSize += (numberOfNonPatternedNodes * bitChildrenPointerSize);

      ///////// END ZERO COMPRESSION HEADER
      //////////////////////
      /// DATA
      //the total number of bytes
      int dataTotalByteSize = 0;

      // PARENT
      int bitParentRefSize = 0;
      //tracks for a newnode its new parent array[node] gives the parent
      //the zero index is NOT USED
      //array[newnode] gives you the new parent node
      int pPointers[] = new int[srcNodeData.getNumberOfNodes() + 1]; //+1 cuz virtualnodes start at 1
      if (isUsingParentPointers()) {
         for (int virtualnode = 1; virtualnode < virtualstore[0].length; virtualnode++) {
            int oldrealnode = virtualstore[0][virtualnode];
            int oldChild = srcNodeData.getNodeChildFirst(oldrealnode);
            int newChild = virtualstore[1][oldChild];
            for (int j = 0; j < srcNodeData.getNodeFamilySize(oldrealnode); j++) {
               pPointers[newChild + j] = virtualnode;
            }
         }
         bitParentRefSize = BitUtils.widthInBits(IntUtils.getMax(pPointers, 0, pPointers.length));
         int lastpointer = -1;
         for (int i = 1; i < pPointers.length; i++) {
            if (lastpointer != pPointers[i]) {
               bitTotalParentSize += bitParentRefSize;
               lastpointer = pPointers[i];
            }
         }
         dataTotalByteSize += BitUtils.byteConsumed(bitTotalParentSize);
      } else {
         //write the defautl one byte header
         dataTotalByteSize += BitUtils.byteConsumed(bitTotalParentSize);
      }

      dataTotalByteSize += BitUtils.byteConsumed(bitTotalChildrenSize);

      //the byte data that will be consumed by specific datas.
      dataTotalByteSize += srcNodeData.getChildDataByteSize();

      //DO THE COPY
      ///////////////////
      int patternHeaderByteSize = 2 + (patterndata[3].length * 10); //2 bytes for length. 10 bytes per record
      int familyTableByteSize = 2 + ((_positions.length - 1) * 4); //2 bytes for length. 10 bytes per record
      int baseHeaderByteSize = ITechSpreadNodeData.SPREAD_BASIC_SIZE + patternHeaderByteSize + familyTableByteSize;
      int byteSize = baseHeaderByteSize + dataTotalByteSize;
      // System.out.println("familyTableByteSize=" + familyTableByteSize + " patternHeaderByteSize=" + patternHeaderByteSize + " dataTotalByteSize=" + dataTotalByteSize + " baseHeaderByteSize="
      //       + baseHeaderByteSize);

      //might be needed
      _tempVirtualStore = virtualstore;
      byte[] fullData = new byte[byteSize];
      //System.out.println("Full Data Array Created Size =" + byteSize);

      //COPY VARIABLE LENGTH PATTERN INFORMATION
      //The Pattern Table, with which you will be able to match an incoming node with a pattern
      // to find children pointer of node X, find pattern of X, if patterned node, find the number of 1 below
      //it and you can compute the children pointer
      int numOfPatterns = patterndata[3].length;
      int pointer = ITechSpreadNodeData.SPREAD_BASIC_SIZE;
      ShortUtils.writeShortBEUnsigned(fullData, pointer, numOfPatterns);
      // add 2 - 1 = 1
      pointer = pointer + 1;
      for (int i = 0; i < numOfPatterns; i++) {
         //Copy start node for pattern
         IntUtils.writeIntBE(fullData, pointer += 1, startnodes[i]);
         IntUtils.writeIntBE(fullData, pointer += 4, zerobelows[i]);
         fullData[pointer += 4] = (byte) patterndata[3][i]; //size
         fullData[pointer += 1] = (byte) patterndata[0][i]; //pattern
      }

      //COPY VARIABLE LENGTH FAMILY TABLE
      //START FAMILY TABLE COPY
      pointer = ITechSpreadNodeData.SPREAD_BASIC_SIZE + patternHeaderByteSize;
      //add the number of families
      ShortUtils.writeShortBEUnsigned(fullData, pointer, _positions.length - 1);
      // add 2 - 4 = -2
      pointer = pointer - 2;
      // System.out.println("For non patterned nodes, number of nodes in a family size of i");
      for (int i = 1; i < _positions.length; i++) {
         IntUtils.writeIntBE(fullData, pointer += 4, _positions[i]);
         //System.out.println("family size =" + i + "\t#families=" + (_positions[i] / i) + "\t#nodes=" + _positions[i]);
      }

      ByteObject header = new ByteObject(pdc.getBOC(), ITechSpreadNodeData.SPREAD_TYPE, ITechSpreadNodeData.SPREAD_BASIC_SIZE);
      //compute the position of each main block: 
      //offset of family information
      //position in fullData of child pointer info. It is next to Family
      int childrenOffset = baseHeaderByteSize;
      //next to children comes Data
      int parentOffset = childrenOffset + BitUtils.byteConsumed(bitTotalChildrenSize);

      int subClassDataOffset = parentOffset + BitUtils.byteConsumed(bitTotalParentSize);

      // System.out.println("BIP 4:  childrenOffset=" + childrenOffset + " parentOffset=" + parentOffset + " subClassDataOffset=" + subClassDataOffset + "\nFullData = " + byteSize + " bytes");
      // System.out.println("consumption : children=" + BitMask.byteConsumed(bitTotalChildrenSize) + " parent=" + BitMask.byteConsumed(bitTotalParentSize) + " subclass=" + getChildDataByteSize());
      // System.out.println(" Parent Flag=" + isFlagSet(FLAG_PARENT_REF) + " autochoosedata=" + isFlagSet(FLAG_DATA_AUTO_CHOOSE));
      // System.out.println("New Root Position = " + _newrootposition);
      // BUILD THE FIXED LENGTH HEADER
      //currently unused
      IntUtils.writeIntBE(fullData, pointer, 6666);
      IntUtils.writeIntBE(fullData, pointer += 2, srcNodeData.getNumberOfNodes());
      IntUtils.writeIntBE(fullData, pointer += 4, _newrootposition);
      IntUtils.writeIntBE(fullData, pointer += 4, childrenOffset);
      IntUtils.writeIntBE(fullData, pointer += 4, parentOffset);
      IntUtils.writeIntBE(fullData, pointer += 4, subClassDataOffset);
      IntUtils.writeIntBE(fullData, pointer += 4, lastpatternednode);
      IntUtils.writeIntBE(fullData, pointer += 4, lastzeronode);
      IntUtils.writeIntBE(fullData, pointer += 4, numofzerospatterned);
      //END FIXED LENGTH HEADER COPY

      //END ZEROS HEADER COPY

      //System.out.println(printPatternTable(fullData, lastpatternednode, lastzeronode, numofzerospatterned));

      //NOW COPY ALL THE NODES
      //Starting coordinate to copy the data in the single array
      //inside the header Copy in the first byte, the bit size of one value 
      header.set1(ITechSpreadNodeData.SPREAD_OFFSET_02_CHILDPOINTERBITS1, bitChildrenPointerSize);
      //specific
      int specHeaderOffset = 0;
      srcNodeData.addSpecificRunHeader(fullData, specHeaderOffset);

      // COPY PARENT DATA
      //now that parents know about the children, do a 2nd pass for letting children now about their parents
      //For Patterns, copy parent pointer only once per families
      linkParentsChildren(bitParentRefSize, pPointers, fullData, parentOffset);

      //debug data
      int[] newchildren = new int[onesPatterned + numberOfNonPatternedNodes];
      //bit coordinate
      BitCoordinate childrenCoordinate = new BitCoordinate(pdc.getUCtx(),childrenOffset, 0);
      BitCoordinate subClassDataCoordinate = new BitCoordinate(pdc.getUCtx(),subClassDataOffset, 0);

      //System.out.println("New Root=" + _newrootposition);
      int countpcopy = 0;
      int countnotpcopy = 0;
      int countNewChildren = 0;

      //pass over all the new virtual nodes. we copy their fields in that order
      //the virtual becomes real, and the old order becomes the shadow
      for (int virtualnode = 1; virtualnode < virtualstore[0].length; virtualnode++) {
         //use the old reference for accessing data
         //case parent vt = 140 oldrealnode=130
         int oldrealnode = virtualstore[0][virtualnode];
         // 1 if node has family and child pointer data 
         int useFamilyData = virtualstore[2][virtualnode];

         //CHECK START sanity check
         if (useFamilyData == 1) {
            if (srcNodeData.getNodeChildFirst(oldrealnode) == 0) {
               throw new RuntimeException();
            }
         }
         if (srcNodeData.getParentPointer(oldrealnode) != virtualstore[3][virtualnode]) {
            throw new RuntimeException();
         }
         //CHECK END

         int oldChild = srcNodeData.getNodeChildFirst(oldrealnode);
         if (virtualnode == virtualstore[0].length - 1) {
            //System.out.println(virtualnode + " Root: oldchild=" + oldChild + " oldnode=" + oldrealnode);
         }

         //CHECK START sanity check
         if (oldChild == 0 && useFamilyData == 1) {
            throw new RuntimeException();
         }
         //CHECK END

         int newChildPointer = virtualstore[1][oldChild];
         if (oldChild == 0) {
            newChildPointer = 0;
         }
         //doubleCheckNewChild(virtualstore, lastpatternednode, oldChild, newChildPointer);

         //update the newnode with the right offset
         if (useFamilyData == 1 || virtualnode > lastpatternednode) {
            //for example in the 010 pattern 2nd node has family information but we don't copy it
            //family is learned with the family table with the childpointer

            //BitMask.copyBit(fullData, famCoordinate, getFamilySize(oldrealnode), bitFamilySize);

            //System.out.println("Copying newChildPointer="+newChildPointer + " bitsize="+bitChildrenPointerSize);

            //the child pointer taking into account the swaps. 
            BitUtils.copyBits(fullData, childrenCoordinate, newChildPointer, bitChildrenPointerSize);
            newchildren[countNewChildren] = newChildPointer;
            countNewChildren++;
            if (virtualnode > lastpatternednode) {
               countnotpcopy++;
            } else {
               countpcopy++;
            }
         }
         //get family size from newChild node.
         int newfamilysize = getNewFamilySize(virtualstore, newChildPointer, lastpatternednode, patterndata[3], patterndata[5]);
         //that family size hasn't change and should be the same as the previous one
         if (newfamilysize != srcNodeData.getNodeFamilySize(oldrealnode)) {
            //        System.out.println("newnode=" + virtualnode + "\t oldnode " + oldrealnode + "\t oldchild " + oldChild + "\t newchild " + newChild + "\toldf=" + getFamilySize(oldrealnode) + "\tnewf="
            //              + newfamilysize + "\toldparent=" + getParentPointer(oldrealnode) + " newparent=" + pPointers[virtualnode]);
            //        System.out.println("lastpatternednode=" + lastpatternednode + " ");
            //        int parent = getParentPointer(oldrealnode);
            //        int newparent = pPointers[virtualnode];
            //        int newpfamilysize = getNewFamilySize(virtualstore, virtualnode, lastpatternednode, patterndata[3], patterndata[5]);
            //        System.out.println("oldparent: famSize=" + getFamilySize(parent) + " childp=" + getChildrenPointer(parent) + " newparent: famSize=" + newpfamilysize);
            throw new NullPointerException("newfamilysize:" + newfamilysize + "!=" + srcNodeData.getNodeFamilySize(oldrealnode) + " for oldnode=" + oldrealnode + " virtualnode=" + virtualnode);
         }
         //System.out.println("newnode=" + virtualnode + "\t oldnode " + oldrealnode + "\t oldchild " + oldChild + "\t newchild " + newChildPointer + "\toldf=" + getFamilySize(oldrealnode) + "\tnewf="
         //       + newfamilysize + "\toldparent=" + getParentPointer(oldrealnode) + " newparent=" + pPointers[virtualnode]);

         //uncompressed sequential copy of data and corevalues
         //copy data to its recipient
         //asks the sub-class to copy the values for that node
         srcNodeData.copyCoreValues(fullData, subClassDataCoordinate, oldrealnode);
      }

      // System.out.println("\n Finished Copying ChildrenPointer at byte " + childrenCoordinate.bytenum);
      // System.out.println("numberOfNonPatternedNodes=" + numberOfNonPatternedNodes + " # of patterned childpointers=" + countpcopy + " # of non patterned childpointer count=" + countnotpcopy);
      // System.out.println(" bytes allocated=" + BitMask.byteConsumed(bitTotalChildrenSize) + " data coord=" + dataCoordinate.bytenum);
      // System.out
      //       .println("patternedNodes=" + patternedNodes + " = [ numofzerospatterned=" + numofzerospatterned + " + onesPatterned=" + onesPatterned + " ] = " + (numofzerospatterned + onesPatterned));
      // System.out.println("number of non patterned nodes =" + numNonPatternedNodes);
      // //CHECK
      // System.out.println("Now Printing Order of Children Pointer Print ");
      // for (int k = 0; k < newchildren.length; k++) {
      //    System.out.println((k + 1) + "\t" + newchildren[k]);
      // }
      // //END DEBUG
      return fullData;
   }

   protected void linkParentsChildren(int bitParentRefSize, int[] pPointers, byte[] fullData, int parentOffset) {
      if (isUsingParentPointers()) {
         //start straight at parent offset
         BitCoordinate parentCoordinate = new BitCoordinate(pdc.getUCtx(), parentOffset, 0);
         BitUtils.copyBits(fullData, parentCoordinate, bitParentRefSize, 8);
         //System.out.println("start writing parent data at " + parentOffset + " bitsize=" + bitParentRefSize);
         int lastpointer = -1;
         int count = 0;
         for (int i = 1; i < pPointers.length; i++) {
            if (lastpointer != pPointers[i]) {
               BitUtils.copyBits(fullData, parentCoordinate, pPointers[i], bitParentRefSize);
               lastpointer = pPointers[i];
               count++;
            }
         }
         //     System.out.println("Finished writing parent data at " + parentCoordinate.bytenum + " count=" + count);
      }
   }

   /**
    * 
    */
   public int getNewFamilySize(int[][] store, int chilp, int lastPatternedNode, int[] patternSizes, int[] patternNumNodes) {
      if (chilp == 0) {
         //case of a leaf
         return 0;
      }
      if (chilp > lastPatternedNode) {
         int val = lastPatternedNode;
         //start at 1 for the 1 sized families
         for (int i = 1; i < _positions.length; i++) {
            //System.out.println("chilp="+chilp + " val="+val  );
            val += _positions[i];
            if (chilp <= val) {
               //System.out.println(i);
               return i;
            }
         }
         //System.out.println("chilp="+chilp + " val="+val  );
         throw new RuntimeException();
      } else {
         //case of patterns with ones.
         return patternSizes[getPatternID(chilp, patternNumNodes)];
      }
   }

   /**
    * 
    * @param node
    * @param patternNumNodes
    * @return
    */
   public int getPatternID(int node, int[] patternNumNodes) {
      int count = 0;
      for (int i = 0; i < patternNumNodes.length; i++) {
         count += patternNumNodes[i];
         if (node < count + 1) {
            return i;
         }
      }
      return -1;
   }
}
