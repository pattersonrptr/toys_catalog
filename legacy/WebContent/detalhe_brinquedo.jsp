<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!doctype html>

<html lang="pt-br">
<head>
<meta charset="utf-8">
<meta http-equiv="Content-Language" content="pt-br">
<meta name="description" content="Catalogo de Brinquedos">
<meta name="author" content="Patterson Antonio da Silva Junior">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>Brinquedo</title>

<link rel="stylesheet" href="resources/bootstrap/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="resources/css/detalhe.css" media="screen">

<script src="resources/jquery/jquery.min.js"></script>
<script src="resources/bootstrap/bootstrap/js/bootstrap.min.js"></script>

<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
</head>

<body>
	<h1 id="page" style="display: none;"><a href='javascript:history.back()'>${cat}</a>::${brinquedo.descricao}</h1>
	<table>
		<tr>
			<td>
				<div id="brinquedoImg">
					<figure>
						<img src="${brinquedo.imgUrl}" alt="${brinquedo.imgUrl}" 
						class="img-rounded" title="${brinquedo.detalhes}">
					</figure>
				</div>

				<div id="brinquedoDados">
					<p>Código: ${brinquedo.cod}</p>
					<br>
					<h4>${brinquedo.descricao}</h4>
					<br>
					<p>R$ ${brinquedo.preco}</p>
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div id="brinquedoDetalhes">
					<div class="panel  panel-info">
						<div class="panel-heading">
							<h3 class="panel-title">Detalhes</h3>
						</div>
						<div class="panel-body">
							<c:out value="${brinquedo.detalhes}" />
						</div>
					</div>
				</div>
			</td>
		</tr>
	</table>
</body>