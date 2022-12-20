import java.io.*;
import java.util.*;
/**
 * This is the implementation of the HUS-Span algorithm (for Negative item).
 * It is used for one of comparing algorithm in experiment.
 *
 * HUS-Span：
 * @article{wang2016efficiently,
 *        title={On efficiently mining high utility sequential patterns},
 * 	      author={Wang, Jun-Zhe and Huang, Jiun-Long and Chen, Yi-Cheng},
 * 	      journal={Knowledge and Information Systems},
 * 	      volume={49},
 * 	      number={2},
 * 	      pages={597--627},
 * 	      year={2016},
 * 	      publisher={Springer}
 * }
 *
 */
public class HUSPanN {
    public long startTimestamp = 0;
    public long endTimestamp = 0;

    public double databaseUtility;
    public double minUtility = 0;

    public int huspCount = 0;
    public int totalSequence = 0;
    public static Map<Integer, Integer> mapItemToNSWU;
    public static List<List<Map<Integer,PairVal>>> matrixSet;
    public int candidateCount = 0;
    BufferedWriter writer = null;

    /**
     * Run HUS-Span algorithm
     *
     * @param input
     * @param output
     * @param minUtilityRatio
     * @throws IOException
     */
    public void runAlgorithm(String input, String output, double minUtilityRatio) throws IOException {

        MemoryLogger.getInstance().reset();

        startTimestamp = System.currentTimeMillis();
        mapItemToNSWU = new TreeMap<>();

        //-------------------
        matrixSet=new ArrayList<>();
        //-------------------
        //first scan:
        // a) calculate utility of each sequence(数据集已给出)
        // b) calculate nswu of each 1-sequence

        BufferedReader myInput = null;
        String thisLine = null;
        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
            while ((thisLine = myInput.readLine()) != null) {

                Set<Integer> tmpSet = new HashSet<Integer>();
                String[] spilts = thisLine.split(" -2 ");
                String NSU = spilts[1].split(" ")[0];
                String SU = spilts[1].split(" ")[1];
                int SUtility = Integer.parseInt(SU.substring(SU.indexOf(":") + 1));
                int NSUtility = Integer.parseInt(NSU.substring(NSU.indexOf(":") + 1));
                databaseUtility += SUtility; // update the utility of each sequence
                totalSequence += 1;  // update the number of sequences
                spilts[0] = spilts[0].substring(0, spilts[0].lastIndexOf(" -1")).trim();
                String[] itemsetString = spilts[0].split(" -1 ");

                for (int i = 0; i < itemsetString.length; i++) {
                    String[] itemAndUtility = itemsetString[i].trim().split(" ");

                    //确保项集中的项按照相同的从小到大顺序排序
                    /*List<UItem> list=new ArrayList<>();
                    for (String val : itemAndUtility) {
                        Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
                        Integer utility = Integer
                                .parseInt(val.trim().substring(val.trim().indexOf("[") + 1, val.trim().indexOf("]")));
                        UItem uItem=new UItem();
                        uItem.setItem(item);
                        uItem.setUtility(utility);
                        list.add(uItem);
                    }
                    Collections.sort(list,new Comparator<UItem>(){
                        public int compare(UItem o1, UItem o2) {
                            return o1.getItem()-o2.getItem();
                        }
                    } );*/
                    for (String val : itemAndUtility) {
                        Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
                         if (!tmpSet.contains(item)) {
                            Integer nswu = mapItemToNSWU.get(item);
                            if (nswu == null) {
                                mapItemToNSWU.put(item, NSUtility);
                                tmpSet.add(item);
                            } else {
                                mapItemToNSWU.put(item, nswu + NSUtility);
                                tmpSet.add(item);
                            }
                        }

                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                try {
                    myInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        minUtility = databaseUtility * minUtilityRatio;
        //second scan:
        //    a) construct new maxtrix of each seuqence
        //    b) construct new utility-chain of each 1-sequence
        List<Node> nodeList = new ArrayList<>();
        Map<Integer, Node> mapItemToNode = new HashMap<>();
        // Filter the unpromising 1-sequences: SWU >= minUtil
        for (Integer item : mapItemToNSWU.keySet()) {
            if (mapItemToNSWU.get(item) >= minUtility) {
                Node node = new Node();
                node.addItemSet(String.valueOf(item));
                nodeList.add(node);
                mapItemToNode.put(item, node);
            }
        }


        System.out.println("TEST: 1-sequences's size " + nodeList.size());

        // build matrix set and utility-chain
        try {
            int sid = 0;
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
            while ((thisLine = myInput.readLine()) != null) {
                sid++;
                List<Map<Integer,PairVal>> matrix=new ArrayList<>();
                String[] spilts = thisLine.split(" -2 ");
                int remainingUtility = 0;
                List<List<UItem>> sequence = new ArrayList<>();
                spilts[0] = spilts[0].substring(0, spilts[0].lastIndexOf(" -1")).trim();
                String[] itemsetString = spilts[0].split(" -1 ");
                for (int i = 0; i < itemsetString.length; i++) {

                    String[] itemAndUtility = itemsetString[i].trim().split(" ");
                    List<UItem> tmp = new ArrayList<>();

                    for (String val : itemAndUtility) {
                        Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
                        Integer utility = Integer
                                .parseInt(val.trim().substring(val.trim().indexOf("[") + 1, val.trim().indexOf("]")));
                        Integer nswu = mapItemToNSWU.get(item);
                        if (nswu >= minUtility) {
                            UItem uItem = new UItem();
                            uItem.setItem(item);
                            uItem.setUtility(utility);
                            tmp.add(uItem);
                            if (utility>=0){
                                remainingUtility += utility;
                            }

                        }

                    }

                    if (tmp.size() == 0) {
                        continue;
                    }

                    // add this sequence
                    sequence.add(tmp);
                }


                int tid = 0;

                // for each sequence in the set of uitemList
                for (List<UItem> uitemList : sequence) {
                    tid++;
                    //bug
                    Map<Integer,PairVal> mapItemset=new LinkedHashMap<>();
                    for (UItem uItem : uitemList) {
                        Integer item=uItem.getItem();
                        Integer utility= uItem.getUtility();
                        if (utility>=0){
                            remainingUtility = remainingUtility - utility;
                        }

                        mapItemset.put(item,new PairVal(utility,remainingUtility));

                        Node node = mapItemToNode.get(item);
                        Element element = new Element();
                        element.setTid(tid);
                        element.setAcu(utility);
                        element.setRu(remainingUtility);
                        node.addElement(sid, element);
                    }

                    matrix.add(mapItemset);

                }

                matrixSet.add(matrix);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                try {
                    myInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // System.out.println("--------------Test Utility Chain-----------------");
        // for(Node node:nodeList) {
        // node.printNode();
        // }
        // System.out.println("-------------------------------------------------");
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
        // check the memory usage
        MemoryLogger.getInstance().checkMemory();
        candidateCount += nodeList.size();

        //
        // for each 1-sequences
        for (Node node : nodeList) {
                Integer utility = node.getUtility();
                if (utility >= minUtility) {
                    huspCount++;
                    writeToFile(node.getSequence(), utility);
                }
                if(node.getPEU()>=minUtility){
                    HUSSpan(node);
                }

        }


        // check the memory usage
        MemoryLogger.getInstance().checkMemory();

        writer.close();
        endTimestamp = System.currentTimeMillis();
    }


    /**
     * HUS-Span algorithm
     *
     * @param node
     * @throws IOException
     */
    private void HUSSpan(Node node)
            throws IOException {

        List<List<Node>> lists=extension(node);
        // check the memory usage
        MemoryLogger.getInstance().checkMemory();

        // for each sequence in candidates
        if (lists != null) {

            // I-extension
            List<Node> iNode = lists.get(0);
            candidateCount += iNode.size();
            for (Node n : iNode) {
               /* String tmp = "{";
                for (String str : n.getSequence()) {
                    tmp += str + " ";
                }
                tmp += "}";
                if (tmp.equals("{142665-1167953-1186415-1804621-2169558-3707668-4660963-4959659 1278446-1533246-2395106-2651861-3706360-4767864 510245-715201-795719-881682-2149554-2415966-2653181-3007531-3481498 }")){
                    System.out.println("@@@@@@@@@@@@@@@@@@");
                }*/
                Integer utility = n.getUtility();
                if (utility >= minUtility) {
                    huspCount++;
                    writeToFile(n.getSequence(), utility);
                }
                // call the HUSSpan function
                if (n.getPEU()>=minUtility){
                    HUSSpan(n);
                }



            }

            // S-extension
            List<Node> sNode = lists.get(1);
            candidateCount += sNode.size();
            for (Node n : sNode) {
               /* String tmp = "{";
                for (String str : n.getSequence()) {
                    tmp += str + " ";
                }
                tmp += "}";
                if (tmp.equals("{142665-1167953-1186415-1804621-2169558-3707668-4660963-4959659 1278446-1533246-2395106-2651861-3706360-4767864 510245-715201-795719-881682-2149554-2415966-2653181-3007531-3481498 }")){
                    System.out.println("@@@@@@@@@@@@@@@@@@");
                }*/
                Integer utility = n.getUtility();
                if (utility >= minUtility) {
                    huspCount++;
                    writeToFile(n.getSequence(), utility);
                }
                // call the HUSSpan function
                if (n.getPEU()>=minUtility){
                    HUSSpan(n);
                }


            }
        }

    }
    private List<List<Node>> extension(Node node) {

        // the utility-chain of this node
        Map<Integer, List<Element>> uc = node.getu_chain();
        
        List<String> sequence = node.getSequence();
        Integer lastItem = getLastItem(sequence);
        Set<Integer> sidSet = uc.keySet();
        //不是按照顺序排的
        List<Integer> itemsForSExtension = new ArrayList<Integer>();
        List<Integer> itemsForIExtension = new ArrayList<Integer>();

        Map<Integer, Integer> iMap = new HashMap<Integer, Integer>();
        Map<Integer, Integer> sMap = new HashMap<Integer, Integer>();

        // calculate the ilist and slist with the PEU upper bound
        for (Integer sid : sidSet) {
            //Map<Integer,PairVal[]> matrix = matrixSet.get(sid-1);
            List<Map<Integer,PairVal>> matrix = matrixSet.get(sid-1);
            List<Element> elementList = uc.get(sid);

            int sPeu = node.getSequencePEU(sid);

            // I-extension
            Set<Integer> tmp = new HashSet<>();
            for (Element e : elementList) {
                int tid = e.getTid().intValue();
                Map<Integer,PairVal> mapItemset=matrix.get(tid-1);
                for (Integer key:mapItemset.keySet()) {
                    if (key>lastItem){
                        tmp.add(key);
                        if (!itemsForIExtension.contains(key)) {
                            itemsForIExtension.add(key);

                        }
                    }
                }
            }

            // update the PEU upper bound----RSU(t,s)=PEU(α,s)
            for (Integer key : tmp) {
                if (iMap.get(key) == null) {
                    iMap.put(key, sPeu);
                } else {
                    iMap.put(key, iMap.get(key) + sPeu);
                }
            }
            tmp.clear();


            // S-extension
            Element ele = elementList.get(0);
            Integer tid = ele.getTid();
            for (int i = tid; i < matrix.size(); i++) {
                Map<Integer,PairVal> mapItemset=matrix.get(i);
                for (Integer key:mapItemset.keySet()) {
                    tmp.add(key);
                    if (!itemsForSExtension.contains(key)) {
                        itemsForSExtension.add(key);

                    }
                }
            }
            // update the PEU upper bound-----------RSU(t,s)=PEU(α,s)
            for (Integer key : tmp) {
                if (sMap.get(key) == null) {
                    sMap.put(key, sPeu);
                } else {
                    sMap.put(key, sMap.get(key) + sPeu);
                }
            }
        }

        // Early prune
        if (itemsForSExtension.size() == 0 && itemsForIExtension.size() == 0) {
            return null;
        }

        // remove the unpromising item in the set of ilist
        for (Integer key : iMap.keySet()) {
            Integer rsuValue = iMap.get(key);
            if (rsuValue < minUtility) {
                itemsForIExtension.remove(key);
            }
        }

        // remove the unpromising item in the set of slist
        for (Integer key : sMap.keySet()) {
            Integer rsuValue = sMap.get(key);
            if (rsuValue < minUtility) {
                itemsForSExtension.remove(key);
            }
        }
        //默认升序排列
        Collections.sort(itemsForIExtension);
        Collections.sort(itemsForSExtension);

        // construct the utility-chain
        List<Node> iNodeList = new ArrayList<>();
        List<Node> sNodeList = new ArrayList<>();
        Map<Integer, Node> mapToINode = new HashMap<>();
        Map<Integer, Node> mapToSNode = new HashMap<>();

        // I-extension
        for (Integer item : itemsForIExtension) {
            Node n = new Node();
            for (int i = 0; i < sequence.size(); i++) {
                if (i != sequence.size() - 1) {
                    n.getSequence().add(sequence.get(i));
                } else {
                    n.getSequence().add(sequence.get(i) + "-" + item);
                }
            }
            iNodeList.add(n);
            mapToINode.put(item, n);
        }

        // S-extension
        for (Integer item : itemsForSExtension) {
            Node n = new Node();
            for (int i = 0; i < sequence.size(); i++) {
                n.getSequence().add(sequence.get(i));
            }
            n.getSequence().add(item + "");
            sNodeList.add(n);
            mapToSNode.put(item, n);
        }

        // scan each sid in the utility-chain (this projected DB)从这里开始调试----------------------
        for (Integer sid : sidSet) {
            // scan the database
            List<Map<Integer,PairVal>> matrix = matrixSet.get(sid-1);
            List<Element> elementList = uc.get(sid);

            // for each element (subsequence) in sid
            for (Element e : elementList) {
                int tid = e.getTid().intValue();
                // I-extension
                Map<Integer,PairVal> mapItemset=matrix.get(tid-1);
                for (Integer key:mapItemset.keySet()){
                    if (key>lastItem && itemsForIExtension.contains(key)){
                        Element ele = new Element();
                        ele.setTid(tid);
                        ele.setAcu(e.getAcu()+mapItemset.get(key).getAcUtility());
                        ele.setRu(mapItemset.get(key).getRemainingUtility());
                        Node node2 = mapToINode.get(key);
                        node2.addElement(sid, ele);
                    }
                }

                // S-extension
                for (int i = tid; i < matrix.size(); i++) {
                    Map<Integer,PairVal> mapItemset2=matrix.get(i);
                    for (Integer key:mapItemset2.keySet()) {
                        if (itemsForSExtension.contains(key)) {
                            Element ele = new Element();
                            ele.setTid(i+1);
                            ele.setAcu(e.getAcu()+mapItemset2.get(key).getAcUtility());
                            ele.setRu(mapItemset2.get(key).getRemainingUtility());
                            Node node2 = mapToSNode.get(key);
                            node2.addElementForSExtendsion(sid, ele);

                        }
                    }
                }
            }


        }

        // check the memory usage
        MemoryLogger.getInstance().checkMemory();

        List<List<Node>> lists = new ArrayList<List<Node>>();
        lists.add(iNodeList);
        lists.add(sNodeList);


        return lists;
    }


    public void printStats() {
        System.out.println("===========  HUS-Span v4.0 ALGORITHM - STATS =========");
        System.out.println(" Total utility of DB: " + databaseUtility);
        System.out.println(" minUtility: "+ minUtility);
        System.out.println(" Total time: " + (endTimestamp - startTimestamp)/1000.0 + " s");
        System.out.println(" Max memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" HUSPs: " + huspCount);
        System.out.println(" Candidates: " + candidateCount);
        System.out.println("===================================================");
    }


    /**
     * Get last item in a sequence
     *
     * @param sequence
     * @return
     */
    private Integer getLastItem(List<String> sequence) {
        String lastItemset = sequence.get(sequence.size() - 1);
        Integer lastItem = -1;
        if (!lastItemset.contains("-")) {
            lastItem = Integer.parseInt(lastItemset);
        } else {
            String[] splits = lastItemset.split("-");
            lastItem = Integer.parseInt(splits[splits.length - 1]);
        }

        return lastItem;
    }


    private void writeToFile(List<String> sequence, Integer utility) throws IOException {
        String tmp = "{";
        for (String str : sequence) {
            tmp += str + " ";
        }
        tmp += "}";


        writer.write(tmp+":"+utility);
         writer.newLine();
         writer.flush();
    }

}
