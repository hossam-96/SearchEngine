package Main_Package;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by moham on 3/12/2017.
 */
public class Servlet extends HttpServlet {

    private Query Q=new Query();
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       //response.setContentType("text/html");
       //PrintWriter out=response.getWriter();
        try
        {
            ArrayList<QRT> QR=null;
            if(request.getParameter("q")!=null)
            {
               QR = Q.Add_Query(request.getParameter("q"));
            }
            request.setAttribute("QR", QR);
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/index.jsp");
            dispatcher.forward(request, response);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
    }
}
