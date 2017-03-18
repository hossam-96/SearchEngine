package Main_Package;

import java.util.Hashtable;

/**
 * Created by moham on 3/11/2017.
 */
public class Contractions
{
    public Hashtable<String,String> Contractions_Map;

    public Contractions()
    {
        Contractions_Map = new Hashtable<String,String>();
        Contractions_Map.put("ain't", "am not");
        Contractions_Map.put( "aren't", "are not");
        Contractions_Map.put( "can't", "cannot");
        Contractions_Map.put( "can't've", "cannot have");
        Contractions_Map.put( "'cause", "because");
        Contractions_Map.put( "could've", "could have");
        Contractions_Map.put( "couldn't", "could not");
        Contractions_Map.put( "couldn't've", "could not have");
        Contractions_Map.put( "didn't", "did not");
        Contractions_Map.put( "doesn't", "does not");
        Contractions_Map.put( "don't", "do not");
        Contractions_Map.put( "hadn't", "had not");
        Contractions_Map.put( "hadn't've", "had not have");
        Contractions_Map.put( "hasn't", "has not");
        Contractions_Map.put( "haven't", "have not");
        Contractions_Map.put( "he'd", "he had");
        Contractions_Map.put( "he'd've", "he would have");
        Contractions_Map.put( "he'll", "he will");
        Contractions_Map.put( "he'll've", "he will have");
        Contractions_Map.put( "he's", "he is");
        Contractions_Map.put( "how'd", "how did");
        Contractions_Map.put( "how'd'y", "how do you");
        Contractions_Map.put( "how'll", "how will");
        Contractions_Map.put( "how's", "how is");
        Contractions_Map.put( "i'd", "i would");
        Contractions_Map.put( "i'd've", "i would have");
        Contractions_Map.put( "i'll", "i will");
        Contractions_Map.put( "i'll've", "i will have");
        Contractions_Map.put( "i'm", "i am");
        Contractions_Map.put( "i've", "i have");
        Contractions_Map.put( "isn't", "is not");
        Contractions_Map.put( "it'd", "it would");
        Contractions_Map.put( "it'd've", "it would have");
        Contractions_Map.put( "it'll", "it will");
        Contractions_Map.put( "it'll've", "it will have");
        Contractions_Map.put( "it's", "it is");
        Contractions_Map.put( "let's", "let us");
        Contractions_Map.put( "ma'am", "madam");
        Contractions_Map.put( "mayn't", "may not");
        Contractions_Map.put( "might've", "might have");
        Contractions_Map.put( "mightn't", "might not");
        Contractions_Map.put( "mightn't've", "might not have");
        Contractions_Map.put( "must've", "must have");
        Contractions_Map.put( "mustn't", "must not");
        Contractions_Map.put( "mustn't've", "must not have");
        Contractions_Map.put( "needn't", "need not");
        Contractions_Map.put( "needn't've", "need not have");
        Contractions_Map.put( "o'clock", "of the clock");
        Contractions_Map.put( "oughtn't", "ought not");
        Contractions_Map.put( "oughtn't've", "ought not have");
        Contractions_Map.put( "shan't", "shall not");
        Contractions_Map.put( "sha'n't", "shall not");
        Contractions_Map.put( "shan't've", "shall not have");
        Contractions_Map.put( "she'd", "she would");
        Contractions_Map.put( "she'd've", "she would have");
        Contractions_Map.put( "she'll", "she will");
        Contractions_Map.put( "she'll've", "she will have");
        Contractions_Map.put( "she's", "she is");
        Contractions_Map.put( "should've", "should have");
        Contractions_Map.put( "shouldn't", "should not");
        Contractions_Map.put( "shouldn't've", "should not have");
        Contractions_Map.put( "so've", "so have");
        Contractions_Map.put( "so's", "so is");
        Contractions_Map.put( "that'd", "that would");
        Contractions_Map.put( "that'd've", "that would have");
        Contractions_Map.put( "that's", "that is");
        Contractions_Map.put( "there'd", "there would");
        Contractions_Map.put( "there'd've", "there would have");
        Contractions_Map.put( "there's", "there is");
        Contractions_Map.put( "they'd", "they would");
        Contractions_Map.put( "they'd've", "they would have");
        Contractions_Map.put( "they'll", "they will");
        Contractions_Map.put( "they'll've", "they will have");
        Contractions_Map.put( "they're", "they are");
        Contractions_Map.put( "they've", "they have");
        Contractions_Map.put( "to've", "to have");
        Contractions_Map.put( "wasn't", "was not");
        Contractions_Map.put( "we'd", "we would");
        Contractions_Map.put( "we'd've", "we would have");
        Contractions_Map.put( "we'll", "we will");
        Contractions_Map.put( "we'll've", "we will have");
        Contractions_Map.put( "we're", "we are");
        Contractions_Map.put( "we've", "we have");
        Contractions_Map.put( "weren't", "were not");
        Contractions_Map.put( "what'll", "what will");
        Contractions_Map.put( "what'll've", "what will have");
        Contractions_Map.put( "what're", "what are");
        Contractions_Map.put( "what's", "what is");
        Contractions_Map.put( "what've", "what have");
        Contractions_Map.put( "when's", "when is");
        Contractions_Map.put( "when've", "when have");
        Contractions_Map.put( "where'd", "where did");
        Contractions_Map.put( "where's", "where is");
        Contractions_Map.put( "where've", "where have");
        Contractions_Map.put( "who'll", "who will");
        Contractions_Map.put( "who'll've", "who will have");
        Contractions_Map.put( "who's", "who is");
        Contractions_Map.put( "who've", "who have");
        Contractions_Map.put( "why's", "why is");
        Contractions_Map.put( "why've", "why have");
        Contractions_Map.put( "will've", "will have");
        Contractions_Map.put( "won't", "will not");
        Contractions_Map.put( "won't've", "will not have");
        Contractions_Map.put( "would've", "would have");
        Contractions_Map.put( "wouldn't", "would not");
        Contractions_Map.put( "wouldn't've", "would not have");
        Contractions_Map.put( "y'all", "you all");
        Contractions_Map.put( "y'all'd", "you all would");
        Contractions_Map.put( "y'all'd've", "you all would have");
        Contractions_Map.put( "y'all're", "you all are");
        Contractions_Map.put( "y'all've", "you all have");
        Contractions_Map.put( "you'd", "you would");
        Contractions_Map.put( "you'd've", "you would have");
        Contractions_Map.put( "you'll", "you will");
        Contractions_Map.put( "you'll've", "you will have");
        Contractions_Map.put( "you're", "you are");
        Contractions_Map.put( "you've", "you have" );
    }

    public String Convert_To_FullFrom(String Term)
    {
        if(Contractions_Map.containsKey(Term.trim()))
            Term=Contractions_Map.get(Term.trim());
        return Term;
    }
}
