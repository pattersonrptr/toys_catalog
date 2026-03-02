<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="pt_BR" /> <!-- CONVERTE números do formato americano para o brasileiro -->

<!doctype html>

<html lang="pt-br" xmlns="http://www.w3.org/1999/xhtml"  
    xmlns:h="http://java.sun.com/jsf/html"  
    xmlns:f="http://java.sun.com/jsf/core"  
    xmlns:p="http://primefaces.org/ui">
<head>
	<meta charset="utf-8">
	<meta http-equiv="Content-Language" content="pt-br">
	<meta name="description" content="Catalogo de Brinquedos">
	<meta name="author" content="Patterson Antonio da Silva Junior">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Administração</title>
	
	<link rel="stylesheet" href="resources/bootstrap/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="resources/css/lista.css" media="screen">
	
	<script type="text/javascript" src="resources/jquery/jquery.min.js"></script>
	<script type="text/javascript" src="resources/bootstrap/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="resources/js/lista.js"></script>
	
	<!--[if lt IE 9]>
	    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
	<![endif]-->
	
	<script type="text/javascript">
		$(document).ready(function() {
			$('#filtro').keyup(function() {
				var myForm = $('#form_busca');
				$.get(myForm.attr('action'), myForm.serialize(), function(data) {
					// alert($(data).filter("#tb").html());
					$("#tb").html($(data).filter("#tb").html());
				});
			});
		});
	</script>
	
</head>

<body>
	<h1 id="page" style="display: none;">Administração</h1>
	<p id="teste"></p>
	<div class="search">
		<form id="form_busca" action="lista" class="search_field navbar-form navbar-left" role="search">
			<div class="form-group">
				<input id="filtro" type="text" name="filtro" class="form-control" placeholder="Busca" autocomplete="off">
			</div>
			<div class="form-group">
				<div class="controls">
					<button id="refresh-btn" type="submit" class="btn btn-primary">
		  				<span class="glyphicon glyphicon-search" aria-hidden="true"></span> Buscar
					</button>
				</div>
			</div>
		</form>
	</div>
	<br><br><br>
	<c:choose>
		<c:when test="${not empty brinquedos}">
		<div id="tb" class="main_content">
			<div  class="adm_list">
				<table  class="container_tb table table-hover">
					<tr>
						<th>Descriçao</th>
						<th>Categoria</th>
						<th>Preços</th>
						<th colspan="2">CONTROLES</th>
					</tr>
					<c:forEach items="${brinquedos}" var="brinquedo">
						<tr>
							<td>${brinquedo.descricao}</td>
							<td>Cat${brinquedo.codCategoria}</td>
							<td>R$ <fmt:formatNumber value="${brinquedo.preco}" minFractionDigits="2" /></td>
							<td><a href="editar?cod=${brinquedo.cod}">editar</a></td>
							<td><a href="excluir?cod=${brinquedo.cod}" onclick="remover(event);">excluir</a></td>
						</tr>
					</c:forEach>
				</table>
			</div>
		</div>
		</c:when>
		<c:otherwise>
			<p>Ainda não há brinquedos cadastrados!</p>
		</c:otherwise>
	</c:choose>
	<a id="novo" href="novo" class="btn btn-primary">Novo Brinquedo</a>
</body>
</html>