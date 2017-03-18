<%@ page import="java.util.ArrayList" %>
<%@ page import="Main_Package.QRT" %>
<!DOCTYPE html>
<html lang="en">

<head>

	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>AHM Search Engine</title>

	<!-- CSS -->
	<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:100,100i,300,300i,500,500i">
	<link rel="stylesheet" href="assets/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="assets/font-awesome/css/font-awesome.min.css">
	<link rel="stylesheet" href="assets/css/animate.css">
	<link rel="stylesheet" href="assets/css/style.css">

	<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
	<!--[if lt IE 9]>
	<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
	<script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
	<![endif]-->

	<!-- Favicon and touch icons -->
	<link rel="shortcut icon" href="assets/ico/favicon.png">
	<link rel="apple-touch-icon-precomposed" sizes="144x144" href="assets/ico/apple-touch-icon-144-precomposed.png">
	<link rel="apple-touch-icon-precomposed" sizes="114x114" href="assets/ico/apple-touch-icon-114-precomposed.png">
	<link rel="apple-touch-icon-precomposed" sizes="72x72" href="assets/ico/apple-touch-icon-72-precomposed.png">
	<link rel="apple-touch-icon-precomposed" href="assets/ico/apple-touch-icon-57-precomposed.png">

</head>

<body>

<!-- Top menu -->
<nav class="navbar navbar-inverse navbar-fixed-top navbar-no-bg" role="navigation">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#top-navbar-1">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="">AHM Search Engineate</a>
		</div>
		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse" id="top-navbar-1">
			<ul class="nav navbar-nav navbar-right navbar-search-button">
				<li><a class="search-button" href="#"><i class="fa fa-search"></i></a></li>
			</ul>
			<form class="navbar-form navbar-right navbar-search-form disabled wow fadeInLeft" role="form" action="" method="get">
				<div class="form-group">
					<input type="text" name="q" placeholder="Search..." class="search form-control">
				</div>
			</form>
			<ul class="nav navbar-nav navbar-right navbar-menu-items wow fadeIn">
			</ul>
		</div>
	</div>
</nav>

<!-- Top content -->
<div class="top-content">
	<div class="container">

		<div class="row">
			<div class="col-sm-12 text wow fadeInLeft">
				<h1>AHM <strong>Search</strong> Engine</h1>
				<div class="description">
					<p class="medium-paragraph">
						enjoy our simple search engine
					</p>
				</div>
			</div>
		</div>

	</div>
</div>

<!-- Features -->
<div class="features-container section-container">
	<div class="container">

		<div class="row">
			<div class="col-sm-12 features section-description wow fadeIn">
				<h2>Query Search Results</h2>
				<div class="divider-1 wow fadeInUp"><span></span></div>
			</div>
		</div>


		<%
			ArrayList<QRT> QR=(ArrayList<QRT>)request.getAttribute("QR");
			int r=0;
			int p=1;
			if(request.getParameter("p")!=null)p=Integer.parseInt(request.getParameter("p"));
			if(QR!=null) r=QR.size();
			if(r==0)
			{
		%>
		<p align="center"><h6 style="color:#ff0500;" >Please Enter Some KeyWords</h6></p>
		<%
			}
		%>



		<div class="row">
			<%
				for(int i=(p-1)*10;i<Math.min(r,(p-1)*10+10);i++)
				{
			%>
			<div class="col-sm-6 features-box wow fadeInLeft">
				<div class="row">
					<div class="col-sm-3 features-box-icon">
						<i><img src="<%=QR.get(i).Favicon%>"></i>
					</div>
					<div class="col-sm-9">
						<h3><a href="<%=QR.get(i).URLs%>" class="image" target="_blank"><%=QR.get(i).TITLE%></a></h3>
						<p>
							<%=QR.get(i).Content%>
						</p>
					</div>
				</div>
			</div>
			<%
				}
			%>

		</div>



	</div>
</div>



<%String next="?";
	if(r!=0)next+="q="+request.getParameter("q")+"&&p=";
	else next+="p=";
%>
<div class="container">
	<ul class="pagination">

		<% if(p!=1){%>
		<li><a  href=<%=next+"1"%>>First</a></li>
		<% }else{%>
		<li class="disabled"><a  href="#">First</a></li>
		<% }%>


		<% if(p!=1){%>
		<li ><a href="<%=next+Integer.toString(p-1)%>"><%="<<"%></a></li>
		<% }else{%>
		<li class="disabled"><a  href="#"><%="<<"%></a></li>
		<% }%>

		<% if((p-1)*10<r){%>
		<li ><a href="<%=next+Integer.toString(p)%>"><%=p%></a></li>
		<% }else{%>
		<li class="disabled"><a  href="#"><%=p%></a></li>
		<% }%>


		<% if((p)*10<r){%>
		<li ><a href="<%=next+Integer.toString(p+1)%>"><%=p+1%></a></li>
		<% }else{%>
		<li class="disabled"><a  hre="#"><%=p+1%></a></li>
		<% }%>

		<% if((p+1)*10<r){%>
		<li ><a href="<%=next+Integer.toString(p+2)%>"><%=p+2%></a></li>
		<% }else{%>
		<li class="disabled"><a  hre="#"><%=p+2%></a></li>
		<% }%>


		<% if((p+2)*10<r){%>
		<li ><a href="<%=next+Integer.toString(p+3)%>"><%=p+3%></a></li>
		<% }else{%>
		<li class="disabled"><a  hre="#"><%=p+3%></a></li>
		<% }%>

		<% if((p+3)*10<r){%>
		<li ><a href="<%=next+Integer.toString(p+4)%>"><%=p+4%></a></li>
		<% }else{%>
		<li class="disabled"><a  hre="#"><%=p+4%></a></li>
		<% }%>

		<% if((p)*10<r){%>
		<li ><a href="<%=next+Integer.toString(p+1)%>"><%=">>"%></a></li>
		<% }else{%>
		<li class="disabled"><a  hre="#"><%=">>"%></a></li>
		<% }%>


		<% if(Math.ceil(r/10.00)!=p&&r!=0){%>
		<li ><a href="<%=next+Integer.toString((int)Math.ceil(r/10.00))%>"><%="Last"%></a></li>
		<% }else{%>
		<li class="disabled"><a  hre="#"><%="Last"%></a></li>
		<% }%>






	</ul>
</div>
<!-- Footer -->
<footer>
	<div class="container">
		<div class="row">
			<div class="col-sm-12 footer-copyright">
				&copy; copyright to AHM
			</div>
		</div>
	</div>
</footer>


<!-- Javascript -->
<script src="assets/js/jquery-1.11.1.min.js"></script>
<script src="assets/bootstrap/js/bootstrap.min.js"></script>
<script src="assets/js/jquery.backstretch.min.js"></script>
<script src="assets/js/wow.min.js"></script>
<script src="assets/js/waypoints.min.js"></script>
<script src="assets/js/scripts.js"></script>

<!--[if lt IE 10]>
<script src="assets/js/placeholder.js"></script>
<![endif]-->

</body>

</html>