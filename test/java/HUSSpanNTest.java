import java.io.IOException;

public class HUSSpanNTest {
    public static void main(String[] args) throws IOException {
        //String input = "src/main/resources/Neg_BMS_27k.txt";
        //String input = "src/main/resources/Neg_BIBLE_36k.txt";
       //String input = "src/main/resources/Neg_sign.txt";
       //String input = "src/main/resources/dataFormat.txt";//0.05;
        //String input = "src/main/resources/Neg_FIFA_10k.txt";//0.05;
        //String input = "src/main/resources/Neg_kosarak10k.txt";
        String input = "src/main/resources/DataBase_USpan_N.txt";
       //String input = "src/main/resources/DataBase_USpan.txt";
       // String input = "src/main/resources/husp_test_negative.txt";

        String output = "src/main/resources/output.txt";
        double minUtilityRatio = 0.0188;

        HUSPanN algo = new HUSPanN();

        System.out.println("test dataset: " + input);
        System.out.println("minUtilityRatio: " + String.format("%.5f", minUtilityRatio));

        algo.runAlgorithm(input, output, minUtilityRatio);

        algo.printStats();
    }
}
