package Main_Package;

import java.io.IOException;
import java.util.*;
import org.apache.mahout.math.SparseMatrix;


public class NewldaAlgo {
    //for Debuge
    private long LDA_Done;
    private long All_Word;
    private long LDA_all;
    private long LDA_Done_Print;
    //==========================================================
    private int topNum; //no. of my clustering centroids
    private List<ArrayList<String>> docMat = new ArrayList<ArrayList<String>>(); //container of my docs
    private List<ArrayList<Integer>> currS = new ArrayList<ArrayList<Integer>>(); //current state(assignment) of this word in that doc
    private Map<Integer, Float> NWT = new HashMap<Integer, Float>(); //n of words in topic k


    private Hashtable<String , ArrayList<Float>> wordTopicProb; //to map words to their topic probability

    //==============================================================indexed words
    private Map<String, Integer> wIdx = new HashMap<String, Integer>(); //mapping an index for each word
    private Map<Integer, String> wIdxInv = new HashMap<Integer, String>();
    private int allWords = 0; //my vocab world
    //============================================================================

    //====================Optimize the magnitude operation in distance calculation
    private float[] wordTopicMatrix_clacColumn_topicIndex;
    private float[] docTopicMatrix_clacColumn_docIndex;
    private ArrayList<Float> magTopDoc;
    //============================================================================

    //Save our Ns
    private SparseMatrix wordTopMat; //number of word i in this topic k
    private SparseMatrix topDocMat; //no of words in this doc j assigned to topic k

    //======================================================HyperParams (as bias)
    private float alpha ;
    private float beta ;
    //============================================================================
    private MongoDB DB;

    public NewldaAlgo() throws Exception{
        DB=new MongoDB();
    }
    public void InitTrain(int topNum) throws Exception {

        List<String> docs=DB.DoucmentToWordsGetWordsAfterCleaning();
        this.All_Word=DB.UniqueWord;
        this.topNum = topNum;

        this.magTopDoc=new ArrayList<Float>();
        wordTopicMatrix_clacColumn_topicIndex=new float[topNum];
        docTopicMatrix_clacColumn_docIndex=new float[docs.size()];

        for (String d : docs) {

            ArrayList<Integer> s = new ArrayList<Integer>();

            ArrayList<String> doc = new ArrayList<String>();

            String vocabs[] = d.split(" "); //split vocabs
            //System.out.println(d);

            for (String w : vocabs) {

                doc.add(w); s.add(-1); //initialize states with -1

                if (!this.wIdx.containsKey(w)) {

                    this.wIdx.put(w, this.allWords);
                    this.wIdxInv.put(this.allWords, w);
                    this.allWords++;
                }
            }

            this.docMat.add(doc);  this.currS.add(s);
        }
        //Edit this
        int DMdims[] = {topNum, this.docMat.size()};
        this.topDocMat = new SparseMatrix(topNum, this.docMat.size());
        int WDdims[] = {this.allWords, topNum};
        this.wordTopMat = new SparseMatrix(this.allWords, topNum);

        //init NWT
        for (int i = 0; i < topNum; i++)
            this.NWT.put(i, 0.0f);


        alpha = 0.01f; //common initialization
        beta = 0.01f;
    }
    public void clear() {
        for (int r = 0; r < this.topNum; r++) {
            for (int c = 0; c < this.docMat.size(); c++) {
                this.topDocMat.set(r, c, 0.0);
            }
        }
        for (int r = 0; r < this.allWords; r++) {
            for (int c = 0; c < this.topNum; c++) {
                this.wordTopMat.set(r, c, 0.0);
            }
        }
    }
    public void debugPrint(){

        System.out.println("current state: " + currS);

        System.out.println("wordTopicProb: " + wordTopicProb);
        System.out.println("NWT: " + NWT);
        System.out.println("wIdx: " + wIdx);
        System.out.println("wIdxInv: " + wIdxInv);

        System.out.println("topDocMat: ");
        for (int i = 0; i < topNum; i++) {
            for (int j = 0; j <docMat.size() ; j++) {
                System.out.print(topDocMat.get(i,j) + " ");
            }
            System.out.println();
        }

        System.out.println("wordTopMat: ");
        for (int i = 0; i < allWords; i++) {
            for (int j = 0; j <topNum ; j++) {
                System.out.print(wordTopMat.get(i,j) + " ");
            }
            System.out.println();
        }
    }
    private void clusterAndLearn(int docIdx) {

        //printMatrix();
        //System.out.println("doc idx=" + docIdx);
        Random rand = new Random();
        ArrayList<String> doc = this.docMat.get(docIdx);
        ArrayList<Integer> s = this.currS.get(docIdx);

        int idx = 0;
        for (String w : doc) {
            //The assumptiion here is that all words' states but the current are true
            int zij = s.get(idx);    //current assignment for this word (topic of word i in doc j)
//            System.out.print("current state of word " + word + " --> zij=" + zij + " - ");
//            System.out.println("word=" + word);
            int wordIndex = this.wIdx.get(w);
//            System.out.print(word + "'s idx = " + wordIndex + " - ");
//            System.out.println();
            float u = rand.nextFloat();
            float[] Prob = new float[this.topNum + 1];  Prob[0] = 0.0f;

            for (int topicIdx = 1; topicIdx <= this.topNum; topicIdx++) {

                float NKj = (float)this.topDocMat.get(topicIdx - 1, docIdx); //no of words in this doc j assigned to topic k
                float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx - 1); //no times word i assigned to this topic
                float NWT = this.NWT.get(topicIdx - 1); //total no of words in this topic

                if (zij == (topicIdx - 1)) {
                    //if my current topic(zij) = specific topic so decrement 1 from its frequencies
                    //because it`s assumed to be wrong,, then do the calculations and re-assign your values
                    NKj -= 1.0; Nwk -= 1.0; NWT -= 1.0;
                }
                Prob[topicIdx] = Prob[topicIdx - 1] + (NKj + this.alpha) * (Nwk + this.beta) / (NWT + this.allWords * this.beta);
                //Normalization constant
            }
            for (int topicIdx = 0; topicIdx <= this.topNum; topicIdx++) {
//                System.out.print(Prob[topicIdx] + " ");
            }
//            System.out.println();

            //===========Updating and Re-assigning
            for (int topicIdx = 1; topicIdx <= this.topNum; topicIdx++) {
                if ( u <= Prob[topicIdx]/Prob[this.topNum]) {

                    s.set(idx, topicIdx - 1); //you picked a specific topic to be the true one topic

                    //so update NKj, Nwk, and NWT
                    //first get this topic frequencies.
                    float NKjn = (float)this.topDocMat.get(topicIdx - 1, docIdx);
                    float Nwkn = (float)this.wordTopMat.get(wordIndex, topicIdx - 1);
                    float NWTn = this.NWT.get(topicIdx - 1);

                    if (zij != (topicIdx - 1)) {

                        //Then increment its frequencies with 1 if it wasn`t the initial one from the beginning
                        this.topDocMat.set(topicIdx - 1, docIdx, (NKjn + 1.0));
                        this.wordTopMat.set(wordIndex, topicIdx - 1, (Nwkn + 1.0));
                        this.NWT.put(topicIdx - 1, (NWTn + 1.0f));

                        //Now decrement the old topic frequencies by 1
                        if (zij >= 0) { //not the -1 initialized

                            float NKjold = (float)this.topDocMat.get(zij, docIdx);
                            float Nwkold = (float)this.wordTopMat.get(wordIndex, zij);
                            float NWTold = this.NWT.get(zij);
                            this.topDocMat.set(zij, docIdx, (NKjold - 1.0));
                            this.wordTopMat.set(wordIndex, zij, (Nwkold - 1.0));
                            this.NWT.put(zij, (NWTold - 1.0f));
                        }
                    }
                    break;    //finished update and new assignment for this word
                }
            }
            LDA_Done+=this.topNum;
            if(LDA_Done/10000>LDA_Done_Print) {
                System.out.print("\r" + Math.floor((LDA_Done * 100.0) / LDA_all) + "%");
                LDA_Done_Print++;
            }
            idx++;
        }

        this.currS.set(docIdx, s);
    }
    public void optimize() {
        long All=(long)this.docTopicMatrix_clacColumn_docIndex.length+(long)this.topNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);
        long f=All/100;


        for(int i=0;i<this.topNum;i++) {
            wordTopicMatrix_clacColumn_topicIndex[i] = (float) this.wordTopMat.viewColumn(i).zSum();
            if(Done%f==0)
                System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
            Done++;
        }
        for(int i=0;i<this.docTopicMatrix_clacColumn_docIndex.length;i++)//docs
        {
            docTopicMatrix_clacColumn_docIndex[i] = (float) this.topDocMat.viewColumn(i).zSum();
            if(Done%f==0)
                System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
            Done++;
        }
        System.out.println();
    }
    public float getTopicWordProbability(String word, int topicIdx) {
        int wordIndex = this.wIdx.get(word);
        float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx); //no times word i assigned to this topic
        float NWT = this.wordTopicMatrix_clacColumn_topicIndex[topicIdx]; //total no of words in this topic

        //probability(distro) that this word is in this topic
        return (Nwk + this.beta) / (NWT + this.allWords * this.beta);
    }
    public void buildWordTopicsProb(){

        ArrayList<Float> tmp = new ArrayList<Float>();
        this.wordTopicProb = new Hashtable<String, ArrayList<Float>>();

        long All=(long)this.allWords*(long)this.topNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);
        long f=All/100;


        for(int i=0;i<this.allWords;i++){
            //get this word
            String w = wIdxInv.get(i);
    //            System.out.print(w + " | ");

            //compute its probability in each topic
            for (int j = 0; j < this.topNum; j++) {
                float tprob = getTopicWordProbability(w, j);
    //                System.out.print("Topic" + j + ": " + tprob + " | ");
                tmp.add(j, tprob);

                if(Done%f==0)
                    System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
                Done++;


            }
            this.wordTopicProb.put(w,new ArrayList<Float>(tmp));
            tmp.clear();

    //            System.out.println();
        }
        System.out.println();
    //        System.out.println(wordTopicProb);
    }
    public void Train_LDA(long epochs,int topics_num) throws Exception{
        InitTrain(topics_num);
        LDA_all=this.All_Word;LDA_all*=this.topNum;
        System.out.println("Stating LDA Training");
        System.out.println("Max Num OF Iteration #"+LDA_all*epochs);
        //Train
        clear();
        for (int i = 1; i <= epochs; i++) {
            System.out.println("Stating LDA Iteration "+i);
            LDA_Done=0;
            LDA_Done_Print=0;
            for (int j = 0; j < this.docMat.size(); j++) {
                clusterAndLearn(j);
            }
            System.out.println();
        }
        System.out.println("Starting Pre");
        optimize();
        System.out.println("LDA buildWordTopicsProb Start");
        buildWordTopicsProb();
        DB.LdaWordTopicsInsert(this.wordTopicProb);
    }
    public static void main(String[] args)throws Exception
    {
        NewldaAlgo Model=new NewldaAlgo();
        Model.Train_LDA(4,100);
    }
}