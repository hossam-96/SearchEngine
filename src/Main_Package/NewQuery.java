package Main_Package;
import Crawler.Database;
import com.mongodb.*;

import javax.sound.midi.Soundbank;
import java.util.*;

/**
 * Created by moham on 3/11/2017.
 */
public class NewQuery
{
    private String Url;
    private String Title;
    private Cleaner C_Query;
    public int Type;
    public String[] Terms;
    private MongoDB DB;
    private Database MysqlDb;
    public NewQuery()throws Exception
    {
        this.C_Query=new Cleaner();
        this.DB=new MongoDB();
        this.MysqlDb=new Database();
    }

    private void Set_Query_Type(String q)
    {
        if(q.substring(0,1).equals("'")&&q.substring(q.length()-1,q.length()).equals("'"))
        {
            this.Type=1;
        }else
        this.Type=0;
    }

    private void Exteact_Query_Key_Terms(String q)
    {
        if(this.Type==1)
        {
            this.Terms=this.C_Query.Clean_Text_lossless(q.substring(1,q.length()-1));
        }
        else
            this.Terms=this.C_Query.Clean_Text(q);
    }

    private Set<Integer> Get_Query_Doucments()
    {
        if(this.Type==0)
        {
            return DB.WordToDoucmentGetUnion(this.Terms);
        }
        else return DB.WordToDoucmentGetIntersection(this.Terms);
    }

    private ArrayList<Integer> Rank_Doucments(Set<Integer> Doucment_Nums)
    {
        Map<Integer,Double> Dis=new HashMap<Integer,Double>();
        ArrayList<Integer> Sorted_Doucment_Nums=new ArrayList<Integer>();
        double Num_of_Doucment=Doucment_Nums.size();
        for (int k:Doucment_Nums)
        {
            String DoucmentNumberString=Integer.toString(k);
            while (DoucmentNumberString.length()!=6)DoucmentNumberString="0"+DoucmentNumberString;
            Sorted_Doucment_Nums.add(k);
            double Distance_between_Doucment_k_and_Query=0;
            ArrayList<String>WordDocument=new ArrayList<>();
            for (int i=0;i<Terms.length;i++)
            {
                WordDocument.add(Terms[i]+DoucmentNumberString);
            }
            Distance_between_Doucment_k_and_Query=DB.LdaWordDoucmentGet(WordDocument);
            Dis.put(k,Distance_between_Doucment_k_and_Query);
        }
        //System.out.println(Dis);
        Collections.sort(Sorted_Doucment_Nums, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return Dis.get(lhs) > Dis.get(rhs) ? -1 : (Dis.get(lhs) > Dis.get(rhs)) ? 1 : 0;
            }
        });
        return Sorted_Doucment_Nums;
    }

    private String get_content(int Doucment_Num)
    {
        String DoucmentNumberString=Integer.toString(Doucment_Num);
        while (DoucmentNumberString.length()!=6)DoucmentNumberString="0"+DoucmentNumberString;

        String content="";
        int First_Pos;
        if(this.Type==1)
            First_Pos=DB.PharsingQueryFirstPos.get(Doucment_Num);
        else
        {
            String Important_Term="";
            double Max_Relev=-100;
            for(int i=0;i<Terms.length;i++)
            {
                ArrayList<String> Term=new ArrayList<>();
                Term.add(Terms[i]+DoucmentNumberString);
                double cur=DB.LdaWordDoucmentGet(Term);
                if(cur>Max_Relev&&Terms[i].equals("Stopword")==false)
                {
                    Max_Relev=cur;
                    Important_Term=Terms[i];
                }
            }
            First_Pos=DB.WordDoucmentToPosGetFirstPos(Important_Term,DoucmentNumberString,"");
        }
        //System.out.print(Doucment_Num+"=>"+First_Pos);
        //System.out.println(this.Idx.Doucment_To_Words.get(Doucment_Num));
        if(First_Pos!=-1)
        {
            String Loss="";
            if(this.Type==1)Loss="_Lossless";
            DBObject Page=DB.DoucmentToWordsGetSlice(First_Pos,Doucment_Num,Loss);
            ArrayList<String>WordsBeforCleaning=(ArrayList<String>)Page.get("WordsBeforCleaning");
            ArrayList<String>WordsAfterCleaning=(ArrayList<String>)Page.get("WordsAfterCleaning");
            this.Url=(String)Page.get("Url");
            this.Title=(String)Page.get("Title");
            for(int i=0;i< Math.min(WordsBeforCleaning.size(), WordsAfterCleaning.size());i++)
            {
                Boolean Bold=false;
                for(int j=0;j<Terms.length;j++)
                {
                    if(WordsAfterCleaning.get(i).equals(Terms[j])&&Terms[j].equals("Stopword")==false){
                        Bold=true;
                        break;
                    }
                }
                String Doucment_Term;
                if(this.Type==0)
                Doucment_Term=WordsBeforCleaning.get(i);
                else  Doucment_Term=WordsAfterCleaning.get(i);
                if(Bold)content+=" <Strong>"+Doucment_Term+"</Strong>";
                else
                content+=" "+Doucment_Term;
            }
        }
        return content.trim();
    }

    public  ArrayList<QRT>  Add_Query(String q)throws Exception
    {
        this.MysqlDb.insertQuery(q);
        this.Set_Query_Type(q);
        this.Exteact_Query_Key_Terms(q);

        ArrayList<Integer> RD;
        /*if(this.Type==1)
        {
            RD=new ArrayList<Integer>();
            RD.addAll(this.Get_Query_Doucments());
        }
        else*/
        RD=this.Rank_Doucments(this.Get_Query_Doucments());
        ArrayList<QRT> Ret=new ArrayList<QRT>();
        for(int i=0;i<RD.size();i++)
        {
            String Content=get_content(RD.get(i));
            //System.out.println(this.Url +" "+RD.get(i));
            Ret.add(new QRT(this.Url,this.Title,RD.get(i),Content));
        }
        return Ret;
    }

    public static void main(String[] args)throws Exception
    {
        NewQuery Q=new NewQuery();
        Q.Add_Query("Hannah Baker");
    }
}
