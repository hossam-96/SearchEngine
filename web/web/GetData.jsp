<%@ page contentType="text/html; charset=iso-8859-1" language="java"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="Crawler.Database"%>
<%@ page import="net.sf.json.JSONObject,net.sf.json.JSONArray"%>
<%
    Database Mysqldb = new Database();
    String name = request.getParameter("name");
    List<String> Results = Mysqldb.GetAjaxData(name);


    JSONArray jsonArr = new JSONArray();

    JSONObject json=new JSONObject();

    Iterator<String> iterator = Results.iterator();
    while(iterator.hasNext()) {
        String Query = (String)iterator.next();
        json.put("name",Query);
        json.put("value",Query);
        jsonArr.add(json);
    }

    out.println(jsonArr);
%>


<%


%>
