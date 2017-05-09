package Main_Package;

import java.io.IOException;
import java.util.*;
import org.apache.mahout.math.SparseMatrix;


public class ldaAlgo {


    //for Debuge
    private long LDA_Done;
    private long LDA_all;
    private long LDA_Done_Print;
    //==========================================================
    private int topNum; //no. of my clustering centroids
    private List<ArrayList<String>> docMat = new ArrayList<ArrayList<String>>(); //container of my docs
    private List<ArrayList<Integer>> currS = new ArrayList<ArrayList<Integer>>(); //current state(assignment) of this word in that doc
    private Map<Integer, Float> NWT = new HashMap<Integer, Float>(); //n of words in topic k


    private Hashtable<String, ArrayList<Float>> wordTopicProb; //to map words to their topic probability
    private Hashtable<Integer, ArrayList<Float>> topicDocProb; //documents distribution on each topic
    private Hashtable<String, ArrayList<Float>> invertedIndexDist; //Inverted idx-WordDoc


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


    public ldaAlgo(int topNum, List<String> docs) throws IOException {

        this.topNum = topNum;

        this.magTopDoc=new ArrayList<Float>();
        wordTopicMatrix_clacColumn_topicIndex=new float[topNum];
        docTopicMatrix_clacColumn_docIndex=new float[docs.size()];

        for (String d : docs) {

            ArrayList<Integer> s = new ArrayList<Integer>();

            ArrayList<String> doc = new ArrayList<String>();

            String vocabs[] = d.split(" "); //split vocabs
//            System.out.println(d);

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
        this.topDocMat = new SparseMatrix(topNum, this.docMat.size());
        this.wordTopMat = new SparseMatrix(this.allWords, topNum);

        //init NWT
        for (int i = 0; i < topNum; i++)
            this.NWT.put(i, 0.0f);


        alpha = 0.01f; //common initialization
        beta = 0.01f;
    }


    //=======================================debugging purpose functions
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


    public void printContainers() {
        System.out.println("topDocMat:");
        for (int r = 0; r < this.topNum; r++) {
            for (int c = 0; c < this.docMat.size(); c++) {
                System.out.print(this.topDocMat.get(r, c) + " ");
            }
            System.out.println();
        }

        System.out.println("wordTopMat:");
        for (int r = 0; r < this.allWords; r++) {
            for (int c = 0; c < this.topNum; c++) {
                System.out.print(this.wordTopMat.get(r, c) + " ");
            }
            System.out.println();
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

    //======================================================================


    //===========================Training and clustering part
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

    //P(w|k)
    public float getTopicWordProbability(String word, int topicIdx) {
        int wordIndex = this.wIdx.get(word);
        float Nwk = (float)this.wordTopMat.get(wordIndex, topicIdx); //no times word i assigned to this topic
        float NWT = this.wordTopicMatrix_clacColumn_topicIndex[topicIdx]; //total no of words in this topic

        //probability(distro) that this word is in this topic
        return (Nwk + this.beta) / (NWT + this.allWords * this.beta);
    }

    //P(k|d)
    public float getTopicDocProbability(int docIdx, int topicIdx) {
        float NKj = (float)this.topDocMat.get(topicIdx, docIdx); //no of words in this doc j assigned to topic k
        float Nj =  docTopicMatrix_clacColumn_docIndex[docIdx]; //total words in this doc

        //distribution of this word in that doc
        return (NKj + this.alpha) / (Nj + this.topNum * this.alpha);
    }

//======================================Building the distribution container and the invIdx
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

    public void buildTopicDocProb(){
        ArrayList<Float> tmp = new ArrayList<Float>();
        this.topicDocProb = new Hashtable<Integer, ArrayList<Float>>();

        long All=(long)this.docMat.size()*(long)this.topNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);

        long f=All/100;


        for(int i=0;i<this.topNum;i++){
            //get this word
//            String w = wIdxInv.get(i);
//            System.out.print("Topic" + i + " | ");

            //compute its probability in each topic
            for (int j = 0; j < this.docMat.size(); j++) {
                float tprob = getTopicDocProbability(j, i);
//                System.out.print("Doc" + j + ": " + tprob + " | ");
                tmp.add(j, tprob);

                if(Done%f==0)
                    System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
                Done++;
            }
            this.topicDocProb.put(i,new ArrayList<Float>(tmp));
            tmp.clear();
        }
        System.out.println();
//        System.out.println(topicDocProb);
    }


    //WordToDocument
    public Hashtable<String, ArrayList<Float>> buildInvertedIndexDistribution(){

//        System.out.println("Cal. magTopDoc");
        //in order not to calc it every tie in the loop

        System.out.println("Starting Cal. MagOne");
        long All=(long)this.docMat.size()*(long)this.topNum;
        long Done=0;
        System.out.println("Max Num OF Iteration #"+All);
        long f=All/100;
        ArrayList<Float> xx = new ArrayList<Float>();
        for (int j = 0; j < this.docMat.size(); j++)
        {
            for (int k = 0; k < this.topNum; k++)
            {
                xx.add(topicDocProb.get(k).get(j));
                if(Done%f==0)
                    System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
                Done++;
            }
            float mag1 = getMagnitude(xx);
            this.magTopDoc.add(mag1);
            xx.clear();
        }
        System.out.println();
//        System.out.println("Cal. magTopDoc is Done");

        Float magTopicDoc = 0.0f;
        Float magWordTopic = 0.0f;

        invertedIndexDist = new Hashtable<String, ArrayList<Float>>();
        ArrayList<Float> tmp = new ArrayList<Float>();


//        System.out.println(this.allWords*this.docMat.size()*this.topNum);



        System.out.println("LDA buildInvertedIndexDistribution Start");
        long done=0;
        long all=this.allWords;all*=this.docMat.size();all*=this.topNum;
        System.out.println("Max Num OF Iteration #"+all);
        f=all/100;
        //Get distance and build InvIdx
        for (int i=0; i<this.allWords; i++) { // WordTopic row

            //Magnitude of first vector(row) in WordTopic Matrix
            magWordTopic = getMagnitude(wordTopicProb.get(wIdxInv.get(i)));

            for (int j = 0; j < this.docMat.size(); j++) { // TopicDoc col
                float sum = 0.0f;
                for (int k = 0; k <this.topNum ; k++) { // WordTopic col

                    if(done%f==0)
                        System.out.print("\r"+Math.ceil((done*100.00)/all)+"%");
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
        System.out.println();
//        System.out.println(invertedIndexDist);
        return invertedIndexDist;
    }
    //==============================================================================

//============================================================Analyzing search query
    public Hashtable<String, ArrayList<Float>> analyzeSearchQuery(String query){

        //Holds distro of every word im my query to get the avg later
        Hashtable<String, ArrayList<Float>> getQueryDistro = new Hashtable<String, ArrayList<Float>>();

        //zeroes array to handle not found words(if any)
        //querylength
        ArrayList<Float> zeroes = new ArrayList<Float>(Collections.nCopies(this.topNum, 0.0f));
        //querylength
        String slices[] = query.split(" ");

//        System.out.println("our lovely words: ");
//        for (int i = 0; i <slices.length ; i++) {
//            System.out.print(slices[i] + " ");
//        }
//
//        System.out.println("Query length: " + query.length() + " slices length: " + slices.length);

        //3*querylength
        for (int i = 0; i <slices.length ; i++) {

            if(this.wordTopicProb.get(slices[i]) != null)
                getQueryDistro.put(slices[i], new ArrayList<Float>(this.wordTopicProb.get(slices[i])));
            else
                getQueryDistro.put(slices[i], new ArrayList<Float>(zeroes));
        }
        //complexity about 5*query length (about 100 iterations in real world example)

        return getQueryDistro;
    }
    //==============================================================================

//Getting query distro==============================================================
    public ArrayList<Float> queryDistro(Hashtable<String, ArrayList<Float>> query){

        float n = (float)query.size();

        //numtopics
        ArrayList<Float> queryTopicsDistro = new ArrayList<Float>(Collections.nCopies(this.topNum, 0.0f));

        //numtopics*querylngth
        for (String key : query.keySet()) {

            ArrayList<Float> tmp = query.get(key);
//            System.out.println("------------------------------------------------");
//            System.out.println("key: " + key + " value: " + query.get(key));
//            System.out.println("tmp :" + tmp);

            for(int i=0; i<this.topNum; i++) {
                queryTopicsDistro.set(i, queryTopicsDistro.get(i) + tmp.get(i));
            }
//            System.out.println("queryTopicsDistro: " + queryTopicsDistro);
        }

//        System.out.println("Lovely container bef AVG: "+ queryTopicsDistro);

        //3*numtopics
        for (int i = 0; i <this.topNum ; i++)
            queryTopicsDistro.set(i, queryTopicsDistro.get(i)/n);

//        System.out.println("Lovely container after AVG: " + queryTopicsDistro);

        //so complexity = 4*numtopics + queryLength*numtopics => about 2500 iterations in real example

        return queryTopicsDistro;
    }



//===============================Testing============================================

    public void Train_LDA(int epochs,long ALL_WORDS_NUM) {
        LDA_all=ALL_WORDS_NUM;LDA_all*=this.topNum;
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
    }
}

