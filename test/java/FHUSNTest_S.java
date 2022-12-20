import java.io.IOException;

public class FHUSNTest_S {
    public static void main(String[] args) throws IOException {
        //String input = "src/main/resources/Neg_BIBLE.txt";
        //String input = "src/main/resources/Neg_FIFA.txt";
        //String input = "src/main/resources/Neg_sign.txt";
        //String input = "src/main/resources/dataFormat.txt";//0.05;
       // String input = "src/main/resources/Neg_FIFA_10k.txt";//0.05;
        //String input = "src/main/resources/Neg_kosarak10k.txt";
        String input = "src/main/resources/DataBase_USpan_N.txt";
        //String input = "src/main/resources/DataBase_USpan.txt";
       // String input = "src/main/resources/DataBase_USpan_N.txt";
        String output = "src/main/resources/output.txt";

        double minUtilityRatio = 0.0188;

        FHUSN_S algo = new FHUSN_S();

        System.out.println("test dataset: " + input);
        System.out.println("minUtilityRatio: " + String.format("%.5f", minUtilityRatio));

        algo.runAlgorithm(input, output, minUtilityRatio);

        algo.printStats();
    }
}
