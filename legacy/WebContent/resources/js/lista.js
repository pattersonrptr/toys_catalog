/**
 * Script para a página da tabela de administração.
 */

/**
 * Exibe uma menságem de confirmação ao usuário
 * OK: 		remove um brinquedo
 * Cancel:	cancela a ação
 */
function remover(e) {
	var res = confirm("Deseja excluir o brinquedo selecionado?");
	if (!res) {
		e.preventDefault();
		e.stopPropagation();
	}
}

/**
 * Ao carregar a página da lista de administraçãp
 * o campo de busca recebe o foco.
 */
$(document).ready(function() {
	$( "#filtro" ).focus();

});

