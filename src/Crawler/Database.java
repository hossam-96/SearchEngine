/**
 * Created by Hosam on 09/03/17.
 */
package Crawler;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.sql.*;

public class Database {
    int MaxPopularRank;
    int MinPopularRank;
    public void insert(String fileName){
        try {
            String jdbcDriver = "com.mysql.jdbc.Driver";
            String db_url = "jdbc:mysql://localhost/link";

            String username = "root";
            String password = "Moha4422med";

            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            String sqlQuery;
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            while (line != null) {
                sqlQuery = "INSERT INTO seeds VALUES ('" + line + "');";
                stmt.executeUpdate(sqlQuery);
                line = br.readLine();
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    public void SetMaxPopularRank()throws Exception
    {
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String db_url = "jdbc:mysql://localhost/link";
        String username = "root";
        String password = "Moha4422med";
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(db_url, username, password);
        Statement stmt = conn.createStatement();
        String sqlQuery="SELECT max(rank) From links";
        ResultSet rs=stmt.executeQuery(sqlQuery);
        rs.next();
        this.MaxPopularRank=rs.getInt(1);
    }
    public void SetMinPopularRank()throws Exception
    {
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String db_url = "jdbc:mysql://localhost/link";
        String username = "root";
        String password = "Moha4422med";
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(db_url, username, password);
        Statement stmt = conn.createStatement();
        String sqlQuery="SELECT min(rank) From links";
        ResultSet rs=stmt.executeQuery(sqlQuery);
        rs.next();
        this.MinPopularRank= rs.getInt(1);
    }
    public float GetPopularRank(String Url)throws Exception
    {
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String db_url = "jdbc:mysql://localhost/link";
        String username = "root";
        String password = "Moha4422med";
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(db_url, username, password);
        Statement stmt = conn.createStatement();
        String sqlQuery="SELECT rank From links Where link='"+Url+"'";
        ResultSet rs=stmt.executeQuery(sqlQuery);
        if(rs.next())
        {
            int rank=rs.getInt(1);
            return ((float)rank)/this.MaxPopularRank;
        }
        return 1;
    }
    public void create(String user, String pass){
        try {
            String jdbcDriver = "com.mysql.jdbc.Driver";
            String db_url = "jdbc:mysql://localhost/";

            String username = user;
            String password = pass;

            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            String sqlQuery = "Create Database link";
            stmt.executeUpdate(sqlQuery);
            stmt.close();
            conn = DriverManager.getConnection(db_url + "link",username,password);
            stmt = conn.createStatement();
            sqlQuery = "CREATE TABLE threads(" +
                    "thread int NOT NULL," +
                    "seed boolean DEFAULT TRUE," +
                    "row int DEFAULT 0," +
                    "numOfPages int DEFAULT 0);";
            stmt.executeUpdate(sqlQuery);
            sqlQuery = "CREATE TABLE links(" +
                    "link VARCHAR(255) NOT NULL," +
                    "thread INT NOT NULL," +
                    "rank INT DEFAULT 1," +
                    "PRIMARY KEY (link));";
            stmt.executeUpdate(sqlQuery);
            sqlQuery = "CREATE TABLE seeds(" +
                    "link VARCHAR(255) NOT NULL);";
            stmt.executeUpdate(sqlQuery);
            sqlQuery = "CREATE TABLE robots(" +
                    "disallow VARCHAR(255) NOT NULL," +
                    "PRIMARY KEY (disallow));";
            stmt.executeUpdate(sqlQuery);
            sqlQuery = "CREATE TABLE refresh(" +
                    "thread INT," +
                    "pageNum INT," +
                    "refresh INT DEFAULT 24," +
                    "link VARCHAR(255)," +
                    "PRIMARY KEY (thread,pageNum));";
            stmt.executeUpdate(sqlQuery);
            sqlQuery = "CREATE TABLE querys(" +
                    "query VARCHAR (1000)," +
                    "PRIMARY KEY (query));";
            stmt.executeUpdate(sqlQuery);
            stmt.close();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public List<String> GetAjaxData(String Query)throws Exception
    {
        Query="%"+Query+"%";
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String db_url = "jdbc:mysql://localhost/link";
        String username = "root";
        String password = "Moha4422med";
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(db_url, username, password);
        PreparedStatement stmt = conn.prepareStatement("SELECT query From querys Where query Like ? ORDER By query Limit 10");
        stmt.setString(1, Query);
        ResultSet rs=stmt.executeQuery();
        List<String>Ret=new ArrayList<>();
        while(rs.next())
        {
            Ret.add(rs.getString(1));
        }
        return Ret;
    }

    public void insertQuery(String Query){
        try {
            String jdbcDriver = "com.mysql.jdbc.Driver";
            String db_url = "jdbc:mysql://localhost/link";

            String username = "root";
            String password = "Moha4422med";

            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO querys VALUES ( ? )");
            stmt.setString(1, Query);
            stmt.executeUpdate();
        }catch (Exception e){
        }
    }

    public static void main(String[] args)throws Exception
    {
        Database Mysqldb=new Database();
        Mysqldb.insertQuery("baker");

    }
}

