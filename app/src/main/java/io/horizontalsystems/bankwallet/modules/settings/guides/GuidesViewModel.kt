package io.horizontalsystems.bankwallet.modules.settings.guides

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.bankwallet.entities.Guide
import io.horizontalsystems.bankwallet.entities.GuideCategory
import io.reactivex.disposables.CompositeDisposable

class GuidesViewModel(private val repository: GuidesRepository) : ViewModel() {

    val guides = MutableLiveData<List<Guide>>()
    val loading = MutableLiveData(false)
    val categories = MutableLiveData<List<String>>()
    val error = MutableLiveData<Throwable?>()
    var selectedCategoryIndex = 0

    private var guideCategories: Array<GuideCategory> = arrayOf()
    private var disposables = CompositeDisposable()

    init {
        repository.guideCategories
                .subscribe {
                    loading.postValue(it is DataState.Loading)

                    if (it is DataState.Success) {
                        didFetchGuideCategories(it.data)
                    }

                    error.postValue((it as? DataState.Error)?.throwable)
                }
                .let {
                    disposables.add(it)
                }
    }

    fun onSelectFilter(selectedFilterIndex: Int) {
        selectedCategoryIndex = selectedFilterIndex
        syncViewItems()
    }

    override fun onCleared() {
        disposables.dispose()

        repository.clear()
    }

    private fun didFetchGuideCategories(guideCategories: Array<GuideCategory>) {
        this.guideCategories = guideCategories

        categories.postValue(guideCategories.map { it.category })

        syncViewItems()
    }

    private fun syncViewItems() {
        guides.postValue(guideCategories[selectedCategoryIndex].guides)
    }
}
