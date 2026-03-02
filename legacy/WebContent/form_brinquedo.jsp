<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>

<html lang="pt-br">
<head>
<meta charset="utf-8">
<meta http-equiv="Content-Language" content="pt-br">
<meta name="description" content="Catalogo de Brinquedos">
<meta name="author" content="Patterson Antonio da Silva Junior">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>${action}</title>

<link rel="stylesheet" href="resources/bootstrap/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="resources/highlight/default.css">
<link rel="stylesheet" href="resources/css/form.css">

<script type="text/javascript" src="resources/highlight/highlight.pack.js"></script>
<script type="text/javascript">hljs.initHighlightingOnLoad();</script>
<script type="text/javascript" src="resources/jquery/jquery.min.js"></script>
<script type="text/javascript" src="resources/bootstrap/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="resources/highlight/jquery.bsvalidate.min.js"></script>
<script type="text/javascript" src="resources/js/form.js"></script>

<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->

<script type="text/javascript">
	$(document).ready(function() {
		$('#brinqForm').bsValidate({
			fields : {
				inputCodigo : {
					required : {
						helpText : "Digite o código.",
						alert : "O campo Código deve ser preenchido corretamente."
					}
				},
				inputDesc : {
					required : {
						helpText : "Digite a descrição do brinquedo.",
						alert : "O campo Descrição deve ser preenchido."
					}
				},
				inputCat : {
					required : {
						helpText : "Digite o código da categoria",
						alert : "O Categoria deve ser preenchido corretamente."
					}
				},
				inputMarca : {
					required : {
						helpText : "Digite a marca do brinquedo",
						alert : "O campo Marca deve ser preenchido."
					}
				},
				inputPreco : {
					required : {
						helpText : "Digite o preço do brinquedo",
						alert : "O campo Preço deve ser preenchido corretamente."
					}
				}
			}
		});
	});
</script>

</head>

<body>
	<div style="width: 450px;">
		<form id="brinqForm" action="${action}" method="post" class="form-horizontal well" role="form">
			<input type="hidden" value="${brinquedo.cod}" name="cod" id="cod">
			
			<div class="form-group">
				<label class="control-label" for="inputCodigo">Código</label>
				<div class="controls">
					<input type="number" value="${brinquedo.cod >= 1 ? brinquedo.cod : 0}" class="form-control required" name="inputCodigo"
						id="inputCodigo" placeholder="Código"
<%-- 						${action == 'editar' ? 'disabled' : ''}  --%> disabled min="1" required="required">
						<div class="help-block with-errors"></div>
				</div>
			</div>

			<div class="form-group">
				<label class="control-label" for="inputDesc">Descrição</label>
				<div class="controls">
					<input type="text"  type="text" data-minlength="5" maxlength="80" value="${brinquedo.descricao}" class="form-control required" name="inputDesc"
						id="inputDesc" placeholder="Descrição" required="required" >
				</div>
			</div>

			<div class="form-group">
				<label class="control-label" for="inputCat">Categoria</label>
				<div class="controls">
					<input type="number" value="${brinquedo.codCategoria}"
						class="form-control required" name="inputCat" id="inputCat" placeholder="Categoria" required="required">
				</div>
			</div>

			<div class="form-group">
				<label class="control-label" for="inputMarca">Marca</label>
				<div class="controls">
					<input type="text" maxlength="80" value="${brinquedo.marca}" class="form-control required required" name="inputMarca"
						id="inputMarca" placeholder="Marca" required="required">
				</div>
			</div>

			<div class="form-group">
				<label class="control-label" for="inputImg">Imagem</label>
				<div class="container">
					<div class="row">
						<div>
							<!-- class="col-xs-12 col-md-6 col-md-offset-3 col-sm-8 col-sm-offset-2"-->
							<!-- image-preview-filename input [CUT FROM HERE]-->
							<div class="input-group image-preview">
								<input type="text" value="${brinquedo.imgUrl}"
									class="form-control image-preview-filename" readonly
									name="inputImg" id="inputImg" placeholder="Imagem">
								<!-- don't give a name === doesn't send on POST/GET -->
								<span class="input-group-btn"> <!-- image-preview-clear button -->
									<button type="button"
										class="btn btn-default image-preview-clear"
										style="display: none;">
										<span class="glyphicon glyphicon-remove"></span> Clear
									</button> <!-- prévia da imagem -->
									<div class="btn btn-default image-preview-input">
										<span class="glyphicon glyphicon-folder-open"></span> <span
											class="image-preview-input-title">Browse</span> <input
											type="file" accept="image/png, image/jpeg, image/gif"
											name="input-file-preview" />
										<!-- renomear -->
									</div>
								</span>
							</div>
							<!-- /input-group -->
						</div>
					</div>
				</div>
			</div>

			<div class="form-group">
				<label class="control-label" for="inputPreco">Preço</label>
				<div class="controls">
					<input type="number" step="any" value="${brinquedo.preco}" class="form-control required" name="inputPreco"
						id="inputPreco" placeholder="Preço" required="required">
				</div>
			</div>

			<div class="form-group">
				<label class="control-label" for="inputDetalhes">Detalhes</label>
				<div class="controls">
					<textarea rows="4" cols="50" class="form-control" name="inputDetalhes"
						id="inputDetalhes" placeholder="Detalhes">${brinquedo.detalhes}
						</textarea>
				</div>
			</div>
			
			
			<br>
			<button type="submit" class="btn btn-primary">Salvar</button>
		</form>
		
		
	</div>
</body>