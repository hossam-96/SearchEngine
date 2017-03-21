package com.nlp.lda;

import java.io.IOException;
import java.util.*;
import org.apache.mahout.math.SparseMatrix;


public class ldaAlgo {

    private int topNum; //no. of my clustering centroids
    private List<ArrayList<String>> docMat = new ArrayList<ArrayList<String>>(); //container of my docs
    private List<ArrayList<Integer>> currS = new ArrayList<ArrayList<Integer>>(); //current state(assignment) of this word in that doc
    private Map<Integer, Float> NWT = new HashMap<Integer, Float>(); //n of words in topic k


    private Hashtable<String, ArrayList<Float>> wordTopicProb; //to map words to their topic probability
    private Hashtable<Integer, ArrayList<Float>> topicDocProb; //documents distribution on each topic
    private Hashtable<String, ArrayList<Float>> invertedIndexDist; //Inverted idx-WordDoc


    //==============================================================indexed words
    private Map<String, Integer> wIdx = new HashMap<String, Integer>();
    private Map<Integer, String> wIdxInv = new HashMap<Integer, String>();
    private int allWords = 0; //my vocab world
    //============================================================================

    //====================Optimize the magnitude operation in distance calculation
    private float[] wordTopicMatrix_viewColumn_topicIndex;
    private float[] docTopicMatrix_viewColumn_docIndex;
    private ArrayList<Float> magTopDoc;
    //============================================================================

    private SparseMatrix wordTopMat; //number of word i in this topic k
    private SparseMatrix topDocMat; //no of words in this doc j assigned to topic k

    //======================================================HyperParams (as bias)
    private float alpha = 0.01f; //common initialization
    private float beta = 0.01f;
    //============================================================================


    public ldaAlgo(int topNum, List<String> documents) throws IOException {
        this.topNum = topNum;

        this.magTopDoc=new ArrayList<Float>();
        wordTopicMatrix_viewColumn_topicIndex=new float[topNum];
        docTopicMatrix_viewColumn_docIndex=new float[documents.size()];

        for (String doc : documents) {
            ArrayList<String> document = new ArrayList<String>();
            ArrayList<Integer> state = new ArrayList<Integer>();

            String tokens[] = doc.split(" ");
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
        //Edit this
        int DMdims[] = {topNum, this.docMat.size()};
        this.topDocMat = new SparseMatrix(DMdims);
        int WDdims[] = {this.allWords, topNum};
        this.wordTopMat = new SparseMatrix(WDdims);

        //init NWT
        for (int idx = 0; idx < topNum; idx++) {
            this.NWT.put(idx, 0.0f);
        }
    }



    //=======================================debugging purpose functions
    public void reset() {
        for (int row = 0; row < this.topNum; row++) {
            for (int col = 0; col < this.docMat.size(); col++) {
                this.topDocMat.set(row, col, 0.0);
            }
        }
        for (int row = 0; row < this.allWords; row++) {
            for (int col = 0; col < this.topNum; col++) {
                this.wordTopMat.set(row, col, 0.0);
            }
        }
    }


    public void printMatrix() {
        System.out.println("topDocMat:");
        for (int row = 0; row < this.topNum; row++) {
            for (int col = 0; col < this.docMat.size(); col++) {
                System.out.print(this.topDocMat.get(row, col) + " ");
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

    //======================================================================


    //===========================Gibbs Sampler(Training and clustering part)
    private void GibbsSampler(int docIdx) {
        //printMatrix();
        //System.out.println("doc idx=" + docIdx);
        Random rand = new Random();
        ArrayList<String> doc = this.docMat.get(docIdx);
        ArrayList<Integer> state = this.currS.get(docIdx);
        int idx = 0;
        for (String w : doc) {
			//The assumptiion here is that all words' states but the current are true 
            int zij = state.get(idx);    //current assignment for this word (topic of word i in doc j)
//            System.out.print("current state of word " + word + " --> zij=" + zij + " - ");
//            System.out.println("word=" + word);
            int wordIndex = this.wIdx.get(w);
//            System.out.print(word + "'s idx = " + wordIndex + " - "); //comm
//            System.out.println(); //del
            float u = rand.nextFloat();
            float[] Prob = new float[this.topNum + 1];
            Prob[0] = 0.0f;
            for (int topicIdx = 1; topicIdx <= this.topNum; topicIdx++) {

                float NKj = (float)this.topDocMat.get(topicIdx - 1, docIdx); //no of words in this doc j assigned to topic k
                float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx - 1); //no times word i assigned to this topic
                float NWT = this.NWT.get(topicIdx - 1); //total no of words in this topic
                if (zij == (topicIdx - 1)) {
                    //if my current topic(zij) = specific topic so decrement 1 from its frequencies
                    //because it`s assumed to be wrong,, then do the calculations and re-assign your values
                    NKj -= 1.0;
                    Nwk -= 1.0;
                    NWT -= 1.0;
                }
                Prob[topicIdx] = Prob[topicIdx - 1] + (NKj + this.alpha) * (Nwk + this.beta) / (NWT + this.allWords * this.beta);
                //Normalization constant 
            }
            for (int topicIdx = 0; topicIdx <= this.topNum; topicIdx++) {
                System.out.print(Prob[topicIdx] + " ");
            }
            System.out.println();

            //===========Updating and Re-assigning
            for (int topicIdx = 1; topicIdx <= this.topNum; topicIdx++) {
                if (u <= Prob[topicIdx] / Prob[this.topNum]) {

                    state.set(idx, topicIdx - 1); //you picked a specific topic to be the true one topic

                    //so update NKj, Nwk, and NWT
                    //first get this topic frequencies.
                    float newNKj = (float)this.topDocMat.get(topicIdx - 1, docIdx);
                    float newNwk = (float)this.wordTopMat.get(wordIndex, topicIdx - 1);
                    float newNWT = this.NWT.get(topicIdx - 1);
                    if (zij != (topicIdx - 1)) {
                        //Then increment its frequencies with 1 if it wasn`t the initial one from the beginning
                        this.topDocMat.set(topicIdx - 1, docIdx, (newNKj + 1.0));
                        this.wordTopMat.set(wordIndex, topicIdx - 1, (newNwk + 1.0));
                        this.NWT.put(topicIdx - 1, (newNWT + 1.0f));
                        //Now decrement the old topic frequencies by 1
                        if (zij >= 0) { //not the -1 initialized
                            float oldNKj = (float)this.topDocMat.get(zij, docIdx);
                            float oldNwk = (float)this.wordTopMat.get(wordIndex, zij);
                            float oldNWT = this.NWT.get(zij);
                            this.topDocMat.set(zij, docIdx, (oldNKj - 1.0));
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
    //======================================================================


    //=====================================Calculations to prepare our invIdx=================================

    public Float getMagnitude(ArrayList<Float> d){

        float x;
        float sum = 0.0f;

        for (int i = 0; i <d.size(); i++) {
            x = d.get(i);
            sum += (x * x);
        }
        return (float)Math.sqrt(sum);
    }

    public void optimize()
    {
        for(int i=0;i<this.topNum;i++)
            wordTopicMatrix_viewColumn_topicIndex[i] = (float) this.wordTopMat.getColumn(i).zSum();

        for(int i=0;i<this.docTopicMatrix_viewColumn_docIndex.length;i++)//docs
            docTopicMatrix_viewColumn_docIndex[i]=(float)this.topDocMat.getColumn(i).zSum();
    }

    //P(w|k)
    public float getTopicWordProbability(String word, int topicIdx) {
        int wordIndex = this.wIdx.get(word);
        float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx); //no times word i assigned to this topic
        float NWT = this.wordTopicMatrix_viewColumn_topicIndex[topicIdx]; //total no of words in this topic

        //probability(distro) that this word is in this topic
        return (Nwk + this.beta) / (NWT + this.allWords * this.beta);
    }

    //P(k|d)
    public float getTopicDocProbability(int docIdx, int topicIdx) {
        float NKj = (float)this.topDocMat.get(topicIdx, docIdx); //no of words in this doc j assigned to topic k
        float Nj =  docTopicMatrix_viewColumn_docIndex[docIdx]; //total words in this doc

        //distribution of this word in that doc
        return (NKj + this.alpha) / (Nj + this.topNum * this.alpha);
    }

//======================================Building the distribution container and the invIdx
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


    //WordToDocument
    public Hashtable<String, ArrayList<Float>> buildInvertedIndexDistribution(){

//        System.out.println("Cal. magTopDoc");
        //in order not to calc it every tie in the loop
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
//        System.out.println(this.allWords*this.docMat.size()*this.topNum);

        //Get distance and build InvIdx
        for (int i=0; i<this.allWords; i++) { // WordTopic row

            //Magnitude of first vector(row) in WordTopic Matrix
            magWordTopic = getMagnitude(wordTopicProb.get(wIdxInv.get(i)));

            for (int j = 0; j < this.docMat.size(); j++) { // TopicDoc col
                float sum = 0.0f;
                for (int k = 0; k <this.topNum ; k++) { // WordTopic col

//                    if(done%1000000==0)
//                        System.out.println(done); //print eevery 1000000 it
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
    //==============================================================================


//===============================Testing============================================

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
