package net.arwix.placeselectorfeature.parts.list.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import net.arwix.placeselector.data.inner.InnerEditDataHolder
import net.arwix.placeselector.parts.list.domain.PlaceListUseCase
import net.arwix.placeselector.parts.list.ui.PlaceListViewModel
import javax.inject.Inject

@HiltViewModel
class DemoListViewModel @Inject constructor(
    placeListUseCase: PlaceListUseCase,
    innerEditDataHolder: InnerEditDataHolder

) : PlaceListViewModel(placeListUseCase, innerEditDataHolder)
