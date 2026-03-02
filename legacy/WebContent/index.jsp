<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!doctype html>

<html lang="pt-br">
<head>
<title>Brinquedos Pentagrama</title>
<meta charset="utf-8">
<meta http-equiv="Content-Language" content="pt-br">
<meta name="description" content="Catalogo de Brinquedos">
<meta name="author" content="Patterson Antonio da Silva Junior">

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<link rel="stylesheet" href="resources/bootstrap/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="resources/css/main.css" media="screen">

<script src="resources/jquery/jquery.min.js"></script>
<script src="resources/bootstrap/bootstrap/js/bootstrap.min.js"></script>
<script src="resources/js/main.js" charset="UTF-8"></script>


<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->

<title>Home</title>
</head>

<body>

	<div id="SiteBody" class="container">
		<div class="row">
			<header>
				<div id="baner">
					<h1  class="col-md-5">
						Unibrinq <br> <small>Brincando que se cresce.</small>
					</h1>
				</div>
			</header>
			<!-- /#banner -->

			<aside>
				<article id="main_menu" class="menu col-md-3">
					<h4>Menu Principal</h4>
					<nav>
						<ul class="nav nav-pills nav-stacked">
							<li class="active"><a href="lista?tipo=destaque" id="home" target="content">
							<span class="icon-home icon-white"></span>Home</a></li>
							<li><a href="lista?tipo=catalogo" id="cat" target="content">Catálogo de Brinquedos</a></li>
							<li><a href="lista" id="adm" target="content">Administração</a></li>
							<li><a href="sobre.jsp" id="sobre" target="content">Sobre o Sistema</a></li>
						</ul>
					</nav>
				</article>
			</aside>
			<!-- /#side_bar -->

			<section class="col-md-6">
				<h4 id="currentPage">Brinquedos em destaque</h4>
				<iframe id="main_frame" src="lista?tipo=destaque" name="content" width="800" height="850"></iframe>
			</section>
			<!-- /#main -->

			<footer>&copy; ${ano} www.unibrinq.com</footer>
			<!-- /#footer -->
		</div>
	</div>
</body>
</html>













