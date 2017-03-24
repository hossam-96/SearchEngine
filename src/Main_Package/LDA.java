package Main_Package;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.math.BigInteger;
import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.Vector;
//import java.util.Vector;

/**
 * TODO Latent Dirichlet Allocation algorithm.
 *
 * @author root.
 *         Created Oct 12, 2014.
 */
public class LDA {
    private long LDA_Done;
    private long LDA_all;
    private long LDA_Done_Print;
    private float alpha = 0.01f;
    private float beta = 0.01f;
    private int topicsNum;
    private SparseMatrix docTopicMatrix;
    private SparseMatrix wordTopicMatrix;

    private Hashtable<String, ArrayList<Float>> wordTopicProb; //to map words to their probability
    private Hashtable<Integer, ArrayList<Float>> topicDocProb; //to map topics to their probability
    private Hashtable<String, ArrayList<Float>> invertedIndexDist; //Inverted index
//    private Float invertedIndex [][];


    private Map<Integer, Float> NK = new HashMap<Integer, Float>();
    private List<ArrayList<String>> documentMatrix = new ArrayList<ArrayList<String>>();
    private List<ArrayList<Integer>> currentState = new ArrayList<ArrayList<Integer>>();
    private Map<String, Integer> wordIndexMap = new HashMap<String, Integer>();
    private Map<Integer, String> wordIndexInverseMap = new HashMap<Integer, String>();
    private int vocabSize = 0;
    private float[] wordTopicMatrix_viewColumn_topicIndex;
    private float[] docTopicMatrix_viewColumn_docIndex;
    private ArrayList<Float> MagOne;
    /**
     * TODO LDA constructor.
     *
     * @param topicsNum
     * @param documents
     * @throws IOException
     */
    public LDA(int topicsNum, List<String> documents) throws IOException {
        this.MagOne=new ArrayList<Float>();
        wordTopicMatrix_viewColumn_topicIndex=new float[topicsNum];
        docTopicMatrix_viewColumn_docIndex=new float[documents.size()];
        this.topicsNum = topicsNum;
        //build document matrix: assuming we receive a clean version of tweets
        for (String doc : documents) {
            ArrayList<String> document = new ArrayList<String>();
            ArrayList<Integer> state = new ArrayList<Integer>();
            //System.out.println(doc);
            String docWoSW = doc;    //without remove stop words
            String tokens[] = docWoSW.split(" ");
            for (String token : tokens) {
                document.add(token);
                state.add(-1);
                if (!this.wordIndexMap.containsKey(token)) {
                    this.wordIndexMap.put(token, this.vocabSize);
                    this.wordIndexInverseMap.put(this.vocabSize, token);
                    this.vocabSize++;
                }
            }
            this.documentMatrix.add(document);
            this.currentState.add(state);
        }
        int documentMatrixCardinality[] = {topicsNum, this.documentMatrix.size()};
        this.docTopicMatrix = new SparseMatrix(topicsNum,this.documentMatrix.size());
        int wordTopicMatrixCardinality[] = {this.vocabSize, topicsNum};
        this.wordTopicMatrix = new SparseMatrix(this.vocabSize,topicsNum);

        //Mapping Words distribution on Docs
//        this.invertedIndex = new Float[this.vocabSize][this.documentMatrix.size()];

        //init NK
        for (int index = 0; index < topicsNum; index++) {
            this.NK.put(index, 0.0f);
        }
    }

    //debug purpose

    /**
     * TODO Print docTopic and wordTopic matrixes.
     */
    public void printMatrix() {
        System.out.println("docTopicMatrix:");
        for (int row = 0; row < this.topicsNum; row++) {
            for (int col = 0; col < this.documentMatrix.size(); col++) {
                System.out.print(this.docTopicMatrix.get(row, col) + " ");
            }
            System.out.println();
        }

        System.out.println("wordTopicMatrix:");
        for (int row = 0; row < this.vocabSize; row++) {
            for (int col = 0; col < this.topicsNum; col++) {
                System.out.print(this.wordTopicMatrix.get(row, col) + " ");
            }
            System.out.println();
        }
    }





    /**
     * TODO clean matrixes.
     */
    public void resetMatrix() {
        for (int row = 0; row < this.topicsNum; row++) {
            for (int col = 0; col < this.documentMatrix.size(); col++) {
                this.docTopicMatrix.set(row, col, 0.0);
            }
        }
        for (int row = 0; row < this.vocabSize; row++) {
            for (int col = 0; col < this.topicsNum; col++) {
                this.wordTopicMatrix.set(row, col, 0.0);
            }
        }
    }

    //Gibbs Sampler
    private void runGibbsSampler(int docIndex) {
        //printMatrix();
        //System.out.println("doc index=" + docIndex);
        Random rand = new Random();
        ArrayList<String> doc = this.documentMatrix.get(docIndex);
        ArrayList<Integer> state = this.currentState.get(docIndex);
        int index = 0;
        for (String word : doc) {
            int zij = state.get(index);    //current assignment for this word
//            System.out.print("current state of word " + word + " --> zij=" + zij + " - "); //comm
//            System.out.println("word=" + word); //comm
            int wordIndex = this.wordIndexMap.get(word);
//            System.out.print(word + "'s index = " + wordIndex + " - "); //comm
//            System.out.println(); //del
            float u = rand.nextFloat();
            float[] Prob = new float[this.topicsNum + 1];
            Prob[0] = 0.0f;
            for (int topicIndex = 1; topicIndex <= this.topicsNum; topicIndex++) {
//                System.out.println("topic index=" + (topicIndex - 1)); //comm
                float Nkj = (float)this.docTopicMatrix.get(topicIndex - 1, docIndex);
//                System.out.println("Nkj = " + Nkj); //comm
                float Nwk = (float)this.wordTopicMatrix.get(wordIndex, topicIndex - 1);
//                System.out.println("Nwk = " + Nwk); //comm
                //the following two lines have been changed to address performance issue (by introducing NK ds)
                //Vector vec = wordTopicMatrix.getColumn(topicIndex - 1);
                //float Nk = vec.zSum();
                float Nk = this.NK.get(topicIndex - 1);    //performance issue
                //System.out.println("Nk = " + Nk + ", new NK=" + Nk_new);
                if (zij == (topicIndex - 1)) {
                    //we need adjustments
                    Nkj -= 1.0;
                    Nwk -= 1.0;
                    Nk -= 1.0;
                }
                Prob[topicIndex] = Prob[topicIndex - 1] + (Nkj + this.alpha) * (Nwk + this.beta) / (Nk + this.vocabSize * this.beta);
            }
            /*
			System.out.println("u=" + u);
			System.out.println("probability:");
			for(int topicIndex = 1; topicIndex <= topicsNum; topicIndex++){
				System.out.println(Prob[topicIndex]/Prob[topicsNum]);
			}
			*/
            //================msh fahem el for de
            for (int topicIndex = 1; topicIndex <= this.topicsNum; topicIndex++) {
                if (u <= Prob[topicIndex] / Prob[this.topicsNum]) {
                    state.set(index, topicIndex - 1);
                    //update Nkj, Nwk, and Nk
                    float newNkj = (float)this.docTopicMatrix.get(topicIndex - 1, docIndex);
                    float newNwk = (float)this.wordTopicMatrix.get(wordIndex, topicIndex - 1);
                    float newNk = this.NK.get(topicIndex - 1);
                    if (zij != (topicIndex - 1)) {
                        this.docTopicMatrix.set(topicIndex - 1, docIndex, (newNkj + 1.0));
                        this.wordTopicMatrix.set(wordIndex, topicIndex - 1, (newNwk + 1.0));
                        this.NK.put(topicIndex - 1, (newNk + 1.0f));
                        if (zij >= 0) {
                            float oldNkj = (float)this.docTopicMatrix.get(zij, docIndex);
                            float oldNwk = (float)this.wordTopicMatrix.get(wordIndex, zij);
                            float oldNk = this.NK.get(zij);
                            this.docTopicMatrix.set(zij, docIndex, (oldNkj - 1.0));
                            this.wordTopicMatrix.set(wordIndex, zij, (oldNwk - 1.0));
                            this.NK.put(zij, (oldNk - 1.0f));
                        }
                    }
                    break;    //done with this word
                }

            }
            LDA_Done+=this.topicsNum;
            if(LDA_Done/10000>LDA_Done_Print) {
                System.out.print("\r" + Math.floor((LDA_Done * 100.0) / LDA_all) + "%");
                LDA_Done_Print++;
            }
            index++;

        }
		/*
		System.out.println("state");
		for(int s : state){
			System.out.println(s);
		}
		*/
        this.currentState.set(docIndex, state);
    }

    /**
     * TODO get TopicWord probability.
     *
     * @param word
     * @param topicIndex
     * @return P(k|w)
     */

    public void pre()
    {
        long All=(long)this.docTopicMatrix_viewColumn_docIndex.length+(long)this.topicsNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);
        long f=All/100;

        for(int i=0;i<this.topicsNum;i++)
        {
            wordTopicMatrix_viewColumn_topicIndex[i]=(float)this.wordTopicMatrix.viewColumn(i).zSum();
            if(Done%f==0)
            System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
            Done++;
        }

        for(int i=0;i<this.docTopicMatrix_viewColumn_docIndex.length;i++)
        {
            docTopicMatrix_viewColumn_docIndex[i]=(float)this.docTopicMatrix.viewColumn(i).zSum();
            if(Done%f==0)
            System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
            Done++;
        }
        System.out.println();
    }

    public float getTopicWordProbability(String word, int topicIndex) {

        int wordIndex = this.wordIndexMap.get(word);
        float Nwk = (float)this.wordTopicMatrix.get(wordIndex, topicIndex);
        float Nk =  wordTopicMatrix_viewColumn_topicIndex[topicIndex];
        return (Nwk + this.beta) / (Nk + this.vocabSize * this.beta);
    }

    /**
     * TODO get TopicDoc probability.
     *
     * @param docIndex
     * @param topicIndex
     * @return P(k|d)
     */
    public float getTopicDocProbability(int docIndex, int topicIndex) {
        float Nkj = (float)this.docTopicMatrix.get(topicIndex, docIndex);
        float Nj =   docTopicMatrix_viewColumn_docIndex[docIndex];

//        System.out.println("Nkj=" + Nkj);
//        System.out.println("Nj=" + Nj);

        return (Nkj + this.alpha) / (Nj + this.topicsNum * this.alpha);
    }

//====================================================================================
    public Hashtable<String, ArrayList<Float>> getWordTopicsProb(){


        ArrayList<Float> tmp = new ArrayList<Float>();
        this.wordTopicProb = new Hashtable<String, ArrayList<Float>>();

        long All=(long)this.vocabSize*(long)this.topicsNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);
        long f=All/100;
        for(int i=0;i<this.vocabSize;i++){
            //get this word
            String w = wordIndexInverseMap.get(i);
//            System.out.print(w + " | ");

            //compute its probability in each topic
            for (int j = 0; j < this.topicsNum; j++) {

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
        return wordTopicProb;

    }

    public Hashtable<Integer, ArrayList<Float>> getTopicDocProb(){


        ArrayList<Float> tmp = new ArrayList<Float>();
        this.topicDocProb = new Hashtable<Integer, ArrayList<Float>>();
        long All=(long)this.documentMatrix.size()*(long)this.topicsNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);

        long f=All/100;
        for(int i=0;i<this.topicsNum;i++){
            //get this word
//            String w = wordIndexInverseMap.get(i);
//            System.out.print("Topic" + i + " | ");

            //compute its probability in each topic
            for (int j = 0; j < this.documentMatrix.size(); j++) {
                float tprob = getTopicDocProbability(j, i);
//                System.out.print("Doc" + j + ": " + tprob + " | ");
                tmp.add(j, tprob);

                if(Done%f==0)
                    System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
                Done++;
            }
            this.topicDocProb.put(i,new ArrayList<Float>(tmp));
            tmp.clear();

//            System.out.println();
        }
        System.out.println();
      return topicDocProb;
    }


    public Hashtable<String, ArrayList<Float>> buildInvertedIndexDistribution(){

        System.out.println("Starting Cal. MagOne");
        long All=(long)this.documentMatrix.size()*(long)this.topicsNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);
        long f=All/100;
        ArrayList<Float> xx = new ArrayList<Float>();
        for (int j = 0; j < this.documentMatrix.size(); j++)
        {
            for (int k = 0; k < this.topicsNum; k++)
            {
                xx.add(topicDocProb.get(k).get(j));
                if(Done%f==0)
                    System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
                Done++;
            }
            float mag1 = getMagnitude(xx);
            this.MagOne.add(mag1);
            xx.clear();
        }
        System.out.println();




        Float mag1 = 0.0f;
        Float mag2 = 0.0f;

        invertedIndexDist = new Hashtable<String, ArrayList<Float>>();
        ArrayList<Float> tmp = new ArrayList<Float>();
        System.out.println("LDA buildInvertedIndexDistribution Start");
        long done=0;
        long all=this.vocabSize;all*=this.documentMatrix.size();all*=this.topicsNum;
        System.out.println("Max Num OF Iteration #"+all);
        f=all/100;
        for (int i=0; i<this.vocabSize; i++) { // WordTopic row


            //Magnitude of first vector(row) in WordTopic Matrix
            mag2 = getMagnitude(wordTopicProb.get(wordIndexInverseMap.get(i)));

            for (int j = 0; j < this.documentMatrix.size(); j++) { // TopicDoc col
                float sum = 0.0f;
                for (int k = 0; k <this.topicsNum ; k++) { // WordTopic col

                    if(done%f==0)
                        System.out.print("\r"+Math.ceil((done*100.00)/all)+"%");
                    sum += wordTopicProb.get(wordIndexInverseMap.get(i)).get(k) * topicDocProb.get(k).get(j);
                    done++;
                }


                mag1=this.MagOne.get(j);
                float m = mag1 * mag2;
                sum /= m;

                tmp.add(j,sum);

            }

            invertedIndexDist.put(wordIndexInverseMap.get(i),new ArrayList<Float>(tmp));
            tmp.clear();

        }

//        for (int i = 0; i < vocabSize; i++) {
//            for (int j = 0; j <documentMatrix.size() ; j++) {
//                System.out.print(invertedIndex[i][j] + " ");
//            }
//            System.out.println();
//        }

        System.out.println();
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




    /**
     * TODO Print top words from a topic word distribution.
     *
     * @param topicIndex
     * @param top
     */
    public void printTopicTopWords(int topicIndex, int top) {
        Vector vec = this.wordTopicMatrix.viewColumn(topicIndex);
        int indexes[] = new int[top];
        Hashtable<Integer, Integer> indexMap = new Hashtable<Integer, Integer>();
        for (int index = 0; index < top; index++) {
            int maxInx = -1;
            double max = 0.0;
            //System.out.println("max=" + max + ", maxInx=" + maxInx);
            for (int wordInx = 0; wordInx < vec.size(); wordInx++) {
                //System.out.println("wordInx=" + wordInx + ", count=" + vec.get(wordInx));
                if (vec.get(wordInx) > max && !indexMap.containsKey(wordInx)) {
                    maxInx = wordInx;
                    max = vec.get(wordInx);
                }
            }
            //System.out.println("top word#" + index);
            //System.out.println("top word index=" + maxInx);
            if (maxInx >= 0) {
                indexMap.put(maxInx, 0);
                indexes[index] = maxInx;
            }
        }
		/*
		System.out.println("word index map:");
		for(int index = 0; index < vocabSize; index++){
			System.out.println(index + ":" + wordIndexInverseMap.get(index));
		}
		*/
        System.out.println("Top words for topic=" + topicIndex);
        for (int index = 0; index < top; index++) {
            if (vec.get(indexes[index]) > 0) {
                System.out.println(this.wordIndexInverseMap.get(indexes[index]) + ":" + getTopicWordProbability(this.wordIndexInverseMap.get(indexes[index]), topicIndex));
            }
        }
    }

    /**
     * TODO method for running LDA for times iterations.
     *
     * @param times
     */
    public void runLDA(int times,long ALL_WORDS_NUM) {
//        resetMatrix();
        LDA_all=ALL_WORDS_NUM;LDA_all*=this.topicsNum;
        System.out.println("Stating LDA Training");
        System.out.println("Max Num OF Iteration #"+LDA_all*times);
        for (int iteration = 1; iteration <= times; iteration++) {
            System.out.println("Stating LDA Iteration "+iteration);
            LDA_Done=0;
            LDA_Done_Print=0;
            for (int docIndex = 0; docIndex < this.documentMatrix.size(); docIndex++) {
                runGibbsSampler(docIndex);
            }
            System.out.println();
//            System.out.println("iteration #" + iteration); //com
//            printMatrix();

            SparseMatrix x ;
            SparseMatrix y;

        }
    }


}