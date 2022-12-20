
import java.io.*;
import java.util.*;
/**
 * This is the implementation of the FHUSN algorithm using NPEU (pruning strategy 3).
 * <br/><br/>
 *  Xiaojie Zhang and Fuyin Lai and Guoting chen and Wensheng gan.
 *  Mining High-Utility Sequences with Positive and Negative Values.
 *  submitted to Information Science, 2022.
 * <br/>
 *
 * ProUM：
 * @article{ProUMWensheng2020,
 *        author    = {Wensheng Gan and Jerry Chun{-}Wei Lin and Jiexiong Zhang and Han{-}Chieh Chao and Hamido Fujita and Philip S. Yu},
 * 	      title     = {{ProUM}: Projection-based utility mining on sequence data},
 * 	      journal   = {Inf. Sci.},
 * 	      volume    = {513},
 *        pages     = {222--240},
 * 	      year      = {2020},
 * }
 */
public class FHUSN_P {
    public long startTimestamp = 0;
    public long endTimestamp = 0;

    public double databaseUtility=0;
    public double minUtility = 0;

    public int huspCount = 0;
    public int totalSequence = 0;

    public static Map<Integer, Integer> mapItemToNSWU;
    public static Map<Integer, Integer> revisedMapItemToNSWU;
    public static Map<Integer, Integer> revisedMapSidToNSU;
    public static Map<Integer, UtilityArray[]> revisedUtilityArray;
    public int candidateCount = 0;
    BufferedWriter writer = null;

    /**
     * Run proUM algorithm
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
        revisedMapItemToNSWU=new TreeMap<>();
        revisedUtilityArray=new HashMap<>();
        revisedMapSidToNSU=new HashMap<>();
        //first scan:
        // a) calculate databaseUtility by SU
        // b) calculate NSWU of each 1-sequence store in mapItemToNSWU

        BufferedReader myInput = null;
        String thisLine = null;

        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
            while ((thisLine = myInput.readLine()) != null) {
                Set<Integer> tmpSet = new HashSet<>();
                String[] spilts = thisLine.split(" -2 ");
                String NSU = spilts[1].split(" ")[0];
                String SU = spilts[1].split(" ")[1];
                int SUtility = Integer.parseInt(SU.substring(SU.indexOf(":") + 1));
                int NSUtility = Integer.parseInt(NSU.substring(NSU.indexOf(":") + 1));
                databaseUtility += SUtility; // update the utility of each sequence
                totalSequence += 1;  // update the number of sequences
                String itemsetString = spilts[0].substring(0, spilts[0].lastIndexOf(" -1")).trim();
                String[] itemsets = itemsetString.split(" -1 ");

                for (int i = 0; i < itemsets.length; i++) {
                    String[] itemAndUtility = itemsets[i].trim().split(" ");
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
        //    a) remove low nswu item form DB to form revised DB
        //    b) calculate revised NSWU of each 1-sequence
        //    c) construct utility_array of revised DB

        Map<Integer, List<List<UItem>>> revisedDataBase = new HashMap<>();

        try {
            int sid = 0;
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
            while ((thisLine = myInput.readLine()) != null) {
                sid++;
                String[] spilts = thisLine.split(" -2 ");
                int remainingUtility = 0;
                int NSUtility = 0;
                List<List<UItem>> sequence = new ArrayList<>();
                String itemsetString = spilts[0].substring(0, spilts[0].lastIndexOf(" -1")).trim();
                String[] itemsets = itemsetString.split(" -1 ");
                int size=0;
                //get revised sequence
                for (int i = 0; i < itemsets.length; i++) {
                    String[] itemAndUtility = itemsets[i].trim().split(" ");
                    List<UItem> tmpItemset = new ArrayList<>();

                    for (String val : itemAndUtility) {
                        Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
                        Integer utility = Integer
                                .parseInt(val.trim().substring(val.trim().indexOf("[") + 1, val.trim().indexOf("]")));
                        Integer nswu = mapItemToNSWU.get(item);
                        if (nswu >= minUtility) {
                            UItem uItem = new UItem();
                            uItem.setItem(item);
                            uItem.setUtility(utility);
                            tmpItemset.add(uItem);
                            size++;
                            if (utility>=0){
                                remainingUtility += utility;
                                NSUtility+=utility;
                            }

                        }

                    }

                    if (tmpItemset.size() == 0) {
                        continue;
                    }

                    //add revised itemset to a revised sequence
                    sequence.add(tmpItemset);
                }
                // add this sequence to revised DB
                revisedDataBase.put(sid, sequence);

                //get revised sequence NSU
                revisedMapSidToNSU.put(sid,NSUtility);
                //get revised utilityarray of current sequence
                int tid = 0;
                int eid=1;//eid=itemId
                UtilityArray[] utilityArrays=new UtilityArray[size+1];
                revisedUtilityArray.put(sid,utilityArrays);
                //store last same item and itemid
                Map<Integer, Integer> mapItemToLastPos=new HashMap<>();
                //store the last itemset's item and itemid
                Map<Integer, Integer> mapItemToLastEid=new HashMap<>();
                Set<Integer> tmpSet = new HashSet<>();
                for (List<UItem> uitemList : sequence) {
                    tid++;
                    for (int i = 0; i < uitemList.size(); i++) {
                        Integer item=uitemList.get(i).getItem();
                        Integer utility=uitemList.get(i).getUtility();
                        if (utility>=0){
                            remainingUtility -= utility;
                        }
                        if (!tmpSet.contains(item)) {
                            Integer revisednswu = revisedMapItemToNSWU.get(item);
                            if (revisednswu == null) {
                                revisedMapItemToNSWU.put(item, NSUtility);
                                tmpSet.add(item);
                            } else {
                             //   System.out.println("test");
                                revisedMapItemToNSWU.put(item, revisednswu + NSUtility);
                                tmpSet.add(item);
                            }

                        }

                        utilityArrays[eid]=new UtilityArray(tid,item,utility,remainingUtility);
                        //i=0 means the first item of certain itemset of current sequence
                        if (i==0&&!mapItemToLastEid.isEmpty()){
                            for (Integer lastEid:mapItemToLastEid.values()) {
                                utilityArrays[lastEid].setNext_eid(eid);
                            }
                            mapItemToLastEid.clear();
                        }
                        mapItemToLastEid.put(item,eid);

                        if(mapItemToLastPos.containsKey(item)){
                            Integer lastPos=mapItemToLastPos.get(item);
                            utilityArrays[lastPos].setNext_pos(eid);
                        }
                        mapItemToLastPos.put(item,eid);
                        eid++;

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

        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));

        for (Integer item : revisedMapItemToNSWU.keySet()) {
            int totalUtility =0;
            //----new add----
            int PEU =0;
            Map<Integer,Integer> mapSidToPeu=new HashMap<>();
            //----new add end----
            //NSWU >= minutil   (NSWU:pruning strategy 1)
            if (revisedMapItemToNSWU.get(item) >= minUtility) {
               //get projected Db of current item
                Map<Integer, UtilityArrProjection> projectedDb = new HashMap<>();
                for(Integer sid : revisedUtilityArray.keySet()){
                    int firstPos=0;
                    UtilityArray[] utilityArray=revisedUtilityArray.get(sid);

                    for (int i = 1; i < utilityArray.length; i++) {
                        if (utilityArray[i].getItem().equals(item)){
                            firstPos=i;
                            break;
                        }
                    }
                    //contain current item
                    if(firstPos >0) {
                        List<UtilityArrPosition> positions = new ArrayList<>();

                        // find the max utility of this item in that sequence
                        // and the max remaining utility in that sequence
                        int maxUtility = Integer.MIN_VALUE;
                        int maxRemainingUtility = 0;
                        //----new add----
                        int peuSeq=Integer.MIN_VALUE;
                        //----new add end---
                        for(int i=firstPos; i != -1; i=utilityArray[i].getNext_pos()) {
                            // get the utility of the item in position i
                            int utility = utilityArray[i].getUtility();
                            int remaining = utilityArray[i].getRemainUtility();
                            positions.add(new UtilityArrPosition(i,utility));
                            //----------new add----
                            if (i==utilityArray.length-1){
                                peuSeq=Math.max(peuSeq,0);
                            }else{
                                peuSeq=Math.max(peuSeq,utility+remaining);
                            }
                            //----------new add  end----
                            if(utility > maxUtility) {
                                maxUtility = utility;
                               // int remaining = utilityArray[i].getRemainUtility();
                                    // If it is the first occurrence of this item
                                    // we remember the remaining utility as the max remaining utility
                                if(remaining > 0 && maxRemainingUtility == 0) {
                                    maxRemainingUtility = remaining;
                                }
                            }
                            
                        }

                        // update the total utility and total remaining utility for all sequences
                        // until now by adding the utility and remaining utility of the current
                        // sequence
                        totalUtility += maxUtility;
                        //----new add----
                        PEU+=peuSeq;
                        mapSidToPeu.put(sid,peuSeq);
                        //----new add end----
                        UtilityArrProjection utilityArrProjection = new UtilityArrProjection(utilityArray, positions);
                        projectedDb.put(sid,utilityArrProjection);
                    }
                }// for -- sid----
                    List<String> huseq=new ArrayList<>();//???????
                    huseq.add(String.valueOf(item));
                    if(totalUtility >= minUtility) {
                        huspCount++;
                        writeToFile(huseq,totalUtility);
                        //System.out.println(huseq+":"+totalUtility);
                    }

                //(NPEU:pruning strategy 3)
                    if(PEU >= minUtility) {
                        ProUM(huseq, projectedDb,mapSidToPeu);
                    }


            }
        }
        MemoryLogger.getInstance().checkMemory();
        writer.close();
        endTimestamp = System.currentTimeMillis();

}
        




    /**
     * ProUM algorithm
     *
     * @throws IOException
     */
    private void ProUM(List<String> prefix, Map<Integer, UtilityArrProjection> projectedDb,Map<Integer,Integer> mapSidToPeu) throws IOException {
        //System.out.println(prefix);

        Set<Integer> ilist = new TreeSet<>();
        Set<Integer> slist = new TreeSet<>();
        //store NPEU
        Map<Integer, Integer> iPMap = new HashMap<>();
        Map<Integer, Integer> sPMap = new HashMap<>();

        //store NRSU
        Map<Integer, Integer> iRMap = new HashMap<>();
        Map<Integer, Integer> sRMap = new HashMap<>();
        //-------new add------
        //store utility
        Map<Integer, Integer> iUMap = new HashMap<>();
        Map<Integer, Integer> sUMap = new HashMap<>();

        Map<Integer, Map<Integer, UtilityArrProjection>> mapIitemToProjectedDb = new HashMap<>();
        Map<Integer, Map<Integer, UtilityArrProjection>> mapSitemToProjectedDb = new HashMap<>();
        //I-contation
        if(projectedDb==null||projectedDb.isEmpty()){
            return;
        }
        candidateCount++;
        //item to map<sid,PEU>
        Map<Integer,Map<Integer,Integer>> mapiItemToSidToPeu=new HashMap<>();
        Map<Integer,Map<Integer,Integer>> mapsItemToSidToPeu=new HashMap<>();
        for (Integer sid : projectedDb.keySet()) {
            Set<Integer> tmp=new HashSet<>();
            UtilityArrProjection utilityArrProjection = projectedDb.get(sid);

            UtilityArray[] utilityArray = utilityArrProjection.getUtilityArray();
            List<UtilityArrPosition> positions = utilityArrProjection.getPositions();

            Map<Integer, Integer> mapMaxUtility = new HashMap<>();

            Map<Integer, Integer> mapPEU = new HashMap<>();
            for (int i = 0; i < positions.size(); i++) {
                UtilityArrPosition position = positions.get(i);
                int pos = position.getPosition();
                int utility = position.getUtility();
                int tid = utilityArray[pos].getEid();
                for (int j = pos + 1; j < utilityArray.length && utilityArray[j].getEid() == tid; j++) {
                    //System.out.println(utilityArray[j].getItem());
                    Integer item = utilityArray[j].getItem();
                    if (!ilist.contains(item)) {
                        ilist.add(item);
                    }
                    if (!tmp.contains(item)){
                        tmp.add(item);
                    }
                    if (!mapMaxUtility.containsKey(item)) {
                        Integer valueUtility=utilityArray[j].getUtility() + utility;
                        mapMaxUtility.put(item,valueUtility );
                    } else {
                        Integer oldUtility = mapMaxUtility.get(item);
                        Integer newUtility = utility + utilityArray[j].getUtility();
                        mapMaxUtility.put(item, Math.max(oldUtility, newUtility));
                    }
                    if (!mapPEU.containsKey(item)) {
                        Integer valueUtility=utilityArray[j].getUtility() + utility;
                        Integer valueRemainUtility=utilityArray[j].getRemainUtility();
                        mapPEU.put(item, valueUtility+valueRemainUtility);
                    } else {
                        Integer old = mapPEU.get(item);
                        Integer valueUtility=utilityArray[j].getUtility() + utility;
                        Integer valueRemainUtility=utilityArray[j].getRemainUtility();
                        mapPEU.put(item, Math.max(old,valueUtility+valueRemainUtility));
                    }

                    if (!mapIitemToProjectedDb.containsKey(item)) {
                        Map<Integer, UtilityArrProjection> newprojectionMap = new HashMap<>();
                        mapIitemToProjectedDb.put(item, newprojectionMap);
                        List<UtilityArrPosition> positionsTmp = new ArrayList<>();
                        positionsTmp.add(new UtilityArrPosition(j, utilityArray[j].getUtility() + utility));
                        UtilityArrProjection utilityArrProjection1 = new UtilityArrProjection(utilityArray, positionsTmp);
                        newprojectionMap.put(sid, utilityArrProjection1);
                    } else {
                        Map<Integer, UtilityArrProjection> newprojectionMap = mapIitemToProjectedDb.get(item);
                        if (newprojectionMap.containsKey(sid)) {
                            UtilityArrProjection newUtilityArrProjection = newprojectionMap.get(sid);
                            List<UtilityArrPosition> newPositions = newUtilityArrProjection.getPositions();
                            newPositions.add(new UtilityArrPosition(j, utilityArray[j].getUtility() + utility));
                        } else {
                            List<UtilityArrPosition> positionsTmp2 = new ArrayList<>();
                            positionsTmp2.add(new UtilityArrPosition(j, utilityArray[j].getUtility() + utility));
                            UtilityArrProjection utilityArrProjection2 = new UtilityArrProjection(utilityArray, positionsTmp2);
                            newprojectionMap.put(sid, utilityArrProjection2);
                        }
                    }
                }//----for extension item----

            }//----for position---

            for (Integer Iitem : mapMaxUtility.keySet()) {
                Integer U=mapMaxUtility.get(Iitem);
                if (!iUMap.containsKey(Iitem)){
                    iUMap.put(Iitem,U);
                }else{
                    Integer oldU = iUMap.get(Iitem);
                    iUMap.put(Iitem,U+oldU);
                }
                Integer PEU = mapPEU.get(Iitem);
                Map<Integer,Integer> mapSidToPeu2=mapiItemToSidToPeu.get(Iitem);
                if (mapSidToPeu2==null||mapSidToPeu2.isEmpty()){
                    mapSidToPeu2=new HashMap<>();
                    mapiItemToSidToPeu.put(Iitem,mapSidToPeu2);
                }
                mapSidToPeu2.put(sid,PEU);
                if (!iPMap.containsKey(Iitem)) {
                    iPMap.put(Iitem, PEU);
                } else {
                    Integer oldPEU = iPMap.get(Iitem);
                    iPMap.put(Iitem, oldPEU + PEU);
                }
            }
            for (Integer key : tmp) {
                if (iRMap.get(key) == null) {
                    iRMap.put(key, mapSidToPeu.get(sid));
                } else {
                    iRMap.put(key, iRMap.get(key) + mapSidToPeu.get(sid));
                }
            }
        }//----for sid------
        for (Integer Iitem:ilist) {
            Integer rsuValue = iRMap.get(Iitem);
            //(NRSU:pruning strategy 4)
            if (rsuValue >= minUtility){
            List<String> newPrefix=new ArrayList<>();
            for (int i = 0; i < prefix.size(); i++) {
                if (i!=prefix.size()-1){
                    newPrefix.add(prefix.get(i));
                }else {
                    String tmp=prefix.get(i);
                    tmp=tmp+"-"+Iitem;
                    newPrefix.add(tmp);
                }
            }
            Integer PEU=iPMap.get(Iitem);
            if(iUMap.get(Iitem)>minUtility){
                huspCount++;
                writeToFile(newPrefix,iUMap.get(Iitem));
                System.out.println(newPrefix+":"+iUMap.get(Iitem));
            }
        //  (NPEU:pruning strategy 3)
            if (PEU>=minUtility){

                ProUM(newPrefix,mapIitemToProjectedDb.get(Iitem),mapiItemToSidToPeu.get(Iitem));
            }
            }
        }

        //S-contation
        for (Integer sid : projectedDb.keySet()) {
            Set<Integer> tmp=new HashSet<>();
            UtilityArrProjection utilityArrProjection = projectedDb.get(sid);
            UtilityArray[] utilityArray = utilityArrProjection.getUtilityArray();
            List<UtilityArrPosition> positions = utilityArrProjection.getPositions();
            Map<Integer, Integer> mapMaxUtility = new HashMap<>();
            Map<Integer, Integer> map2PEU = new HashMap<>();
            for (int i = 0; i < positions.size(); i++) {
                UtilityArrPosition position = positions.get(i);
                int pos = position.getPosition();
                int utility = position.getUtility();
                int tid = utilityArray[pos].getEid();
                while (pos < utilityArray.length && utilityArray[pos].getEid() == tid) {
                    pos++;
                }

                for (int j = pos; j < utilityArray.length; j++) {
                    // System.out.println(utilityArray[j].getItem());
                    Integer item = utilityArray[j].getItem();
                    if (!slist.contains(item)) {
                        slist.add(item);
                    }
                    if (!tmp.contains(item)){
                        tmp.add(item);
                    }
                    if (!mapMaxUtility.containsKey(item)) {
                        Integer valUtility=utilityArray[j].getUtility() + utility;
                        mapMaxUtility.put(item, valUtility);
                    } else {
                        Integer oldUtility = mapMaxUtility.get(item);
                        Integer newUtility = utility + utilityArray[j].getUtility();
                        mapMaxUtility.put(item, Math.max(oldUtility, newUtility));
                    }
                    if (!map2PEU.containsKey(item)) {
                        Integer valUtility=utilityArray[j].getUtility() + utility;
                        Integer valRemainUtility=utilityArray[j].getRemainUtility();
                        map2PEU.put(item, valUtility+valRemainUtility);
                    } else {
                        Integer old = map2PEU.get(item);
                        Integer valUtility=utilityArray[j].getUtility() + utility;
                        Integer valRemainUtility=utilityArray[j].getRemainUtility();
                        map2PEU.put(item, Math.max(old,valUtility+valRemainUtility));
                    }
                    if (!mapSitemToProjectedDb.containsKey(item)) {
                        Map<Integer, UtilityArrProjection> newprojectionMap = new HashMap<>();
                        mapSitemToProjectedDb.put(item, newprojectionMap);
                        List<UtilityArrPosition> positionsTmp = new ArrayList<>();
                        positionsTmp.add(new UtilityArrPosition(j, utilityArray[j].getUtility() + utility));
                        UtilityArrProjection utilityArrProjection1 = new UtilityArrProjection(utilityArray, positionsTmp);
                        newprojectionMap.put(sid, utilityArrProjection1);
                    } else {
                        Map<Integer, UtilityArrProjection> newprojectionMap = mapSitemToProjectedDb.get(item);
                        if (newprojectionMap.containsKey(sid)) {
                            UtilityArrProjection newUtilityArrProjection = newprojectionMap.get(sid);
                            List<UtilityArrPosition> newPositions = newUtilityArrProjection.getPositions();
                            Boolean ismatch=false;
                            for (int k = 0; k < newPositions.size(); k++) {
                                if (newPositions.get(k).getPosition()==j){
                                    ismatch=true;
                                    Integer oldU=newPositions.get(k).getUtility();
                                    Integer newU=utilityArray[j].getUtility() + utility;
                                    newPositions.get(k).setUtility(Math.max(oldU,newU));
                                    break;
                                }
                            }
                            if (!ismatch){
                                newPositions.add(new UtilityArrPosition(j, utilityArray[j].getUtility() + utility));
                            }
                        } else {
                            List<UtilityArrPosition> positionsTmp2 = new ArrayList<>();
                            positionsTmp2.add(new UtilityArrPosition(j, utilityArray[j].getUtility() + utility));
                            UtilityArrProjection utilityArrProjection2 = new UtilityArrProjection(utilityArray, positionsTmp2);
                            newprojectionMap.put(sid, utilityArrProjection2);
                        }
                    }
                }//----for extension items----

            }//----for position-----
            for (Integer Sitem : mapMaxUtility.keySet()) {
                Integer U=mapMaxUtility.get(Sitem);
                if (!sUMap.containsKey(Sitem)){
                    sUMap.put(Sitem,U);
                }else{
                    Integer oldU = sUMap.get(Sitem);
                    sUMap.put(Sitem,U+oldU);
                }
                Integer PEU=map2PEU.get(Sitem);
                Map<Integer,Integer> mapSidToPeu3=mapsItemToSidToPeu.get(Sitem);
                if (mapSidToPeu3==null||mapSidToPeu3.isEmpty()){
                    mapSidToPeu3=new HashMap<>();
                    mapsItemToSidToPeu.put(Sitem,mapSidToPeu3);
                }
                mapSidToPeu3.put(sid,PEU);

                if (!sPMap.containsKey(Sitem)) {
                    sPMap.put(Sitem, PEU);
                } else {
                    Integer old = sPMap.get(Sitem);
                    sPMap.put(Sitem, old + PEU);
                }
            }
            for (Integer key : tmp) {
                if (sRMap.get(key) == null) {
                    sRMap.put(key, mapSidToPeu.get(sid));
                } else {
                    sRMap.put(key, sRMap.get(key) + mapSidToPeu.get(sid));
                }
            }
        }//----for sid----
        for (Integer Sitem:slist) {
            Integer rsuValue = sRMap.get(Sitem);
            // (NRSU:pruning strategy 4)
           if (rsuValue>=minUtility) {

            List<String> newPrefix = new ArrayList<>();
            for (int i = 0; i < prefix.size(); i++) {
                newPrefix.add(prefix.get(i));
            }
            newPrefix.add(String.valueOf(Sitem));
            Integer PEU = sPMap.get(Sitem);
            if (sUMap.get(Sitem) > minUtility) {
                huspCount++;
                writeToFile(newPrefix, sUMap.get(Sitem));
                // System.out.println(newPrefix + ":" + sUMap.get(Sitem));
            }
            // (NPEU:pruning strategy 3)
            if (PEU >= minUtility) {

                ProUM(newPrefix, mapSitemToProjectedDb.get(Sitem), mapsItemToSidToPeu.get(Sitem));
            }
      }
        }
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
