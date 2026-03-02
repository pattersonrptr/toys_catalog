
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!doctype html>

<html lang="pt-br">
<head>
<meta charset="utf-8">
<meta http-equiv="Content-Language" content="pt-br">
<meta name="description" content="Catalogo de Brinquedos">
<meta name="author" content="Patterson Antonio da Silva Junior">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>${param.tipo}</title>

<link rel="stylesheet" href="resources/bootstrap/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="resources/css/lista.css" media="screen">

<script src="resources/jquery/jquery.min.js"></script>
<script src="resources/bootstrap/bootstrap/js/bootstrap.min.js"></script>

<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
</head>

<body>

	<c:if test="${param.tipo == 'catalogo'}">
		<h1 id="page" style="display: none;">${param.tipo}</h1>
		<table>
			<tr>
				<c:forEach items="${categorias}" varStatus="status" var="categoria">
					<td>
						<figure>
							<a
								href="lista?tipo=categoria&idCategoria=${fn:split(categoria, '#')[3]}
								&nomeCategoria=${fn:split(categoria, '#')[1]}"
								
								id="cat" target="content"> 
								<img src="${fn:split(categoria, '#')[2]}" alt="" class="img-rounded"
								title="titulo img">
							</a>
							<figcaption>
								<a
									href="lista?tipo=categoria&idCategoria=${fn:split(categoria, '#')[3]}
								&nomeCategoria=${fn:split(categoria, '#')[1]}" 	
									id="cat" target="content"> ${fn:split(categoria, '#')[1]}</a> <br>
								${fn:split(categoria, '#')[0]}&nbspiten(s)
							</figcaption>
						</figure>
					</td>
					<c:if test="${status.count % 2 eq 0}">
			</tr>
			<tr>
				</c:if>
				</c:forEach>
			</tr>
		</table>
	</c:if>

	<c:if test="${param.tipo == 'destaque'}">
		<h1 id="page" style="display: none;">${param.tipo}</h1>
		<table>
			<tr>
				<c:forEach items="${brinquedos}" varStatus="status" var="brinquedo">
					<td>
						<figure>
							<a href="detalhe?cod=${brinquedo.cod}&page=detalhes"
								target="content"> <img src="${brinquedo.imgUrl}"
								alt="${brinquedo.descricao}" class="img-rounded"
								title="titulo img">
							</a>
							<figcaption>
								${brinquedo.descricao}<br>R$ ${brinquedo.preco}
							</figcaption>
						</figure>
					</td>
					<c:if test="${status.count % 2 eq 0}">
			</tr>
			<tr>
				</c:if>
				</c:forEach>
			</tr>
		</table>
	</c:if>

	<c:if test="${param.tipo == 'categoria'}">
		<h1 id="page" style="display: none;">${categoria.nome}</h1>
		<table>
			<tr>
				<c:forEach items="${brinquedos}" varStatus="status" var="brinquedo">
					<td>
						<figure>
							<a href="detalhe?cod=${brinquedo.cod}&cat=${categoria.nome}" target="content"> <img
								src="${brinquedo.imgUrl}" alt="${brinquedo.descricao}"
								class="img-rounded" title="titulo img">
							</a>
							<figcaption>
								${brinquedo.descricao}<br>R$ ${brinquedo.preco}
							</figcaption>
						</figure>
					</td>
					<c:if test="${status.count % 2 eq 0}">
			</tr>
			<tr>
				</c:if>
				</c:forEach>
			</tr>
		</table>
	</c:if>
</body>