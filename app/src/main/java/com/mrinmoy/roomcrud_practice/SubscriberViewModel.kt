package com.mrinmoy.roomcrud_practice

import android.util.Patterns
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrinmoy.roomcrud_practice.db.Subscriber
import com.mrinmoy.roomcrud_practice.db.SubscriberRepository
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Pattern

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel(), Observable {

    val subscribers = repository.subscribers
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete: Subscriber

    @Bindable
    val inputName = MutableLiveData<String?>()

    @Bindable
    val inputEmail = MutableLiveData<String?>()

    @Bindable
    val saveorUpdateButton = MutableLiveData<String>()

    @Bindable
    val clearAllData = MutableLiveData<String>()

    private val statusMessage = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>>
        get() = statusMessage

    init {
        saveorUpdateButton.value = "Save"
        clearAllData.value = "Clear All"
    }

    fun saveOrUpdate() {

        if (inputName.value == null) {
            statusMessage.value = Event("Enter Subscriber Name")
        } else if (inputEmail.value == null) {
            statusMessage.value = Event("Enter Subscriber Email")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.value!!).matches()) {
            statusMessage.value = Event("Enter Correct Email")
        } else {
            if (isUpdateOrDelete) {
                subscriberToUpdateOrDelete.name = inputName.value!!
                subscriberToUpdateOrDelete.email = inputEmail.value!!
                update(subscriberToUpdateOrDelete)
            } else {
                val name: String = inputName.value!!
                val email: String = inputEmail.value!!
                insert(Subscriber(0, name, email))
                inputName.value = null
                inputEmail.value = null
            }
        }
    }

    fun clearAllData() {

        if (isUpdateOrDelete) {
            delete(subscriberToUpdateOrDelete)
        } else {
            clearAll()
        }
    }

    fun insert(subscriber: Subscriber) {
        viewModelScope.launch {

            val newRowID = repository.insert(subscriber)
            if (newRowID > -1) {
                statusMessage.value = Event("Subscriber Inserted Sucessfullt $newRowID")
            } else {
                statusMessage.value = Event("Error Occured")
            }
        }
    }

    fun delete(subscriber: Subscriber) {
        viewModelScope.launch {
            val noOfRows = repository.delete(subscriber)
            if (noOfRows > 0) {
                inputName.value = null
                inputEmail.value = null
                isUpdateOrDelete = false
                saveorUpdateButton.value = "Save"
                clearAllData.value = "Clear All"
                statusMessage.value = Event("$noOfRows Subscriber Deleted Sucessfullt")
            } else {
                statusMessage.value = Event("Error Occured")
            }
        }
    }

    fun update(subscriber: Subscriber) {
        viewModelScope.launch {
            val numberOfRows = repository.update(subscriber)
            if (numberOfRows > 0) {
                inputName.value = null
                inputEmail.value = null
                isUpdateOrDelete = false
                saveorUpdateButton.value = "Save"
                clearAllData.value = "Clear All"
                statusMessage.value = Event("$numberOfRows Subscriber Updated Sucessfullt")
            } else {
                statusMessage.value = Event("Error Occured")
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            val noOfRows = repository.deleteAll()
            if (noOfRows > 0) {
                statusMessage.value = Event("$noOfRows Old Subscribers Deleted Sucessfullt")
            } else {
                statusMessage.value = Event("Error Occured")
            }
        }
    }

    fun initUpdateAndDelete(subscriber: Subscriber) {
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = subscriber
        saveorUpdateButton.value = "Update"
        clearAllData.value = "Delete"
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

}