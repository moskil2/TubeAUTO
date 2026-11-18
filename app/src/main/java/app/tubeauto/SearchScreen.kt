package app.tubeauto

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template

/**
 * Etap 5: ekran wpisywania tekstu z klawiaturą Android Auto na ekranie auta.
 * Otwiera się, gdy na stronie stuknięto pole tekstowe. Po zatwierdzeniu wraca
 * do strony i przekazuje wpisany tekst. "Wstecz" wraca bez wpisywania.
 * W czasie jazdy Android Auto sam blokuje klawiaturę (zasada Google).
 */
class SearchScreen(
    carContext: CarContext,
    private val onSubmit: (String) -> Unit
) : Screen(carContext) {

    override fun onGetTemplate(): Template =
        SearchTemplate.Builder(object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {}

            override fun onSearchSubmitted(searchText: String) {
                screenManager.pop()
                if (searchText.isNotBlank()) onSubmit(searchText)
            }
        })
            .setHeaderAction(Action.BACK)
            .setSearchHint("Type here")
            .setShowKeyboardByDefault(true)
            .setItemList(
                ItemList.Builder()
                    .setNoItemsMessage("Type and confirm")
                    .build()
            )
            .build()
}
