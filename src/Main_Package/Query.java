package Main_Package;

import org.omg.PortableInterceptor.ServerRequestInfo;

import java.util.*;

/**
 * Created by moham on 3/11/2017.
 */
public class Query
{
    private Indexer Idx;
    private Cleaner C_Query;
    private int Type;
    private String[] Terms;

    public Query()
    {

        this.C_Query=new Cleaner();
        this.Idx=new Indexer(50);
        this.Idx.Create();
        this.Idx.Save();
        //this.Idx.Load();
    }

    private void Set_Query_Type(String q)
    {
        if(q.substring(0,1).equals("'")&&q.substring(q.length()-1,q.length()).equals("'"))
        {
            this.Type=1;
        }
        this.Type=0;
    }

    private void Exteact_Query_Key_Terms(String q)
    {
        if(this.Type==1)
        {
            this.Terms=this.C_Query.Clean_Text_lossless(q);
        }
        else
            this.Terms=this.C_Query.Clean_Text(q);
    }

    private Set<Integer> Get_Query_Doucments()
    {
        Set<Integer> Query_Doucments = new HashSet<Integer>();
        for(int i=0;i<Terms.length;i++)
        {
            if(this.Type==0)
            {
                if(this.Idx.Word_to_Doucment.containsKey(Terms[i]))
                Query_Doucments.addAll(this.Idx.Word_to_Doucment.get(Terms[i]));
            }
        }
        return Query_Doucments;
    }

    private ArrayList<Integer> Rank_Doucments(Set<Integer> Doucment_Nums)
    {
        Map<Integer,Float> Dis=new HashMap<Integer,Float>();
        ArrayList<Integer> Sorted_Doucment_Nums=new ArrayList<Integer>();
        float Num_of_Doucment=Doucment_Nums.size();
        for (int k:Doucment_Nums)
        {
            Sorted_Doucment_Nums.add(k);
            float Distance_between_Doucment_k_and_Query=0;
            for (int i=0;i<Terms.length;i++)
            {
                if(  this.Idx.Distance_Between_Word_and_Doucment.containsKey(Terms[i]))
                {
                    Distance_between_Doucment_k_and_Query+=this.Idx.Distance_Between_Word_and_Doucment.get(Terms[i]).get(k);
                }
            }
            Distance_between_Doucment_k_and_Query/=Num_of_Doucment;
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
        String content="";

        String Important_Term="";
        float Max_Relev=0;
        for(int i=0;i<Terms.length;i++)
        {
            float cur=this.Idx.Distance_Between_Word_and_Doucment.get(Terms[i]).get(Doucment_Num);
            if(cur>Max_Relev)
            {
                Max_Relev=cur;
                Important_Term=Terms[i];
            }
        }
        //System.out.println(Important_Term);
        int First_Pos=-1;
        for(Integer i:this.Idx.WordDoucment_to_pos.get(Important_Term).get(Doucment_Num))
        {
            First_Pos=i;
            break;
        }
        System.out.print(Doucment_Num+"=>"+First_Pos);
        System.out.println(this.Idx.Doucment_To_Words.get(Doucment_Num));
        if(First_Pos!=-1)
        {
            for(int i=First_Pos;i<Math.min(this.Idx.Doucment_To_Words.get(Doucment_Num).size(),First_Pos+30);i++)
            {
                String Doucment_Term=this.Idx.Doucment_To_Words.get(Doucment_Num).get(0).get(i);
                content+=" "+Doucment_Term;
            }
        }
        return content.trim();
    }

    public  ArrayList<QRT> Add_Query(String q)throws Exception
    {
        this.Set_Query_Type(q);
        this.Exteact_Query_Key_Terms(q);

        ArrayList<Integer> RD=this.Rank_Doucments(this.Get_Query_Doucments());
        ArrayList<QRT> Ret=new ArrayList<QRT>();
        String Title;
        String URL;
        for(int i=0;i<10;i++)
        {
            Title=Idx.Doucment_To_Words.get(RD.get(i)).get(0).get(Idx.Doucment_To_Words.get(RD.get(i)).get(0).size()-1);
            URL=Idx.Doucment_To_Words.get(RD.get(i)).get(0).get(Idx.Doucment_To_Words.get(RD.get(i)).get(0).size()-2);
            Ret.add(new QRT(Title,URL,RD.get(i),this.get_content(RD.get(i))));
        }
        return Ret;
    }



}
