package com.nlp.lda;

import java.io.IOException;
import java.util.*;

import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.Vector;


public class ldaAlgo {
    private float alpha = 0.01f;
    private float beta = 0.01f;
    private int topNum;
    private List<ArrayList<String>> docMat = new ArrayList<ArrayList<String>>();
    private List<ArrayList<Integer>> currS = new ArrayList<ArrayList<Integer>>(); //current state(assignment) of word in doc
    private SparseMatrix wordTopMat; //number of words in each topic
    private SparseMatrix docTopMat;

    private Hashtable<String, ArrayList<Float>> wordTopicProb; //to map words to their probability
    private Hashtable<Integer, ArrayList<Float>> topicDocProb; //to map topics to their probability
    private Hashtable<String, ArrayList<Float>> invertedIndexDist; //Inverted idx-WordDoc


    private Map<Integer, Float> NWT = new HashMap<Integer, Float>(); //n of words in topic k
    private Map<String, Integer> wIdx = new HashMap<String, Integer>();
    private Map<Integer, String> wIdxInv = new HashMap<Integer, String>();
    private int allWords = 0;


    public ldaAlgo(int topNum, List<String> documents) throws IOException {
        this.topNum = topNum;
//        this.tweetUtil = new LDAUtilities(true, true);
        //build document matrix: assuming we receive a clean version of tweets
        for (String doc : documents) {
            ArrayList<String> document = new ArrayList<String>();
            ArrayList<Integer> state = new ArrayList<Integer>();
            //System.out.println(doc);
//            String docWoSW = this.tweetUtil.cleanDoc(doc);    //remove stop words
            String tokens[] = doc.split(" "); //keep this or remove it?!
            System.out.println(doc);
            for (String token : tokens) {
                document.add(token);
                state.add(-1);
                if (!this.wIdx.containsKey(token)) {
                    this.wIdx.put(token, this.allWords);
                    this.wIdxInv.put(this.allWords, token);
                    this.allWords++;
                }
            }
            this.docMat.add(document);
            this.currS.add(state);
        }
        int DMdims[] = {topNum, this.docMat.size()};
        this.docTopMat = new SparseMatrix(DMdims);
        int WDdims[] = {this.allWords, topNum};
        this.wordTopMat = new SparseMatrix(WDdims);

        //init NWT
        for (int idx = 0; idx < topNum; idx++) {
            this.NWT.put(idx, 0.0f);
        }
    }



    //remove?!!
    public void reset() {
        for (int row = 0; row < this.topNum; row++) {
            for (int col = 0; col < this.docMat.size(); col++) {
                this.docTopMat.set(row, col, 0.0);
            }
        }
        for (int row = 0; row < this.allWords; row++) {
            for (int col = 0; col < this.topNum; col++) {
                this.wordTopMat.set(row, col, 0.0);
            }
        }
    }

    //Gibbs Sampler
    private void GibbsSampler(int docIdx) {
        //printMatrix();
        //System.out.println("doc idx=" + docIdx);
        Random rand = new Random();
        ArrayList<String> doc = this.docMat.get(docIdx);
        ArrayList<Integer> state = this.currS.get(docIdx);
        int idx = 0;
        for (String word : doc) {
            int zij = state.get(idx);    //current assignment for this word
//            System.out.print("current state of word " + word + " --> zij=" + zij + " - "); //comm
//            System.out.println("word=" + word); //comm
            int wordIndex = this.wIdx.get(word);
//            System.out.print(word + "'s idx = " + wordIndex + " - "); //comm
//            System.out.println(); //del
            float u = rand.nextFloat();
            float[] Prob = new float[this.topNum + 1];
            Prob[0] = 0.0f;
            for (int topicIdx = 1; topicIdx <= this.topNum; topicIdx++) {

                float NKj = (float)this.docTopMat.get(topicIdx - 1, docIdx);
                float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx - 1);
                float NWT = this.NWT.get(topicIdx - 1);
                if (zij == (topicIdx - 1)) {
                    //we need adjustments cause it`s assignmented to diff topic
                    NKj -= 1.0;
                    Nwk -= 1.0;
                    NWT -= 1.0;
                }
                Prob[topicIdx] = Prob[topicIdx - 1] + (NKj + this.alpha) * (Nwk + this.beta) / (NWT + this.allWords * this.beta);
            }

            //================Updating and Re-assigning
            for (int topicIdx = 1; topicIdx <= this.topNum; topicIdx++) {
                if (u <= Prob[topicIdx] / Prob[this.topNum]) {
                    state.set(idx, topicIdx - 1);
                    //update NKj, Nwk, and NWT
                    float newNKj = (float)this.docTopMat.get(topicIdx - 1, docIdx);
                    float newNwk = (float)this.wordTopMat.get(wordIndex, topicIdx - 1);
                    float newNWT = this.NWT.get(topicIdx - 1);
                    if (zij != (topicIdx - 1)) {
                        this.docTopMat.set(topicIdx - 1, docIdx, (newNKj + 1.0));
                        this.wordTopMat.set(wordIndex, topicIdx - 1, (newNwk + 1.0));
                        this.NWT.put(topicIdx - 1, (newNWT + 1.0f));
                        if (zij >= 0) {
                            float oldNKj = (float)this.docTopMat.get(zij, docIdx);
                            float oldNwk = (float)this.wordTopMat.get(wordIndex, zij);
                            float oldNWT = this.NWT.get(zij);
                            this.docTopMat.set(zij, docIdx, (oldNKj - 1.0));
                            this.wordTopMat.set(wordIndex, zij, (oldNwk - 1.0));
                            this.NWT.put(zij, (oldNWT - 1.0f));
                        }
                    }
                    break;    //finished update and new assignment
                }
            }
            idx++;
        }

        this.currS.set(docIdx, state);
    }


    //P(k|w)
    public float getTopicWordProbability(String word, int topicIdx) {

        int wordIndex = this.wIdx.get(word);
        float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx);
        Vector vec = this.wordTopMat.getColumn(topicIdx);
        float NWT = (float)vec.zSum();

        return (Nwk + this.beta) / (NWT + this.allWords * this.beta);
    }

    //P(k|d)
    public float getTopicDocProbability(int docIdx, int topicIdx) {
        float NKj = (float)this.docTopMat.get(topicIdx, docIdx);
        Vector vec = this.docTopMat.getColumn(docIdx);
        float Nj = (float)vec.zSum();

//        System.out.println("NWTj=" + NWTj);
//        System.out.println("Nj=" + Nj);

        return (NKj + this.alpha) / (Nj + this.topNum * this.alpha);
    }

//====================================================================================
    public void buildWordTopicsProb(){

        ArrayList<Float> tmp = new ArrayList<Float>();
        this.wordTopicProb = new Hashtable<String, ArrayList<Float>>();

        for(int i=0;i<this.allWords;i++){
            //get this word
            String w = wIdxInv.get(i);
//            System.out.print(w + " | ");

            //compute its probability in each topic
            for (int j = 0; j < this.topNum; j++) {
                float tprob = getTopicWordProbability(w, j);
//                System.out.print("Topic" + j + ": " + tprob + " | ");
                tmp.add(j, tprob);
            }
            this.wordTopicProb.put(w,new ArrayList<Float>(tmp));
            tmp.clear();

//            System.out.println();
        }
        System.out.println(wordTopicProb);
    }

    public void buildTopicDocProb(){
        ArrayList<Float> tmp = new ArrayList<Float>();
        this.topicDocProb = new Hashtable<Integer, ArrayList<Float>>();

        for(int i=0;i<this.topNum;i++){
            //get this word
//            String w = wIdxInv.get(i);
//            System.out.print("Topic" + i + " | ");

            //compute its probability in each topic
            for (int j = 0; j < this.docMat.size(); j++) {
                float tprob = getTopicDocProbability(j, i);
//                System.out.print("Doc" + j + ": " + tprob + " | ");
                tmp.add(j, tprob);
            }
            this.topicDocProb.put(i,new ArrayList<Float>(tmp));
            tmp.clear();
//            System.out.println();
        }
        System.out.println(topicDocProb);
    }

    //debugging
    public void printMatrix() {
        System.out.println("docTopMat:");
        for (int row = 0; row < this.topNum; row++) {
            for (int col = 0; col < this.docMat.size(); col++) {
                System.out.print(this.docTopMat.get(row, col) + " ");
            }
            System.out.println();
        }

        System.out.println("wordTopMat:");
        for (int row = 0; row < this.allWords; row++) {
            for (int col = 0; col < this.topNum; col++) {
                System.out.print(this.wordTopMat.get(row, col) + " ");
            }
            System.out.println();
        }
    }

    public void debugPrint(){

//        Random rand = new Random();
//        System.out.println(rand.nextInt());

        System.out.println("current state: " + currS);
//        System.out.println(wordTopMat.viewPart(0,2,0,2));

        System.out.println("wordTopicProb: " + wordTopicProb);
        System.out.println("NWT: " + NWT);
        System.out.println("wIdx: " + wIdx);
        System.out.println("wIdxInv: " + wIdxInv);

        System.out.println("docTopMat: ");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j <2 ; j++) {
                System.out.print(docTopMat.get(i,j) + " ");
            }
            System.out.println();
        }

        System.out.println("wordTopMat: ");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j <2 ; j++) {
                System.out.print(wordTopMat.get(i,j) + " ");
            }
            System.out.println();
        }
    }

    //WordToDocument
    public void buildInvertedIndexDistribution(){

        ArrayList<Float> tmpVector = new ArrayList<Float>();
        Float mag1 = 0.0f;
        Float mag2 = 0.0f;

        invertedIndexDist = new Hashtable<String, ArrayList<Float>>();
        ArrayList<Float> tmp = new ArrayList<Float>();

        for (int i=0; i<this.allWords; i++) { // WordTopic row

            //Magnitude of first vector(row) in WordTopic Matrix
            mag2 = getMagnitude(wordTopicProb.get(wIdxInv.get(i)));

            for (int j = 0; j < this.docMat.size(); j++) { // TopicDoc col
                float sum = 0.0f;
                tmpVector.clear();
                for (int k = 0; k <this.topNum ; k++) { // WordTopic col
                    tmpVector.add(topicDocProb.get(k).get(j));
                    sum += wordTopicProb.get(wIdxInv.get(i)).get(k) * topicDocProb.get(k).get(j);
                }

                //Magnitude of first vector(column) in TopicDoc Matrix
                mag1 = getMagnitude(tmpVector);
                float m = mag1 * mag2;
                sum /= m;

                tmp.add(j,sum);
            }
            invertedIndexDist.put(wIdxInv.get(i),new ArrayList<Float>(tmp));
            tmp.clear();
        }
        System.out.println(invertedIndexDist);
    }

    public Float getMagnitude(ArrayList<Float> d){

        float x;
        float sum = 0.0f;

        for (int i = 0; i <d.size() ; i++) {
            x = d.get(i);
            sum += (x * x);
        }
        return (float)Math.sqrt(sum);
    }

//    public void dist(ArrayList<Float> d, float mag) {
//
//        float x,s ;
//        for (int i = 0; i < d.size(); i++) {
//            x = d.get(i);
//            s = x / mag;
//            d.set(i,s);
//
//        }
//    }

//====================================================================================

    public void runLDA(int epochs) {
        //Train
        reset();
        for (int i = 1; i <= epochs; i++) {
            for (int j = 0; j < this.docMat.size(); j++) {
                GibbsSampler(j);
            }
        }
    }
}