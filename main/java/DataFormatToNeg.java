/**
 * This file can generate sequence with negative item
 * based on real datasets
 * **/
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataFormatToNeg {
    public static void main(String[] args) throws IOException {
        Map<Integer,Integer> mapItemToCountseq=new TreeMap<>();
        List<List<Map<Integer,Integer>>> database=new ArrayList<>();
        List<List<Map<Integer,Integer>>> revisedatabase=new ArrayList<>();
        Map<Integer,Integer> mapSidToNSU=new HashMap<>();
        Map<Integer,Integer> mapSidToSU=new HashMap<>();
        //String input = "src/main/resources/SIGN_sequence_utility.txt";
        String input = "src/main/resources/husp_BIBLE.txt";
        String output = "src/main/resources/dataFormat.txt";
        //String input = "src/main/resources/husp_FIFA.txt";
        //String input = "src/main/resources/husp_kosarak10k.txt";
        //String input = "src/main/resources/SIGN_sequence_utility.txt";
        int sid=0;
        BufferedReader myInput = null;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
        String thisLine = null;
        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
            while ((thisLine = myInput.readLine()) != null) {
                int checkSU=0;
                List<Map<Integer,Integer>> sequence=new ArrayList<>();
                sid++;
                String[] spilts = thisLine.split(" -2 ");
                String SU = spilts[1];
                int SUtility = Integer.parseInt(SU.substring(SU.indexOf(":") + 1));
                mapSidToSU.put(sid,SUtility);
                spilts[0] = spilts[0].substring(0, spilts[0].lastIndexOf(" -1")).trim();
                String[] itemsetString = spilts[0].split(" -1 ");

                for (int i = 0; i < itemsetString.length; i++) {
                    Map<Integer,Integer> itemset=new TreeMap<>();
                    String[] itemAndUtility = itemsetString[i].trim().split(" ");
                    for (String val : itemAndUtility) {
                        Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
                        Integer utility = Integer.parseInt(val.trim().substring(val.trim().indexOf("[") + 1, val.trim().indexOf("]")));
                        checkSU+=utility;
                        itemset.put(item,utility);
                        Integer count=mapItemToCountseq.get(item);
                        if (null==count){
                            mapItemToCountseq.put(item,1);
                        }else {
                            mapItemToCountseq.put(item,count+1);
                        }
                    }
                    sequence.add(itemset);
                }
                database.add(sequence);
                if (checkSU!=SUtility){
                    System.out.println("error");
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
        List<Integer> keyList=mapItemToCountseq.keySet().stream().collect(Collectors.toList());
        int numberSidofNeg=0;
//生成随机数
        int numberOfItem= (int) Math.ceil(mapItemToCountseq.size()*0.06);
        Set<Integer> negItemIndex=new TreeSet<>();
        Set<Integer> negItem=new TreeSet<>();
        for (int i = 0; i < numberOfItem; i++) {
            int randomIndex = (int)(Math.random()*mapItemToCountseq.size());
            while (negItemIndex.contains(randomIndex)){
                randomIndex = (int)(Math.random()*mapItemToCountseq.size());
            }
            negItemIndex.add(randomIndex);
            negItem.add(keyList.get(randomIndex));
            System.out.println(keyList.get(randomIndex));
        }
      //遍历database
        for (int i = 0; i < database.size(); i++) {
            List<Map<Integer,Integer>> seq=database.get(i);
            Integer SU=mapSidToSU.get(i+1);
            Integer NSU=mapSidToSU.get(i+1);
            List<Map<Integer,Integer>> seqNeg=new ArrayList<>();
                for (Map<Integer,Integer> itemset:seq){
                    Map<Integer,Integer> itemsetNeg=new TreeMap<>();
                    for (Integer item:itemset.keySet()) {
                        if (negItem.contains(item)){
                            itemsetNeg.put(item,-itemset.get(item));
                            NSU-=itemset.get(item);
                            SU-=2*itemset.get(item);
                        }else {
                            itemsetNeg.put(item,itemset.get(item));
                        }
                    }
                seqNeg.add(itemsetNeg);
                }
                if (NSU<0||SU<0){
                    System.out.println("need check sid= "+(i+1));
                }
                if (NSU!=SU){
                    numberSidofNeg++;
                    System.out.println("NSU!=SU sid="+(i+1));
                }
            mapSidToNSU.put(i+1,NSU);
            mapSidToSU.put(i+1,SU);
            revisedatabase.add(seqNeg);
        }

        for (int i = 0; i < revisedatabase.size(); i++) {
            StringBuilder buffer = new StringBuilder();
            Integer SU=mapSidToSU.get(i+1);
            Integer NSU=mapSidToNSU.get(i+1);
            List<Map<Integer,Integer>> seqNeg=revisedatabase.get(i);
            for (int l = 0; l < seqNeg.size(); l++) {
                List<Integer> keys=seqNeg.get(l).keySet().stream().collect(Collectors.toList());
                for (int j = 0; j < keys.size(); j++) {
                    buffer.append(keys.get(j));
                    buffer.append("[");
                    buffer.append(seqNeg.get(l).get(keys.get(j)));
                    buffer.append("]");
                    //System.out.print(keys.get(j)+"["+seqNeg.get(l).get(keys.get(j))+"]");
                    if (j==keys.size()){

                    }else {
                        buffer.append(" ");
                       // System.out.print(" ");
                    }
                }
                buffer.append("-1 ");
                //System.out.print("-1 ");
                if (l==seqNeg.size()-1){
                    buffer.append("-2 ");
                    //System.out.print("-2 ");
                }

            }
            buffer.append("NSUtility:");
            buffer.append(NSU);
            buffer.append(" SUtility:");
            buffer.append(SU);
            writer.write(buffer.toString());
            writer.newLine();
            writer.flush();
           // System.out.print("NSUtility:"+NSU+" SUtility:"+SU);
          //  System.out.println();
        }

        System.out.println("sequence number contains negtive utility: "+numberSidofNeg);
        double port=(double) numberSidofNeg/sid;
        System.out.println("sequence portion of negtive : "+port);
    }

}
