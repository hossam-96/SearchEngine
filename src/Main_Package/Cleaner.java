package Main_Package;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by moham on 3/9/2017.
 */
public class Cleaner {
    private Const Con = new Const();
    private Stemmer stemmer;
    private Contractions Contractions_Text;
    private String StopWords;

    public Cleaner() {
        stemmer = new Stemmer();
        Contractions_Text = new Contractions();
        StopWords = "";
        try (BufferedReader br = new BufferedReader(new FileReader(this.Con.Root_Path + "\\stoplists\\long.txt"))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                if (StopWords.isEmpty())
                    StopWords += sCurrentLine;
                else StopWords += "|" + sCurrentLine;
            }

        } catch (Exception e) {
            e.getMessage();
        }
        StopWords = "\\b(" + StopWords + ".....)\\b\\s?";
    }

    public String Replace_contractions_with_full_form(String Text) {
        String Terms[] = Text.trim().split(" ");
        for (int i = 0; i < Terms.length; i++) {
            Terms[i] = Contractions_Text.Convert_To_FullFrom(Terms[i].toLowerCase());
        }
        return String.join(" ", Terms);
    }

    public String Convert_To_Lower(String Text) {
        return Text.toLowerCase();
    }

    public String Remove_(String Text) {
        return Text.replaceAll("-+", " ");
    }

    public String Remove_punctuation(String Text) {
        return Text.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
    }

    public String Remove_StopWords(String Text) {
        return Text.replaceAll(StopWords, "Stopword ");
    }

    public String[] Remove_Non_Ascii(String Text) {
        Text = Text.replaceAll("[^\\p{ASCII}]", " ");
        Text = Text.trim().replaceAll(" +", " ");
        String Terms[] = Text.split(" ");
        for (int i = 0; i < Terms.length; i++)
            Terms[i] = Terms[i].trim();
        return Terms;
    }

    public String[] Remove_Non_English(String Text) {
        Text = Text.replaceAll("[^A-Za-z\\s]", " ");
        Text = Text.trim().replaceAll(" +", " ");
        String Terms[] = Text.split(" ");
        for (int i = 0; i < Terms.length; i++)
            Terms[i] = Terms[i].trim();
        return Terms;
    }

    public String[] Stem_Words(String[] Terms) {
        for (int i = 0; i < Terms.length; i++) {
            this.stemmer.add(Terms[i].toCharArray(), Terms[i].length());
            this.stemmer.stem();
            Terms[i] = this.stemmer.toString();
        }
        return Terms;
    }

    public String[] Clean_Text(String Text) {
        //System.out.println(Text);
        Text = this.Convert_To_Lower(Text);
        Text=this.Remove_punctuation(Text);
        //System.out.println(Text);
        Text = this.Replace_contractions_with_full_form(Text);
        //System.out.println(Text);
        Text = this.Remove_StopWords(Text);
        //System.out.println(Text);
        String Terms[] = this.Remove_Non_English(Text);
        Terms = this.Stem_Words(Terms);
        return Terms;
    }

    public String[] Clean_Text_V2(String Text) {
        Text=this.Remove_punctuation(Text);
        Text = this.Replace_contractions_with_full_form(Text);
        String Terms[] = this.Remove_Non_English(Text);
        return Terms;
    }

    public String[] Clean_Text_lossless(String Text) {
        Text = this.Convert_To_Lower(Text);
        Text = this.Remove_punctuation(Text);
        Text = this.Replace_contractions_with_full_form(Text);
        String Terms[] = this.Remove_Non_Ascii(Text);
        return Terms;
    }

    public String[][] Triple_Clean(String Text) {
        String[][] Ret = new String[3][];

        Ret[0] = Clean_Text_lossless(Text);
        Ret[1] = Clean_Text(Text);
        Ret[2] = Clean_Text_V2(Text);
        return Ret;
    }
}
