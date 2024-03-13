package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.core.src4.structs.IntToInts;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkOrderedIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechNodeData;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToInt;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNodeTopo;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.string.PowerLinkStringToInt;

/**
 * Trie Analysis
 * <li> Counts number of nodes
 * <li> Family topologies
 * <li> 
 * <br>
 * <br>
 * A sub class CharTrieTopology will make a grappe suffix analysis.
 * 
 * @author Charles Bentley
 *
 */
public class NodeTopologyComputer {

   public static int TOPOLOGY_FAMILY_SIZE_MAX = 5;

   private char[]       buffer;

   IPowerTrieNodes      nodedata;

   private PDCtx        pdc;

   private TrieNodeTopo tnt;

   public NodeTopologyComputer(PDCtx pdc) {
      this.pdc = pdc;
   }

   /**
    * Adds all leaves that are below the given node
    * @param node
    * @param leaves
    */
   private void addLeaves(int node, IntBuffer leaves) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         if (nodedata.isLeaf(node)) {
            leaves.addInt(node);
         }
         nodedata.getNodeChildren(node, nodeBuffer);
      }
   }

   /**
    * morph linker to a simple IntToString.
    * Sorts the {@link IntToStrings}.
    * <br>
    * <br>
    * Write a report of the topology analysis.
    * <br>
    * <br>
    * 
    * @param topoTrie
    * @param threshold
    * @return
    */
   public IntToStrings buildTopologyTrieValues(IPowerLinkStringToInt linker, double threshold) {
      //max is the number of leaves. this is the maximum of zeros

      //the leaves are the nodes which waste the space used by firstchild and familysize data 
      int numLeaves = linker.getIntFromKeyString("l");
      int total = linker.getIntFromKeyString("z");

      int max = numLeaves;
      //
      IntToStrings src = (IntToStrings) linker.getMorph(null);
      //get all the bit values and the length of the bit word in position 1
      IntToStrings its = new IntToStrings(pdc.getUC(), src.nextempty);
      int totalint = 0;
      //System.out.println("BitString\t= #Appearances\t % of zeros covered ");
      for (int j = 0; j < src.nextempty; j++) {
         //first integer is the pattern as an integer -> make it a String.
         String s = src.strings[j];
         //number of occurences. was computed by the totalTopolgy BitTrie and store in data
         int numofpatterns = src.ints[j];

         int countzero = StringUtils.countChars(s, '0'); //we are interested in zeros.
         int tot = numofpatterns * countzero; //the total number of zeros in the pattern occurences

         //compute the percentage of leaves covered
         double percent = (double) ((double) tot / (double) max) * (double) 100;
         //System.out.println(s + " = " + numofpatterns + " - "+ percent);

         if (percent >= threshold) {
            totalint += tot;
            its.add(numofpatterns, s);
            //System.out.println(s + "\t= " + sdata + "\t" + StringUtils.prettyPercentage(tot, max, 1) + "%");
         }
      }
      //System.out.println("# of Patterned Empty Nodes  = "+totalint + " Leaves= "+max + " Full Total="+ fullTotal);
      //System.out.println("Empty Nodes Coverage = " + StringUtils.prettyPercentage(totalint, max, 1) + "%");
      //System.out.println("Compressing Ratio = " + StringUtils.prettyPercentage(totalint, fullTotal, 1) + "%");
      return its;
   }

   public void compute() {
      int type = tnt.paramType;
      int paramNode = tnt.paramNode;

      if (type == TrieNodeTopo.TOPO_TYPE_01_NUM_NODES) {
         if (tnt.paramTypeSub == TrieNodeTopo.TOPO_SUB_TYPE_01_NUM_LEAVES) {
            if (tnt.all) {
               tnt.results = runCountLeavesAll(paramNode);
            } else {
               tnt.resultCount = runCountLeaves(paramNode);
            }
         } else if (tnt.paramTypeSub == TrieNodeTopo.TOPO_SUB_TYPE_02_NUM_FLAGS) {
            tnt.resultCount = runCountFlag(paramNode);
         } else {
            if (tnt.all) {
               tnt.results = runCountNumNodesBelow(paramNode);
            } else {
               tnt.resultCount = runCountNodes(paramNode);
            }
         }
      } else if (type == TrieNodeTopo.TOPO_TYPE_02_FAMILIES) {
         tnt.results = runCountFamilies(paramNode);
      } else if (type == TrieNodeTopo.TOPO_TYPE_03_PATTERN) {
         buffer = new char[TOPOLOGY_FAMILY_SIZE_MAX + 1];
         PowerLinkStringToInt linker = new PowerLinkStringToInt(pdc);
         runCountTopo(paramNode, linker);
         double threshold = tnt.parameterDouble;
         tnt.resultStrings = buildTopologyTrieValues(linker, threshold);
      } else if (type == TrieNodeTopo.TOPO_TYPE_06_DEPTH) {
         if (tnt.all) {
            tnt.results = runCountDepth(paramNode);
         } else {
            tnt.resultCount = getNodeDepth(paramNode);
         }
      } else if (type == TrieNodeTopo.TOPO_TYPE_05_HEIGHTS) {
         tnt.results = runCountHeight(paramNode);
         if (!tnt.all) {
            tnt.resultCount = tnt.results[paramNode];
         }
      } else if (type == TrieNodeTopo.TOPO_TYPE_07_MAX_FAMILY_SIZE) {
         tnt.resultCount = runCountMaxFamilySize(paramNode);
      } else if (type == TrieNodeTopo.TOPO_TYPE_04_GRAPPES) {
         PowerLinkStringToInt linker = new PowerLinkStringToInt(pdc);
         runCountGrappes(paramNode, linker);
      }
   }

   private int[] getArrayNodeIntToIntLinker() {
      return new int[nodedata.getLastNode() + 1];
   }

   /**
    * Remove the two mark bits
    * @param i
    * @return
    */
   public String getBitTopologString(int i) {
      String s = Integer.toBinaryString(i).substring(0);
      //remove the mark bit
      String n = s.substring(1);
      int newi = BitUtils.toInteger(n);
      return Integer.toBinaryString(newi).substring(1);
   }

   public int getNodeDepth(int node) {
      int pnode = nodedata.getNodeParent(node);
      int nodeDepth = 0;
      while (pnode != IPowerTrieNodes.NOT_A_NODE) {
         nodeDepth++;
         pnode = nodedata.getNodeParent(pnode);
      }
      return nodeDepth;
   }

   /**
    * a flag
    * @param node
    * @return
    */
   private String getNodeSignature(int node) {
      StringBBuilder sb = new StringBBuilder(pdc.getUC());
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int numFlags = nodedata.getTech().get1(ITechNodeData.NODEDATA_OFFSET_03_NUM_FLAGS1);
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         int data = nodedata.getNodePayload(node);
         sb.append(data);
         for (int i = 0; i < numFlags; i++) {
            int flag = 1 << i;
            if (nodedata.hasNodeFlag(node, flag)) {
               sb.append('1');
            } else {
               sb.append('0');
            }
         }
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return sb.toString();
   }

   /**
    * Creates a mapping of bit patterns (00,11,010,111,...) mapped with the number of times that pattern is repeated.
    * <br>
    * <br>
    * The pattern is accepted if it is above or equal the percentage threshold given in parameter.
    * <br>
    * <br>
    * Return the patterns with enough zeros
    * <br>
    * <br>
    * @param topoTrie a Int To Int Map.
    * @return {@link IntToStrings}
    */
   public IntToStrings getTopologyTrieValues(IPowerLinkOrderedIntToInt topoTrie, double threshold) {
      //max is the number of leaves. this is the maximum of zeros

      int max = topoTrie.getData(1);

      //get all the bit values and the length of the bit word in position 1
      int[] top = topoTrie.getValuesDatas();
      IntToStrings its = new IntToStrings(pdc.getUC(), top.length / 2);
      int totalint = 0;
      //System.out.println("BitString\t= #Appearances\t % of zeros covered ");
      for (int j = 0; j < top.length; j += 2) {
         //first integer is the pattern as an integer -> make it a String.
         String s = getBitTopologString(top[j]);
         //number of occurences. was computed by the totalTopolgy BitTrie and store in data
         int numofpatterns = top[j + 1];
         int countzero = StringUtils.countChars(s, '0'); //we are interested in zeros.
         int tot = numofpatterns * countzero; //the total number of zeros in the pattern occurences
         //compute the percentage of leaves covered
         double percent = (double) ((double) tot / (double) max) * (double) 100;
         //System.out.println(s + " = " + numofpatterns + " - "+ percent);
         if (percent >= threshold) {
            totalint += tot;
            its.add(numofpatterns, s);
            //System.out.println(s + "\t= " + sdata + "\t" + StringUtils.prettyPercentage(tot, max, 1) + "%");
         }
      }
      //System.out.println("# of Patterned Empty Nodes  = "+totalint + " Leaves= "+max + " Full Total="+ fullTotal);
      //System.out.println("Empty Nodes Coverage = " + StringUtils.prettyPercentage(totalint, max, 1) + "%");
      //System.out.println("Compressing Ratio = " + StringUtils.prettyPercentage(totalint, fullTotal, 1) + "%");
      return its;
   }

   public void reset(TrieNodeTopo tnt, IPowerTrieNodes fastNodeData) {
      this.tnt = tnt;
      this.nodedata = fastNodeData;
   }

   /**
    * Computes {@link TrieNodeTopo#TOPO_TYPE_06_DEPTH}
    * <br>
    * <br>
    * Strategy to compute it.
    * <br>
    * 
    * At each leaf. go back up to root and update the depth of nodes
    * <br>
    * <br>
    * 
    * @param node
    */
   private int[] runCountDepth(int node) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int[] depth = getArrayNodeIntToIntLinker();
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         int nodeDepth = getNodeDepth(node);
         depth[node] = nodeDepth;
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return depth;
   }

   /**
    * Compute the family distribution for all nodes in the tree starting at node inclusive.
    * <br>
    * <br>
    * 
    * @param node
    * @return
    */
   private int[] runCountFamilies(int node) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int[] results = new int[nodedata.getTech().get2(ITechNodeData.NODEDATA_OFFSET_06_MAX_FAMILYSIZE2)];
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         int familySize = nodedata.getNodeFamilySize(node);
         results[familySize]++;
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return results;
   }

   private int runCountFlag(int node) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int count = 0;
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         if (nodedata.hasNodeFlag(node, tnt.parameterFlag) == tnt.parameterFlagBool) {
            count++;
         }
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return count;
   }

   /**
    * For all nodes
    * <br>
    * <br>
    * Returns nodes which are the root of the same data/flag pattern.
    * <br>
    * <br>
    * 
    * @param node
    * @param linker
    */
   private void runCountGrappes(int node, IPowerLinkStringToInt linker) {
      int[] numBelow = runCountNumNodesBelow(node);
      int threshold = 6;
      int[] sigPointers = getArrayNodeIntToIntLinker();
      String[] sis = new String[sigPointers.length];

      for (int i = 0; i < numBelow.length; i++) {
         int val = numBelow[i];
         if (val != 0 && val <= threshold) {
            String sig = getNodeSignature(i);
            sis[i] = sig;
            sigPointers[i] = linker.incrementIntFromKeyString(1, sig);
         }
      }
      int grapeThreshold = 4;

      // accept grapes with enough occurences
      IntToStrings vals = (IntToStrings) linker.getMorph(null);
      for (int i = 0; i < vals.nextempty; i++) {
         if (vals.ints[i] > grapeThreshold) {

            //add the grapp
         }
      }

      IntBuffer grappedNodes = new IntBuffer(pdc.getUC());
      //elagage
      node = nodedata.getRootNode();
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int count = 0;
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         int grapePointer = sigPointers[node];
         if (grapePointer != 0) {
            int val = linker.getIntFromPointer(grapePointer);
            if (val > grapeThreshold) {
               //this node is grapped
               grappedNodes.addInt(node);
               //do not add children since node is grapped
               continue;
            }
         }
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      tnt.resultBuffer = grappedNodes;
      tnt.resultStrings = vals;

      //we could keep one instance and remove. but this would make pointers expensive. We prefer a new trie
      //with only grappes so this pointer is smaller. accept until grappe trie has 255 nodes.

      //1: first occurrence is chosen. child pointer is read.. parent pointer is removed.
      //2: childpointer is linked to the grappe sig
      //3: grappedNode is flagged as such.
      //4: when ever a node is found matching this signature. signature gives the childpointer and node is grapped as in #3

      //process is known as grapping the nodedata.
   }

   /**
    * Computes {@link TrieNodeTopo#TOPO_TYPE_05_HEIGHTS}
    * <br> 
    * <br> 
    * Strategy to compute it.
    * At each leaf. go back up to root and update the depth of nodes
    * <br>
    * <br>
    * <br>
    * @param node
    */
   private int[] runCountHeight(int node) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int[] heights = getArrayNodeIntToIntLinker();
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         if (nodedata.isLeaf(node)) {
            int pnode = nodedata.getNodeParent(node);
            int nodeHeight = 0;
            while (pnode != IPowerTrieNodes.NOT_A_NODE) {
               nodeHeight++;
               if (nodeHeight > heights[pnode]) {
                  heights[pnode] = nodeHeight;
               }
               pnode = nodedata.getNodeParent(pnode);
            }
         }
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return heights;
   }

   /**
    * Count the leaves below
    * @param node
    */
   private int runCountLeaves(int node) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int count = 0;
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         if (nodedata.isLeaf(node)) {
            count++;
         }
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return count;
   }

   private int[] runCountLeavesAll(int node) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int[] counts = getArrayNodeIntToIntLinker();
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         counts[node] = runCountLeaves(node);
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return counts;
   }

   private int runCountMaxFamilySize(int node) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int max = 0;
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         int famSize = nodedata.getNodeFamilySize(node);
         if (famSize > max) {
            max = famSize;
         }
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return max;
   }

   /**
    * For node topologies.
    * <br>
    * <br>
    * Iteration is
    * @param node
    * @param mode
    * @return
    */
   private int runCountNodes(int node) {
      //#debug
      System.out.println("#NodeToplogyComputer#countingNodes for " + node);
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      int count = 0;
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         //#debug
         System.out.println(nodedata.toStringNode(node));
         count++;
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      //#debug
      System.out.println("#NodeToplogyComputer#finalCount = " + count);
      return count;
   }

   /**
    * Compute nodes below topology for all nodes below the given node.
    * <br>
    * <br>
    * Store the value in a {@link IntToInts} linker.
    * <br>
    * <br>
    * 
    * @param node
    * @param linker
    */
   private int[] runCountNumNodesBelow(int node) {
      IntBuffer leaves = new IntBuffer(pdc.getUC());
      addLeaves(node, leaves);
      int[] counts = getArrayNodeIntToIntLinker();
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         counts[node] = runCountNodes(node);
         nodedata.getNodeChildren(node, nodeBuffer);
      }
      return counts;
   }

   private void runCountTopo(int node, IPowerLinkStringToInt linker) {
      IntBuffer nodeBuffer = new IntBuffer(pdc.getUC());
      nodeBuffer.addInt(node);
      while (nodeBuffer.getSize() != 0) {
         node = nodeBuffer.removeLast();
         runCountTopoInside(node, linker);
         nodedata.getNodeChildren(node, nodeBuffer);
      }
   }

   /**
    * Increment the family pattern of this node
    * @param node
    */
   private void runCountTopoInside(int node, IPowerLinkStringToInt linker) {
      int familySize = nodedata.getNodeFamilySize(node);
      linker.incrementIntFromKeyString(1, "total");
      if (familySize == 0) {
         linker.incrementIntFromKeyString(1, "leaves");
      } else {
         if (familySize <= TOPOLOGY_FAMILY_SIZE_MAX) {
            // key of the Topology to the BitTrie: Topology = familySize and Pattern
            // Key Built
            IntBuffer children = new IntBuffer(pdc.getUC());

            nodedata.getNodeChildren(node, children);
            int size = children.getSize();
            int[] ref = children.getIntsRef();
            for (int i = 1; i <= size; i++) {
               int childNode = ref[i];
               //get their family size
               int famsize = nodedata.getNodeFamilySize(childNode);
               int childIndex = i - 1;
               if (famsize != 0) {
                  buffer[childIndex] = '1';
               } else {
                  buffer[childIndex] = '0';
               }
            }
            linker.incrementIntFromKeyString(1, buffer, 0, familySize);
         }
      }
   }

}
