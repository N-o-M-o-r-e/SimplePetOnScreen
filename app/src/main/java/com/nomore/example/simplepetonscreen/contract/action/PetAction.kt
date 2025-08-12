package com.nomore.example.simplepetonscreen.contract.action

sealed class PetAction {
    object StartPet : PetAction()
    object StopPet : PetAction()
    object CheckPermission : PetAction()
    object RequestPermission : PetAction()
    data class OnPermissionResult(val hasPermission: Boolean) : PetAction()
    object ClearError : PetAction()
}