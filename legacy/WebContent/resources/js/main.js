/**
 * Script principal, contém funções que podem ser usadas em qualquer página do site
 */

/**
 * 
 */
$(document).ready(function() {
	$(".menu li").click(function(e) {
		$(".menu li").removeClass('active');
		var $this = $(this);
		if (!$this.hasClass('active')) {
			$this.addClass('active');
		}
		// e.preventDefault();
		e.stopPropagation();
	});

	/**
	 * Ações que são executadas ao carregar o elemento IFRAME
	 * muda as informações do menu de navegação do site dependendo da página que for
	 * carregada no IFRAME ' main_frame ' da página principal ' index.jsp '
	 * 
	 */
	$("#main_frame").load(function() {
		$(this).load(function() {
			var titulo = $(this).contents().find("title").html();
			var page = $(this).contents().find("#page").html();

			if(titulo == "catalogo") {
				changeNavLink ("Catálogo de brinquedos");
			} else
				if(titulo == "destaque") {
					changeNavLink ("Brinquedos em destaque");
				} else
					if(titulo == "novo") {
						changeNavLink ("<a href='javascript:history.back()'>Administração</a>::Novo Brinquedo");
					} else
						if(titulo == "editar") {
							changeNavLink ("<a href='javascript:history.back()'>Administração</a>::Alteração");
						} else
							if(titulo == "categoria") {
								changeNavLink ("<a href='javascript:history.back()'>Catálogo de brinquedos</a>::" + page);
							} else
								if(titulo == "Brinquedo") {
									var pages = $(this).contents().find("#page").html().split("::");
									if(pages[0].split("><")[1]) {
										changeNavLink ("<a href='javascript: window.history.back()'>Brinquedos em destaque</a>" + page);
									} else {
										changeNavLink ("<a href='javascript: window.history.go(-2)'>Catálogo de brinquedos</a>::" + page);
									}
								}
								else {
									changeNavLink(titulo);
								}
		});
	});
});

/**
 * changeNavLink() Muda os links do menu de navegação conforme a página que for passada
 * como argumento
 * @param currPage Página atual, carregada no IFRAME ' main_frame '
 */
function changeNavLink (currPage) {
	$('#currentPage', window.parent.document).html(currPage);
}



