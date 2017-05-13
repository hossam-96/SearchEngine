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
	<link rel="stylesheet" href="assets/css/jqpagination.css" />


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
	<style>
		#container {
			display: block;
			position:relative
		}
		.ui-autocomplete {
			position: absolute;
		}
	</style>
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
			<a class="navbar-brand" href="index.jsp">AHM Search Engine</a>
		</div>
		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse" id="top-navbar-1">
			<ul class="nav navbar-nav navbar-right navbar-search-button">
				<li><a class="search-button" href="#"><i class="fa fa-search"></i></a></li>
			</ul>
			<form class="navbar-form navbar-right navbar-search-form disabled wow fadeInLeft" role="form" action="" method="get">
				<div class="form-group">
					<input type="text" id="SearchQuery" name="q" placeholder="Search..." class="search form-control">
					<div id="container"></div>
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
<script>
	var QRT=new Array();
</script>
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

		<%
			for(int i=(p-1)*10;i<r;i++)
			{
		%>
		<%="<script>QRT.push(['"+QR.get(i).Favicon+"','"+QR.get(i).URLs+"','"+QR.get(i).TITLE+"','"+QR.get(i).Content+"']);</script>"%>
		<%
			}
		%>
		<div class="row" id="results">

		</div>



	</div>
</div>




<%
	if(r!=0)
	{
%>
	<div class="gigantic pagination" id="pagination">
		<a href="#" class="first" data-action="first">&laquo;</a>
		<a href="#" class="previous" data-action="previous">&lsaquo;</a>
		<input type="text" readonly="readonly" />
		<a href="#" class="next" data-action="next">&rsaquo;</a>
		<a href="#" class="last" data-action="last">&raquo;</a>
	</div>
<%
	}
%>
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
<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script src="https://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<script src="assets/bootstrap/js/bootstrap.min.js"></script>
<script src="assets/js/jquery.backstretch.min.js"></script>
<script src="assets/js/wow.min.js"></script>
<script src="assets/js/waypoints.min.js"></script>
<script src="assets/js/scripts.js"></script>
<script src="assets/js/jquery.jqpagination.js"></script>

<!--[if lt IE 10]>
<script src="assets/js/placeholder.js"></script>
<![endif]-->
<script>
    $(function() {
        $("#SearchQuery").autocomplete({
            source: function(request, response) {
                $.ajax({
                    url: "GetData.jsp",
                    type: "POST",
                    dataType: "json",
                    data: { name: request.term},
                    success: function( data ) {

                        response( $.map( data, function( item ) {
                            return {
                                label: item.name,
                                value: item.value,
                            }
                        }));
                    },
                    error: function (error) {
                        alert('error: ' + error);
                    }
                });
            },
            minLength: 2,
            appendTo: "#container"
        });
    });
</script>
<script>
    function Show(start)
    {
        for(var i=start;i<Math.min(start+10,QRT.length);i++)
        {
            var result='<div class="col-sm-6 features-box wow fadeInLeft"> <div class="row"><div class="col-sm-3 features-box-icon"> <i><img src="'+QRT[i][0]+'" width="42" height="42"></i> </div> <div class="col-sm-9"> <h3><a href="'+QRT[i][1]+'" class="image" target="_blank">'+QRT[i][2]+'</a></h3> <p>'+QRT[i][3]+'</p> </div> </div> </div>';
            $('#results').append(result)
        }
    }
    Show(0);
</script>
<script>
    $(document).ready(function() {
        // hide all but the first of our paragraphs
        $('.some-container p:not(:first)').hide();

        $('.pagination').jqPagination({
            max_page    : Math.ceil(QRT.length/10),
            paged       : function(page) {
                var myNode = document.getElementById("results");
                while (myNode.firstChild) {
                    myNode.removeChild(myNode.firstChild);
                }
                var start=(page-1)*10;
                for(var i=start;i<Math.min(start+10,QRT.length);i++)
                {
                    var result='<div class="col-sm-6 features-box wow fadeInLeft"> <div class="row"><div class="col-sm-3 features-box-icon"> <i><img src="'+QRT[i][0]+'" width="42" height="42"></i> </div> <div class="col-sm-9"> <h3><a href="'+QRT[i][1]+'" class="image" target="_blank">'+QRT[i][2]+'</a></h3> <p>'+QRT[i][3]+'</p> </div> </div> </div>';
                    $('#results').append(result)
                }

            }
        });

    });
</script>
</body>

</html>
