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

    private Hashtable<String, ArrayList<Float>> wordTopicProb; //to map words to their probability
    private Hashtable<Integer, ArrayList<Float>> topicDocProb; //to map topics to their probability
    private Hashtable<String, ArrayList<Float>> invertedIndexDist; //Inverted idx-WordDoc

    private Map<Integer, Float> NWT = new HashMap<Integer, Float>(); //n of words in topic k
    private Map<String, Integer> wIdx = new HashMap<String, Integer>();
    private Map<Integer, String> wIdxInv = new HashMap<Integer, String>();
    private int allWords = 0;

    private float[] wordTopicMatrix_viewColumn_topicIndex; //to optimize the magnitude operation
    private float[] docTopicMatrix_viewColumn_docIndex;
    private ArrayList<Float> magTopDoc;

    private SparseMatrix wordTopMat; //number of words in each topic
    private SparseMatrix docTopMat;



    public ldaAlgo(int topNum, List<String> documents) throws IOException {
        this.topNum = topNum;

        this.magTopDoc=new ArrayList<Float>();
        wordTopicMatrix_viewColumn_topicIndex=new float[topNum];
        docTopicMatrix_viewColumn_docIndex=new float[documents.size()];

        for (String doc : documents) {
            ArrayList<String> document = new ArrayList<String>();
            ArrayList<Integer> state = new ArrayList<Integer>();

            String tokens[] = doc.split(" "); //Discuss Manga?!
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

//        this.docTopMat = new float[topNum][this.docMat.size()];
//        this.wordTopMat = new float[allWords][topNum];

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


    public void pre()
    {
        for(int i=0;i<this.topNum;i++)
            wordTopicMatrix_viewColumn_topicIndex[i] = (float) this.wordTopMat.getColumn(i).zSum();

        for(int i=0;i<this.docTopicMatrix_viewColumn_docIndex.length;i++)//docs
            docTopicMatrix_viewColumn_docIndex[i]=(float)this.docTopMat.getColumn(i).zSum();
    }

    //P(w|k)
    public float getTopicWordProbability(String word, int topicIdx) {
        int wordIndex = this.wIdx.get(word);
        float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx);
        float NWT = this.wordTopicMatrix_viewColumn_topicIndex[topicIdx];

        return (Nwk + this.beta) / (NWT + this.allWords * this.beta);
    }

    //P(k|d)
    public float getTopicDocProbability(int docIdx, int topicIdx) {
        float NKj = (float)this.docTopMat.get(topicIdx, docIdx);
        float Nj =  docTopicMatrix_viewColumn_docIndex[docIdx];

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
    public Hashtable<String, ArrayList<Float>> buildInvertedIndexDistribution(){

//        System.out.println("String Cal. magTopDoc");
        ArrayList<Float> xx = new ArrayList<Float>();
        for (int j = 0; j < this.docMat.size(); j++)
        {
            for (int k = 0; k < this.topNum; k++)
            {
                xx.add(topicDocProb.get(k).get(j));
            }
            float mag1 = getMagnitude(xx);
            this.magTopDoc.add(mag1);
            xx.clear();
        }

//        System.out.println("Cal. magTopDoc is Done");

        Float magTopicDoc = 0.0f;
        Float magWordTopic = 0.0f;

        invertedIndexDist = new Hashtable<String, ArrayList<Float>>();
        ArrayList<Float> tmp = new ArrayList<Float>();

        int done=0;
        System.out.println(this.allWords*this.docMat.size()*this.topNum);
//        System.out.println(this.allWords*this.docMat.size()*this.topNum);

        //Get distance and build InvIdx
        for (int i=0; i<this.allWords; i++) { // WordTopic row

            //Magnitude of first vector(row) in WordTopic Matrix
            magWordTopic = getMagnitude(wordTopicProb.get(wIdxInv.get(i)));

            for (int j = 0; j < this.docMat.size(); j++) { // TopicDoc col
                float sum = 0.0f;
                for (int k = 0; k <this.topNum ; k++) { // WordTopic col

                    //?!!
//                    if(done%1000000==0)
                        System.out.println(done);
                    sum += wordTopicProb.get(wIdxInv.get(i)).get(k) * topicDocProb.get(k).get(j);
                    done++;
                }
                //Magnitude of first vector(column) in TopicDoc Matrix
                magTopicDoc = this.magTopDoc.get(j);
                float m = magTopicDoc * magWordTopic;
                sum /= m;
                tmp.add(j,sum);
            }
            invertedIndexDist.put(wIdxInv.get(i),new ArrayList<Float>(tmp));
            tmp.clear();
        }
        System.out.println(invertedIndexDist);
        return invertedIndexDist;
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